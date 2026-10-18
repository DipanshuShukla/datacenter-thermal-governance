import json
import logging
from confluent_kafka import Consumer, Producer
from .config import Config

logger = logging.getLogger(__name__)

class TelemetryMessagingClient:
    def __init__(self):
        self.consumer = Consumer({
            'bootstrap.servers': Config.KAFKA_BOOTSTRAP_SERVERS,
            'group.id': Config.KAFKA_CONSUMER_GROUP,
            'auto.offset.reset': 'latest'
        })
        self.producer = Producer({
            'bootstrap.servers': Config.KAFKA_BOOTSTRAP_SERVERS
        })

    def subscribe(self):
        self.consumer.subscribe([Config.KAFKA_RAW_TOPIC])
        logger.info(f"Subscribed to topic: {Config.KAFKA_RAW_TOPIC}")

    def poll_message(self, timeout=1.0):
        msg = self.consumer.poll(timeout)
        if msg is None:
            return None
        if msg.error():
            logger.error(f"Kafka Consumer Error: {msg.error()}")
            return None

        try:
            return json.loads(msg.value().decode('utf-8'))
        except json.JSONDecodeError as e:
            logger.error(f"Failed to decode telemetry JSON: {e}")
            return None

    def publish_prediction(self, routing_key, payload):
        try:
            key_bytes = routing_key.encode('utf-8') if routing_key else b'UNKNOWN'
            val_bytes = json.dumps(payload).encode('utf-8')

            self.producer.produce(
                topic=Config.KAFKA_PREDICTED_TOPIC,
                key=key_bytes,
                value=val_bytes
            )
            # Serve delivery reports asynchronously
            self.producer.poll(0)
        except Exception as e:
            logger.error(f"Failed to publish to {Config.KAFKA_PREDICTED_TOPIC}: {e}")

    def shutdown(self):
        self.consumer.close()
        self.producer.flush()
import sys
import logging
from src.kafka_client import TelemetryMessagingClient
from src.physics_engine import ThermodynamicEngine

# Configure standard stdout logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s | %(levelname)s | %(message)s',
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger(__name__)

def main():
    logger.info("Starting ENGINPILOT THERM-NET Physics Worker...")

    kafka_client = TelemetryMessagingClient()
    kafka_client.subscribe()

    try:
        while True:
            # 1. Consume Raw Telemetry
            raw_data = kafka_client.poll_message(timeout=1.0)

            if not raw_data:
                continue

            # 2. Calculate Physics
            predicted_data = ThermodynamicEngine.calculate_prediction(raw_data)

            # 3. Publish Enriched Telemetry
            datacenter_id = raw_data.get('datacenter_id', 'DC_UNKNOWN')
            rack_id = raw_data.get('rack_id', 'RACK_UNKNOWN')

            kafka_client.publish_prediction(routing_key=datacenter_id, payload=predicted_data)

            logger.info(
                f"Rack: {rack_id} | T_Pred: {predicted_data['t_predicted_c']}°C | "
                f"Delta: {predicted_data['temp_delta']} | Engine: {'CoolProp' if predicted_data['used_coolprop'] else 'Fallback'}"
            )

    except KeyboardInterrupt:
        logger.info("Received shutdown signal. Closing gracefully...")
    except Exception as e:
        logger.error(f"Fatal error in main loop: {e}", exc_info=True)
    finally:
        kafka_client.shutdown()
        logger.info("Physics Worker offline.")

if __name__ == '__main__':
    main()
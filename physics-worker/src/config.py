import os
from dotenv import load_dotenv

# Load local .env file if it exists (ignored in Docker)
load_dotenv()

class Config:
    KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    KAFKA_RAW_TOPIC = os.getenv("KAFKA_RAW_TELEMETRY_TOPIC_NAME", "telemetry_raw")
    KAFKA_PREDICTED_TOPIC = os.getenv("KAFKA_PREDICTED_TOPIC_NAME", "telemetry_predicted")
    KAFKA_CONSUMER_GROUP = os.getenv("KAFKA_CONSUMER_GROUP_ID", "physics-worker-group")

    # Standard atmospheric pressure in Pascals for CoolProp
    ATM_PRESSURE_PA = 101325
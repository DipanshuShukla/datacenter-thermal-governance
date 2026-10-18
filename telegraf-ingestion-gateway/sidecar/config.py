import os
from dotenv import load_dotenv

# Load local .env file if it exists (ignored in Docker)
load_dotenv()

class Config:
    CONFIG_DIR = os.getenv("TELEGRAF_CONFIG_DIR", "/etc/telegraf/telegraf.d")
    CONFIG_API_URI = os.getenv("CONFIG_API_URI", "http://localhost")
    POLL_INTERVAL = int(os.getenv("POLL_INTERVAL", 60))
import os
import time
import signal
import requests

from config import Config


def start():
    print("Starting Config Pull Sidecar...")
    known_configs = set(os.listdir(Config.CONFIG_DIR))
    
    while True:
        try:
            # TODO: add authentication headers here
            response = requests.get(Config.CONFIG_API_URI)
            if response.status_code == 200:
                payload = response.json() # Expected format: {"device_123.conf": "[[inputs.modbus]]..."}
                
                reload_needed = False
                for filename, config_data in payload.items():
                    if filename not in known_configs:
                        filepath = os.path.join(Config.CONFIG_DIR, filename)
                        with open(filepath, 'w') as f:
                            f.write(config_data)
                        
                        known_configs.add(filename)
                        reload_needed = True
                        print(f"Written new config: {filename}")
                
                if reload_needed:
                    print("New Config Loaded. Sending SIGHUP to Telegraf (PID 1)")
                    os.kill(1, signal.SIGHUP)
                    
        except Exception as e:
            print(f"Error fetching configs: {e}")
            
        time.sleep(Config.POLL_INTERVAL)


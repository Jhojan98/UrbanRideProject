import paho.mqtt.client as mqtt
import json
import time
import random
import uuid
import sys

# Configuration
BROKER = "34.9.26.232"
PORT = 1883
TOPIC_TEMPLATE = "bikes/{}/telemetry"
NUM_BIKES = 5

# API Configuration
API_BASE_URL = "http://34.9.26.232:8090/bicy"
API_MECHANIC = f"{API_BASE_URL}/mechanic"
API_ELECTRIC = f"{API_BASE_URL}/electric"

# Villavicencio approximate bounding box
LAT_MIN = 4.11
LAT_MAX = 4.18
LON_MIN = -73.67
LON_MAX = -73.58

def generate_bike_ids(n):
    # Set seed for reproducible IDs
    random.seed(42)
    ids = []
    for _ in range(n):
        prefix = random.choice(["ELEC", "MECH"])
        number = random.randint(100000, 999999)
        ids.append(f"{prefix}-{number}")
    # Reset seed to avoid identical simulation behavior if not desired, 
    # though keeping it ensures full reproducibility.
    # We'll leave it set for now as 'patron repetido' likely implies full state.
    return ids

def get_random_coordinate():
    lat = random.uniform(LAT_MIN, LAT_MAX)
    lon = random.uniform(LON_MIN, LON_MAX)
    return lat, lon

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Connected to MQTT Broker!")
    else:
        print(f"Failed to connect, return code {rc}")

def check_bike_exists(bike_id):
    try:
        response = requests.get(f"{API_BASE_URL}/{bike_id}", timeout=5)
        if response.status_code == 200:
            return True
        return False
    except Exception as e:
        print(f"Error checking bike {bike_id}: {e}")
        return False

def create_bike(bike_id):
    try:
        bike_type = bike_id.split("-")[0] # ELEC or MECH
        url = API_ELECTRIC if bike_type == "ELEC" else API_MECHANIC
        
        # Varying the series/model as requested
        model_series = random.randint(2020, 2025)
        
        # Removing "id" from body as it likely causes 500 Error if server generates it
        payload = {
            "series": model_series,
            "padlockStatus": "UNLOCKED"
        }
        
        headers = {"Content-Type": "application/json"}
        response = requests.post(url, json=payload, headers=headers, timeout=5)
        
        if response.status_code in [200, 201]:
            try:
                data = response.json()
                # Assuming the server returns the created object with an "id" or "idBicycle"
                created_id = data.get("id") or data.get("idBicycle")
                if created_id:
                     print(f"Created bike (Series {model_series}). Server assigned ID: {created_id}")
                     return created_id
                else:
                    print(f"Created bike but could not parse ID from response: {data}")
                    return None
            except:
                print(f"Created bike (Series {model_series}) but response not JSON.")
                return None
        else:
            print(f"Failed to create bike {bike_id}: {response.status_code} - {response.text}")
            return None
    except Exception as e:
        print(f"Error creating bike {bike_id}: {e}")
        return None

class Bike:
    def __init__(self, bike_id):
        self.id = bike_id
        self.lat, self.lon = get_random_coordinate()
        self.battery = round(random.uniform(50.0, 100.0), 2)
        self.padlock_status = random.choice(["LOCKED", "UNLOCKED"])
        
    def update(self):
        # Simulate movement if unlocked
        if self.padlock_status == "UNLOCKED":
             # Move slightly (~10 meters)
            self.lat += random.uniform(-0.0001, 0.0001)
            self.lon += random.uniform(-0.0001, 0.0001)
            # Drain battery faster when moving
            self.battery -= random.uniform(0.05, 0.2)
        else:
            # Drain battery slowly when idle
            self.battery -= random.uniform(0.0, 0.01)
            
        # Randomly toggle status rarely (1% chance)
        if random.random() < 0.01:
            self.padlock_status = "LOCKED" if self.padlock_status == "UNLOCKED" else "UNLOCKED"
            
        # Keep battery within bounds
        if self.battery < 0:
            self.battery = 100.0 # Recharged
        
        # Keep within Villavicencio bounds (clamp)
        self.lat = max(LAT_MIN, min(LAT_MAX, self.lat))
        self.lon = max(LON_MIN, min(LON_MAX, self.lon))

def main():
    print("Checking dependencies...", flush=True)
    try:
        import paho.mqtt.client as mqtt
        global requests
        import requests
    except ImportError as e:
        print(f"Error: Missing dependency {e.name}.")
        print("Please install: pip install paho-mqtt requests")
        sys.exit(1)

    print(f"Generating {NUM_BIKES} bike IDs...", flush=True)
    initial_ids = generate_bike_ids(NUM_BIKES)
    final_bike_ids = []
    
    print("Verifying/Creating bikes in backend...", flush=True)
    for bid in initial_ids:
        if check_bike_exists(bid):
            print(f"Bike {bid} already exists.", flush=True)
            final_bike_ids.append(bid)
        else:
            print(f"Bike {bid} not found. Creating...", flush=True)
            new_id = create_bike(bid)
            if new_id:
                final_bike_ids.append(str(new_id))
            else:
                print(f"Skipping {bid} due to creation failure.", flush=True)

    client = mqtt.Client(client_id="SimulationScript_v1")
    client.on_connect = on_connect

    print(f"Connecting to broker at {BROKER}:{PORT}...", flush=True)
    try:
        client.connect(BROKER, PORT, 60)
    except Exception as e:
        print(f"Could not connect to broker: {e}")
        print("Ensure your MQTT broker (e.g., RabbitMQ with MQTT plugin) is running.")
        return

    client.loop_start()

    print(f"Initializing {len(final_bike_ids)} bikes with persistent state...", flush=True)
    bikes = [Bike(bid) for bid in final_bike_ids]
    
    print("Starting simulation. Press Ctrl+C to stop.", flush=True)
    print("Bikes will move and drain battery gradually.", flush=True)

    try:
        while True:
            start_time = time.time()
            count = 0
            for bike in bikes:
                bike.update()
                
                # Payload matching BicycleTelemetryDTO
                payload = {
                    "idBicycle": bike.id,
                    "padlockStatus": bike.padlock_status,
                    "latitude": bike.lat,
                    "longitude": bike.lon, 
                    "battery": round(bike.battery, 2),
                    "timestamp": int(time.time() * 1000)
                }

                topic = TOPIC_TEMPLATE.format(bike.id)
                client.publish(topic, json.dumps(payload))
                count += 1
                
                if count % 100 == 0:
                    time.sleep(0.01)

            duration = time.time() - start_time
            print(f"Published updates for {count} bikes in {duration:.2f} seconds.", flush=True)
            
            time_to_sleep = max(0, 5.0 - duration)
            if time_to_sleep > 0:
                time.sleep(time_to_sleep)

    except KeyboardInterrupt:
        print("\nSimulation stopped by user.")
        client.loop_stop()
        client.disconnect()

if __name__ == "__main__":
    main()

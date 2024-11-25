from datetime import datetime
import time
import requests
from dotenv import load_dotenv
import os

load_dotenv()

BASE_URL = "https://api.artifactsmmo.com/my/"  # Replace with your base URL
API_TOKEN = os.getenv("API_TOKEN")      # Replace with your API token

def send_request(character, path, method, body):
    url = f"{BASE_URL}{character}"
    return send_request_to_url(url, path, method, body)

def send_request_to_url(url, path, method, body):
    headers = {
        "Accept": "application/json",
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_TOKEN}"
    }
    try:
        response = requests.request(
            method=method,
            url=f"{url}{path}",
            headers=headers,
            json=body
        )
        result = response.json()

        if "error" in result:
            print("Failed:", url, path, result["error"])
        if response.status_code != 200:
            print("Unexpected status:", response.status_code, result)
            
        return result
    except Exception as error:
        print("error", error)
        return False

def handle_result_cooldown(result):
    if "data" in result and "cooldown" in result["data"]:
        start = datetime.now()
        time.sleep(result["data"]["cooldown"]["remaining_seconds"] + 1)

    
# Example usage:
# send_request("character_name", "/endpoint", "POST", {"key": "value"})

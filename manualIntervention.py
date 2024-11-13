import requests
import json

url = "http://localhost:3000/action"
headers = {
  'Content-Type': 'application/json'
}


def sendAction(name, action, params):
    payload = json.dumps({
        "characterName": name,
        "action": action,
        **params
    })

    response = requests.request("POST", url, headers=headers, data=payload)

    print(response.text)

sendAction("Bobby", "move", {"x": 1, "y": -2})
sendAction("Bobby", "attack", {"repeat": 100})

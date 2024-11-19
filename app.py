import threading
from flask import Flask
from flask import request

from Character import Character

app = Flask(__name__)

names = ["Bobby", "Stuart", "George", "Tim", "Joe"]
characters = [Character(n) for n in names]
character_map = {}
for name, c in zip(names, characters): character_map[name] = c

for c in characters: c.load_data()
threads = []
for character in characters:
    thread = threading.Thread(target=character.run_agent)
    # thread = threading.Thread(target=character.execute_plan, args=[plan])

    thread.daemon = True  # Allow the program to exit even if threads are running
    threads.append(thread)

# Start all threads
for thread in threads:
    thread.start()


@app.route("/")
def hello_world():
    return "<p>Hello, World!</p>"


@app.route("/submitplan", methods=["POST"])
def submit_plan():
    body = request.json

    if "plan" not in body:
        return {"error": True, "message": "No plan found in body"}
    if "character" not in body:
        return {"error": True, "message": "No character found in body"}
    if body["character"] not in character_map:
        return {"error": True, "message": "Charcater not found"}

    print(f"Setting plan of {body['character']} to {body['plan']}")
    character_map[body["character"]].plan.extend(body["plan"])


    return "added to plan successfully"


@app.route("/setdefault", methods=["POST"])
def set_default():
    body = request.json

    if "action" not in body:
        return {"error": True, "message": "No action found in body"}
    if "character" not in body:
        return {"error": True, "message": "No character found in body"}
    if body["character"] not in character_map:
        return {"error": True, "message": "Charcater not found"}

    print(f"Setting default action of {body['character']} to {body['action']} {'Subaction: ' + str(body['subaction']) if 'subaction' in body else ''} ")
    character_map[body["character"]].default_action = body["action"]
    if 'subaction' in body:
        character_map[body["character"]].default_subaction = body["subaction"]

    return "Set default action successfully"

@app.route("/emptyplan", methods=["POST"])
def empty_plan():
    body = request.json

    if "character" not in body:
        return {"error": True, "message": "No character found in body"}
    if body["character"] not in character_map:
        return {"error": True, "message": "Charcater not found"}

    print(f"Emptying the plan of {body['character']}")
    character_map[body["character"]].plan = []

    return "Emptied plan successfully"
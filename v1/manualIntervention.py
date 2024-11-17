import requests
import json

headers = {
  'Content-Type': 'application/json'
}


def sendAction(name, action, params):
    url = "http://localhost:3000/action"
    payload = json.dumps({
        "characterName": name,
        "action": action,
        **params
    })
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response.text)

def setDefault(name, action, params):
    url = "http://localhost:3000/default"
    payload = json.dumps({
        "characterName": name,
        "action": action,
        **params
    })
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response.text)

# Characters
bobby = "Bobby"
stuart = "Stuart"
george = "George"
tim = "Tim"
joe = "Joe"


# Locations
bank = {"x": 4, "y": 1}
jewelryCrafting = {"x": 1, "y": 3}
gearCrafting = {"x": 3, "y": 1}
weaponCrafting = {"x": 2, "y": 1}
forge = {"x": 1, "y": 5}

green_slime = {"x": 0, "y": -1}
yellow_slime = {"x": 4, "y": -1}
red_slime = {"x": 1, "y": -1}
chicken = {"x": 0, "y": 1}

copper_mine = {"x": 2, "y": 0}


# Codes
copper_ore = "copper_ore"
copper = "copper"
copper_boots = "copper_boots"
copper_helmet = "copper_helmet"
copper_ring = "copper_ring"
copper_dagger = "copper_dagger"
feather = "feather"
copper_armor = "copper_armor"
copper_legs_armor = "copper_legs_armor"




# - crafting armor for all characters except Bobby
# - Joe and Tim needs swords
# - Everyone needs a ring

# Copper - 8 copper ore
# Copper dagger - 6 copper 
# Ring - 6 copper
# Leg armor - 5 copper, 2 feather
# Body armor - 5 copper, 2 feather
# helmet armor - 6 copper
# boot armor - 8 copper
# Sticky Sword - 5 copper, 2 yellow slimeballs

# setDefault("Joe", "autopilot", {})


# sendAction("Joe", "withdraw", {"code": feather, "quantity": 16})

def smelt_copper(name):
    for i in range(10):
        sendAction(name, "move", bank)
        sendAction(name, "deposit all", {})
        sendAction(name, "withdraw", {"code": copper_ore, "quantity": 8 * 12})
        sendAction(name, "move", forge)
        sendAction(name, "craft", {"code": copper, "quantity": 12})


def mine_and_smelt_copper(name):
    copper_to_make = 12
    sendAction(name, "move", bank)
    sendAction(name, "deposit all", {})
    sendAction(name, "copy queue", {})
    sendAction(name, "move", copper_mine)
    sendAction(name, "collect", {"repeat": 8 * copper_to_make})
    sendAction(name, "move", forge)
    sendAction(name, "craft", {"code": copper, "quantity": copper_to_make})
    sendAction(name, "move", bank)
    sendAction(name, "deposit all", {})
    sendAction(name, "paste queue", {})

mine_and_smelt_copper(bobby)
mine_and_smelt_copper(stuart)
mine_and_smelt_copper(george)
mine_and_smelt_copper(tim)
mine_and_smelt_copper(joe)




# smelt_copper(bobby)
# smelt_copper(stuart)
# smelt_copper(george)
# smelt_copper(tim)


# setDefault(joe, "idle", {})
# sendAction(joe, "move", bank)
# sendAction(joe, "deposit all", {})
# sendAction(joe, "withdraw", {"code": copper, "quantity": 6 * 16})
# sendAction(joe, "move", weaponCrafting)
# sendAction(joe, "craft", {"code": copper_dagger, "quantity": 16})
# sendAction(joe, "recyle", {"code": copper_dagger, "quantity": 16})


import math
from actions import *
import encyclopedia as ency
from Character import Character
from util import handle_result_cooldown

BANK_LOCATION_1 = [4, 1]
BANK_LOCATION_2 = [7, 13]

def get_closest_bank(x, y):
    loc_1_dist = math.dist([x,y], BANK_LOCATION_1)
    loc_2_dist = math.dist([x,y], BANK_LOCATION_2)
    if loc_1_dist < loc_2_dist:
        return BANK_LOCATION_1
    else:
        return BANK_LOCATION_2


# Deposit all
# - move to bank
# - deposit all items
def deposit_all_items(character: Character):
    current_map = ency.get_location(character.x, character.y)
    if current_map.empty or (current_map["content_code"] != "bank").bool():
        bank_location = get_closest_bank(character.x, character.y)
        result = move(character.name, bank_location[0], bank_location[1])
        character.x = bank_location[0]
        character.y = bank_location[1]
        handle_result_cooldown(result)

    for slot in character.inventory:
        if slot.quantity > 0:
            print(f"Depositing slot {slot.slot}")
            result = deposit_item(character.name, slot)
            handle_result_cooldown(result)

# Withdraw
# - Move to bank
# - withdraw that item
def withdraw_from_bank(character: Character, item_code: str, quantity: int):
    current_map = ency.get_location(character.x, character.y)
    if current_map.empty or (current_map["content_code"] != "bank").bool():
        bank_location = get_closest_bank(character.x, character.y)
        result = move(character.name, bank_location[0], bank_location[1])
        character.x = bank_location[0]
        character.y = bank_location[1]
        handle_result_cooldown(result)
    
    result = withdraw_item(character.name, item_code, quantity)
    handle_result_cooldown(result)


# Attack specific monster
# - Find monster in the monsters table
# - Move to that location
def attack_monster(character: Character, monster: str):
    location = ency.get_monster_spot(monster)
    if location.empty:
        print("Monster does not exists")
        return

    location = location.iloc[0]
    if (character.x != location["x"]) or (character.y != location["y"]):
        result = move(character.name, int(location["x"]), int(location["y"]))
        handle_result_cooldown(result)

    attack(character.name)


# Collect specific resource
# - FInd that reource in the resources table
# - Move to that resource

# Collect highest level resource unlocked from mining, etc...
def collect_highest_unlocked_resource(character: Character, skill: str):
    lvl = character.get_skill_level(skill)
    locations = ency.get_locations_by_skill(skill)
    locations = locations.loc[locations["level"] <= lvl]
    locations.sort_values(by="level", inplace=True, ascending=False)

    location = locations.iloc[0]
    if (character.x != location["x"]) or (character.y != location["y"]):
        result = move(character.name, int(location["x"]), int(location["y"]))
        handle_result_cooldown(result)
    
    result = collect(character.name)
    handle_result_cooldown(result)

def go_and_collect_item(character: Character, code: str):
    # Find resource location
    locations = ency.get_location_by_resource(code)
    location = locations.iloc[0]
    if (character.x != location["x"]) or (character.y != location["y"]):
        result = move(character.name, int(location["x"]), int(location["y"]))
        character.x = location["x"]
        character.y = location["y"]
        handle_result_cooldown(result)
    
    result = collect(character.name)
    handle_result_cooldown(result)

# Craft specific item
def go_and_craft_item(character: Character, item_code: str, quantity: int):
    # Find where the item is
    item = ency.get_item_by_name(item_code)
    if item is not None:
        if "recipe" in item:
            move_to_location(character, item["recipe"]["skill"])
            result = craft(character.name, item_code, quantity)
            handle_result_cooldown(result)
        else:
            print(f"{item_code} is not craftable")
    else:
        print(f"{item_code} can not be found")
# Equip item
# - ??


def move_to_location(character: Character, location_name: str):
    location = ency.get_location_by_name(location_name)
    if len(location) > 0:
        location = location.iloc[0]
        
    if character.x != location["x"] or character.y != location["y"]:
        result = move(character.name, int(location["x"]), int(location["y"]))
        handle_result_cooldown(result)

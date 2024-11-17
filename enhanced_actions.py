from actions import *
import encyclopedia as ency
from Character import Character
from util import handle_result_cooldown

BANK_LOCATION = (4, 1)

# Deposit all
# - move to bank
# - deposit all items
async def deposit_all_items(character: Character):
    current_map = ency.get_location(character.x, character.y)
    if current_map.empty or (current_map["content_code"] != "bank").bool():
        result = move(character.name, BANK_LOCATION[0], BANK_LOCATION[1])
        await handle_result_cooldown(result)

    for slot in character.inventory:
        if slot.quantity > 0:
            print(f"Depositing slot {slot.slot}")
            result = deposit_item(character.name, slot)
            await handle_result_cooldown(result)

# Withdraw
# - Move to bank
# - withdraw that item


# Attack specific monster
# - Find monster in the monsters table
# - Move to that location
async def attack_monster(character: Character, monster: str):
    location = ency.get_monster_spot(monster)
    if location.empty:
        print("Monster does not exists")
        return

    location = location.iloc[0]
    if (character.x != location["x"]) or (character.y != location["y"]):
        result = move(character.name, int(location["x"]), int(location["y"]))
        await handle_result_cooldown(result)

    attack(character.name)



# Collect specific resource
# - FInd that reource in the resources table
# - Move to that resource

# Collect highest level resource unlocked from mining, etc...
async def collect_highest_unlocked_resource(character: Character, skill: str):
    lvl = character.get_skill_level(skill)
    locations = ency.get_locations_by_skill(skill)
    locations = locations.loc[locations["level"] <= lvl]
    locations.sort_values(by="level", inplace=True, ascending=False)

    location = locations.iloc[0]
    if (character.x != location["x"]) or (character.y != location["y"]):
        result = move(character.name, int(location["x"]), int(location["y"]))
        await handle_result_cooldown(result)
    
    result = collect(character.name)
    await handle_result_cooldown(result)

# Craft specific item
# - Use plan generator to generate a plan for that item

# Equip item
# - ??


async def move_to_location(character: Character, location_name: str):
    location = ency.get_location_by_name(location_name)
    if len(location) > 0:
        location = location.iloc[0]
        
    if character.x != location["x"] or character.y != location["y"]:
        result = move(character.name, int(location["x"]), int(location["y"]))
        await handle_result_cooldown(result) 
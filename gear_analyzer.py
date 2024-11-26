# Get the player
import json
import pandas as pd

from actions import get_bank_items
from encyclopedia import get_item_by_name, get_items_that_match, get_monster_spot
from plan_generator import generate_crafting_plan

# Go through the various gear slots- weapon, boots, helmet, shielf, ring, leg armor, body armor, amulet


slots = ["weapon","shield","helmet","body_armor","leg_armor","boots", "ring", "amulet"]
columns = ["hp","haste","attack_fire","attack_earth","attack_water","attack_air","dmg_fire","dmg_earth","dmg_water","dmg_air","res_fire","res_earth","res_water","res_air"]

def generate_gear_comparisons(max_level):
  for slot in slots:
    def filter_items(item):
        return item["type"] == slot and item["level"] <= max_level
    possible_armor = get_items_that_match(filter_items)
    rows = []
    for armor in possible_armor:
        item = {
            "code": armor["code"]
        }
        for col in columns:
            effect_val = next((effect["value"] for effect in armor["effects"] if effect["name"] == col), 0)
            item[col] = effect_val
        rows.append(item)


    df = pd.DataFrame(rows)
    df.sort_values(by="hp", inplace=True, ascending=False)
    df.to_csv(f"./gear/{slot}.csv")


def find_best_weapon_for_monster(monster_code, max_level, available_in_bank=True, current_weapon = None, current_inventory = None):
    bank_items = get_bank_items()
    def filter_weapons(item):
        if item["code"] == current_weapon:
           return True
        if available_in_bank:
            in_bank = any(bank_item["code"] == item["code"] for bank_item in bank_items)
            in_inventory = False
            if current_inventory is not None:
                in_inventory = any(inv_item.code == item["code"] for inv_item in current_inventory)
            return item["type"] == "weapon" and item["level"] <= max_level and (in_bank or in_inventory)
        else:
            return item["type"] == "weapon" and item["level"] <= max_level
    target_monster_df = get_monster_spot(monster_code)
    if len(target_monster_df) == 0:
       print(f"Failed to find monster: {monster_code}")
       return False
    
    monster = target_monster_df.iloc[0]
    candidate_weapons = get_items_that_match(filter_weapons)

    if len(candidate_weapons) == 0:
       print(f"No candidate weapons for max level {max_level}")
       return False

    best_weapon = None
    best_dmg = 0
    for weapon in candidate_weapons:
        damage_fire = next((effect["value"] for effect in weapon["effects"] if effect["name"] == "attack_fire"), 0) * (1 - int(monster["res_fire"])/100)
        damage_earth = next((effect["value"] for effect in weapon["effects"] if effect["name"] == "attack_earth"), 0) * (1 - int(monster["res_earth"])/100)
        damage_water = next((effect["value"] for effect in weapon["effects"] if effect["name"] == "attack_water"), 0) * (1 - int(monster["res_water"])/100)
        damage_air = next((effect["value"] for effect in weapon["effects"] if effect["name"] == "attack_air"), 0) * (1 - int(monster["res_air"])/100)
        total_dmg = damage_fire + damage_earth + damage_water + damage_air
        if total_dmg > best_dmg:
           best_dmg = total_dmg
           best_weapon = weapon

    return best_weapon


def find_best_armor_for_monster(monster_code, armor_slot, max_level, available_in_bank=True, current_armor = None, current_inventory = None, weapon_code = None):
    bank_items = get_bank_items()
    
    
    
    def filter_armors(item):
        if item["code"] == current_armor:
           return True
        if available_in_bank:
            in_bank = any(bank_item["code"] == item["code"] for bank_item in bank_items)
            in_inventory = False
            if current_inventory is not None:
                in_inventory = any(inv_item.code == item["code"] for inv_item in current_inventory)
            return item["type"] == armor_slot and item["level"] <= max_level and (in_bank or in_inventory)
        else:
            return item["type"] == armor_slot and item["level"] <= max_level
    
    target_monster_df = get_monster_spot(monster_code)
    if len(target_monster_df) == 0:
       print(f"Failed to find monster: {monster_code}")
       return False
    
    monster = target_monster_df.iloc[0]
    candidate_armors = get_items_that_match(filter_armors)
    if len(candidate_armors) == 0:
        print(f"Failed to get armor candidates for {monster_code} {armor_slot} {max_level} {current_armor}")
        return False

    def get_effect(effect_name, item):
        return next((effect["value"] for effect in item["effects"] if effect["name"] == effect_name), 0)
    def get_as_percent(num):
        return num / 100
    


    player_fire_dmg = 1
    player_earth_dmg = 1
    player_water_dmg = 1
    player_air_dmg = 1
    if weapon_code != None:
        weapon_item = get_item_by_name(weapon_code)
        player_fire_dmg = get_effect("attack_fire", weapon_item)
        player_earth_dmg = get_effect("attack_earth", weapon_item)
        player_water_dmg = get_effect("attack_water", weapon_item)
        player_air_dmg = get_effect("attack_air", weapon_item)

    best_armor = None
    best_effects = 0
    for armor in candidate_armors:
        damage_fire = get_as_percent(get_effect("dmg_fire", armor)) * player_fire_dmg
        fire_damage_done = damage_fire * (1 - int(monster["res_fire"])/100)
        resistance_fire = get_as_percent(get_effect("res_fire", armor)) * monster["attack_fire"]

        damage_earth = get_as_percent(get_effect("dmg_earth", armor)) * player_earth_dmg
        earth_damage_done = damage_earth * (1 - int(monster["res_earth"])/100)
        resistance_earth = get_as_percent(get_effect("res_earth", armor)) * monster["attack_earth"]

        damage_water = get_as_percent(get_effect("dmg_water", armor)) * player_water_dmg
        water_damage_done = damage_water * (1 - int(monster["res_water"])/100)
        resistance_water = get_as_percent(get_effect("res_water", armor)) * monster["attack_water"]

        damage_air = get_as_percent(get_effect("dmg_air", armor)) * player_air_dmg
        air_damage_done = damage_air * (1 - int(monster["res_air"])/100)
        resistance_air = get_as_percent(get_effect("res_air", armor)) * monster["attack_air"]
        
        total_dmg = fire_damage_done + earth_damage_done + water_damage_done + air_damage_done
        total_res = resistance_fire + resistance_earth + resistance_water + resistance_air
        # print(f"Against {monster_code} the {armor['code']} will add {total_dmg} damage and {total_res} resistance")
        if total_dmg + total_res > best_effects:
           best_effects = total_dmg + total_res
           best_armor = armor

    return best_armor

def gather_craftable_weapons_for_level(level):
    def filter_items(item):
        return item["type"] == "weapon" and item["level"] == level
    weapons = get_items_that_match(filter_items)
    for item in weapons:
        print(item["code"])
        plan = generate_crafting_plan(item["code"], 5)
        for step in plan:
            print(json.dumps(step), ",")
   
def get_potion_needed_for_monster(character_level, monster_code):
    '''
    If the level of the monster is < 15, return None
    
    get all potions that are type "utility" and level <= character level
    Calculate the 
    
    potion's effects are either:
    - boost_dmg_earth
    - boost_dmg_air
    - boost_dmg_fire
    - boost_dmg_water
    - restore (this heals character health)
    Choose the potion that is 
    '''
    pass


if __name__ == "__main__":
    # gather_craftable_weapons_for_level(10)
    GEAR_SLOTS = ["shield","helmet","body_armor","leg_armor","boots","ring", "amulet"]
    print("=====Optimal=====")
    for gear in GEAR_SLOTS:
        result = find_best_armor_for_monster("ogre", gear, 20, False, weapon_code="skull_staff")
        print(f"Slot: {gear}. Result: {result['code']}")


    print("=====Available in Bank=====")
    for gear in GEAR_SLOTS:
        result = find_best_armor_for_monster("ogre", gear, 20, True, weapon_code="skull_staff")
        print(f"Slot: {gear}. Result: {result['code']}")

    # result = find_best_armor_for_monster("ogre", "helmet", 20, False, weapon_code="skull_staff")
    
# Get the player
import pandas as pd

from actions import get_bank_items
from encyclopedia import get_items_that_match, get_monster_spot

# Go through the various gear slots- weapon, boots, helmet, shielf, ring, leg armor, body armor, amulet


slots = ["weapon","shield","helmet","body_armor","leg_armor","boots", "ring", "amulet"]
columns = ["hp","haste","attack_fire","attack_earth","attack_water","attack_air","dmg_fire","dmg_earth","dmg_water","dmg_air","res_fire","res_earth","res_water","res_air"]

def generate_gear_comparisons(max_level):
  def filter_items(item):
    return item["type"] == slot and item["level"] <= max_level
  for slot in slots:
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
            in_inventory = True
            if current_inventory is not None:
                in_inventory = any(inv_item["code"] == item["code"] for inv_item in current_inventory)
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
    #    print(f"No candidate weapons for max level {max_level}")
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
        # print(f"Against {monster_code}, {weapon['code']} will do {total_dmg} dmg")

    # print(f"Best weapon: {best_weapon} for {best_dmg} dmg")
    return best_weapon
    # Check the resistances that the monster has
    # choose a weapon that maximizes damage

    # Check the damages that the monster does
    # Choose the armor that takes the least damage

if __name__ == "__main__":
    weapon = find_best_weapon_for_monster("flying_serpent", 15, available_in_bank=False, current_weapon="iron_sword")
    print(weapon)
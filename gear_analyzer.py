# Get the player
from Character import Character
import json
import pandas as pd


# Go through the various gear slots- weapon, boots, helmet, shielf, ring, leg armor, body armor, amulet
items_file = open("./encyclopedia/items/all_items.json")
items = json.loads(items_file.read())

monsters_file = open("./encyclopedia/monsters/monsters_1.json")
monsters = json.loads(monsters_file.read())

slots = ["weapon","shield","helmet","body_armor","leg_armor","boots", "ring", "amulet"]
columns = ["hp","haste","attack_fire","attack_earth","attack_water","attack_air","dmg_fire","dmg_earth","dmg_water","dmg_air","res_fire","res_earth","res_water","res_air"]

def generate_gear_comparisons(max_level):
  for slot in slots:
    possible_armor = [item for item in items.values() if item["type"] == slot and item["level"] <= max_level]
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


def find_best_gear_for_monster(monster_code, max_level):
    monster = 
    # Check the resistances that the monster has
    # choose a weapon that maximizes damage

    # Check the damages that the monster does
    # Choose the armor that takes the least damage
    return
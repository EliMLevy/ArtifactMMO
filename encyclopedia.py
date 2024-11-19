import pandas as pd
import json

monsters = pd.read_csv("./encyclopedia/monsters.csv")
resources = pd.read_csv("./encyclopedia/resources.csv")

items_files = open("./encyclopedia/items/all_items.json")
items = json.loads(items_files.read())

all_maps = pd.read_csv("./encyclopedia/all_maps.csv")

def get_location(x, y):
    return all_maps.loc[(all_maps["x"] == x) & (all_maps["y"] == y)]

def get_monster_spot(monster: str):
    return monsters.loc[monsters["map_code"] == monster]

def get_location_by_name(code):
    return all_maps.loc[all_maps["content_code"] == code]

def get_locations_by_skill(skill):
    return resources.loc[resources["skill"] == skill]

def get_items_that_match(condition):
    return [item for item in items.values() if condition(item)]


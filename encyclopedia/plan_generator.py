import pandas as pd
import json

# Given an item, create a plan for aquiring it
# - Look the item up in the items
# - if it isnt craftable, look it up in the resources and monsters
#   - if found, go there and collect
# - if it is craftable, repeat for ingredients

resources = pd.read_csv("resources.csv")
monsters = pd.read_csv("monsters.csv")


items_file = open("./items/all_items.json")
items = json.loads(items_file.read())

maps_file = open("./maps/all_maps.json")
maps = json.loads(maps_file.read())


def check_monsters_or_resources(df, item_code):
    locations = df[df["resource_code"] == item_code]
    if len(locations) > 0:
        locations = locations.sort_values(by=['drop_chance'], ascending=True)
        target_location = locations.iloc[0]
        return (target_location['x'], target_location['y'])
    else:
        return None
            
def gather_craftable_item(target_item, quantity):
    plan = []
    for ingredient in target_item["recipe"]["items"]:
        plan.extend(create_plan(ingredient["code"], quantity * ingredient["quantity"]))
    skill_needed = target_item["recipe"]["skill"]
    if skill_needed in maps:
        plan.extend([f"Go to ({maps[skill_needed][0]['x']},{maps[skill_needed][0]['y']}) for {skill_needed}", f"craft {target_item['code']} x{quantity}"])
    else:
        plan.append(f"Cannot find where to use {skill_needed}")
    return plan

def collect_resource(item_code, quantity):
    # Check resources
    resource_location = check_monsters_or_resources(resources, item_code)
    if resource_location != None:
        return [f"Go to {item_code} at ({resource_location[0]}, {resource_location[1]})", f"Collect x{quantity}"]
    # Check monsters
    monster_location = check_monsters_or_resources(monsters, item_code)
    if monster_location != None:
        return [f"Go to {item_code} at ({monster_location[0]}, {monster_location[1]})", f"Attack x{quantity}"]
    else:
        return [f"Could not find where to get {item_code}"]

def create_plan(item_code, quantity) -> list[str]:
    if item_code in items:
        plan = []
        target_item = items[item_code]
        if "recipe" in target_item:
            plan.extend(gather_craftable_item(target_item, quantity))
        else:
            plan.extend(collect_resource(item_code, quantity))
        return plan
    else:
        return [f"Can not attain {item_code}"]
    


print(create_plan("adventurer_vest", 1))
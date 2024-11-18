import pandas as pd
import json

from actions import get_bank_items

# Given an item, create a plan for aquiring it
# - Look the item up in the items
# - if it isnt craftable, look it up in the resources and monsters
#   - if found, go there and collect
# - if it is craftable, repeat for ingredients

resources = pd.read_csv("./encyclopedia/resources.csv")
monsters = pd.read_csv("./encyclopedia/monsters.csv")


items_file = open("./encyclopedia/items/all_items.json")
items = json.loads(items_file.read())

maps_file = open("./encyclopedia/maps/all_maps.json")
maps = json.loads(maps_file.read())

bank_items = get_bank_items()

def is_item_in_bank(item_code):
    item = next((item for item in bank_items if item["code"] == item_code), None)
    return item is not None

def get_quantity_of_item_in_bank(item_code):   
    return next((item["quantity"] for item in bank_items if item["code"] == item_code), 0)
    
'''
input -> [{withdraw, 160, iron_ore}, {craft, 20, iron}]
output -> [{withdraw, 100, iron}, {craft, 12, iron}, {withdraw, 60, iron_ore}, {craft, 8, iron}]
'''
def review_plan(plan):
    # Step through plan and keep track of inventory.
    # withdraw, collect, attack increase space used.
    # Craft -> get conversion rate
    # deposit all -> inventory space used = 0
    
    # If we need to withdraw, collect or attack too many items then we need to split 
    pass

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
        plan.extend([{"action": "move", "x": maps[skill_needed][0]['x'], "y": maps[skill_needed][0]['y']},
                     {"action": "craft", "code": target_item['code'], "quantity": quantity }])
    else:
        plan.append({"action": "error", "message": f"Cannot find {skill_needed}"})
    return plan

def collect_resource(item_code, quantity):
    # Check resources
    resource_location = check_monsters_or_resources(resources, item_code)
    if resource_location != None:
        return [{"action": "move", "x": resource_location[0], "y": resource_location[1]},
            {"action": "collect", "repeat": quantity}]
    # Check monsters
    monster_location = check_monsters_or_resources(monsters, item_code)
    if monster_location != None:
        return [
            {"action": "move", "x": monster_location[0], "y": monster_location[1]},
            {"action": "collect", "repeat": quantity}]
    else:
        return [{"action": "error", "message": f"cannot find {item_code}"}]

def create_plan(item_code, quantity) -> list[str]:
    if get_quantity_of_item_in_bank(item_code) >= quantity:
        return [{"action": "withdraw", "code": item_code, "quantity": quantity}]
        

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
    

plan = [{"action": "deposit all"}]
plan.extend(create_plan("iron_ring", 10))
plan.append({"action": "deposit all"})

for item in plan:
    print(item, ",")
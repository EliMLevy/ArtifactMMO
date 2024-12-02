'''
Given a crafting level 
Find all the gear that can be crafted with accessible ingredients
Sorted in the order of ease of crafting (by monster difficulty)

Algo:
- Get all gear craftable at that level
- For each item, compile a list of all ingredients (down to atomic ingredients)
- Sort all the gear based on the highest leveled monster that is required to kill

'''

from collections import defaultdict
import json
from encyclopedia import get_item_by_name, get_items_that_match, get_location_by_monster_drop

def ingredient_collector(item, ingredients: defaultdict, multiplier):
    # Simple DFS. 
    # For each ingredient, if that ingredient has no recipe, add the quantity
    # If it does have a recipe, recurse
    if "recipe" in item:
        for ingredient in item["recipe"]["items"]:
            ingredient_info = get_item_by_name(ingredient["code"])
            if ingredient_info is not None and "recipe" in ingredient_info:
                ingredient_collector(ingredient_info, ingredients, multiplier * ingredient["quantity"])
            else:
                ingredients[ingredient["code"]] += ingredient["quantity"] * multiplier
    else:
        raise Exception(f"{item} is not craftable!")

    return

def compile_gear_crafting_list(crafting_level):
    # Get all gear craftable at that level
    gear_types = ['weapon', 'shield', 'helmet', 'body_armor', 'leg_armor', 'boots', 'amulet', 'ring']
    def item_of_level(item):
        return item["type"] in gear_types and item["level"] == crafting_level
    gear = get_items_that_match(item_of_level)

    result = [] # list of objects containing: item, ingredients, difficulty

    for item in gear:
        if "recipe" not in item:
            print(f"{item['code']} is not craftable")
            continue
        # compile a list of all ingredients (down to atomic ingredients)
        ingredients = defaultdict(int) # ingredient_code -> quantity_needed
        ingredient_collector(item, ingredients, 1)
        # Go through ingredients and find the most difficult monster drop to obtain
        difficulty = 0
        for ingredient in ingredients.keys():
            location_df = get_location_by_monster_drop(ingredient)
            if len(location_df) > 0: # if it is a resource, this will be False
                level = int(location_df.iloc[0]["level"])
                difficulty = max(difficulty, level)
        result.append({
            "item": item,
            "ingredients": ingredients,
            "difficulty": difficulty
        })


    sorted_results = sorted(result, key=lambda item: item["difficulty"])

    return sorted_results


if __name__ == '__main__':

    gear_list = compile_gear_crafting_list(20)
    all_ingredients = defaultdict(int) # ingredient_code -> quantity_needed

    for item in gear_list:
        print(f"{item['item']['code']} -> {item['difficulty']}")
        print(json.dumps(item["ingredients"]))
        for i in item["ingredients"].keys():
            all_ingredients[i] += item["ingredients"][i]



    # steel_battleaxe = get_item_by_name("steel_battleaxe")
    # ingredients = defaultdict(int) # ingredient_code -> quantity_needed
    # ingredient_collector(steel_battleaxe, ingredients, 193)
    
    # for ingredient in ingredients.keys():
    #     print(f"{ingredient} x{ingredients[ingredient]}")

    # ingredients = defaultdict(int)
    # ingredient_collector(piggy_pants, ingredients, 1)
    # print(ingredients)
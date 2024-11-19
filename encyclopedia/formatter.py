import json
import pandas as pd

# Resources table
# resource code, x, y, drop, drop_chance, map_name

'''
# Items dictionary
{
    item code: {
        level: number,
        craft: {
            skill: string,
            level: number,
            items: [
                {
                    code: string,
                    quantity: number
                }
            ]
        }
    }
}

# Workshop table
code, x, y

'''


'''
Scenario:
Make an iron sword
- Look up iron sword in items dictionary
- Check the character level required.
- TODO - figure out what to do if we arent leveled up enough
- Check the crafting skill needed. 
- TODO - WIP: If we arent leveled up enough in weaponcrafting, find an item that ...
- For each item in the crafting recipe
    - Look up that item in the items dictionary
    - If it needs to be crafted -> recursion
    - If it isnt crafted, look it up in the resources table
        - Sort the results by drop chance and then by closest distance
        - Collect that many of that item
- If all ingrediants are satisfied, go to workshop based on skill

'''


def map_combination():
    def extract_map(input):
        return {
            "x" : input["x"],
            "y" : input["y"],
            "content" : input["content"]
        }

    # Combine maps
    all_maps = {}
    for i in range(1, 5):
        current_map = open(f"./maps/maps_{i}.json")
        parsed = json.loads(current_map.read())
        interesting_maps = [m for m in parsed["data"] if m["content"] != None]
        cleaned_maps = map(extract_map, interesting_maps)
        for m in cleaned_maps:
            if m["content"]["code"] in all_maps:
                all_maps[m["content"]["code"]].append(m)
            else:
                all_maps[m["content"]["code"]] = [m]
                

    output = open(f"./maps/all_maps.json", "w+")
    output.write(json.dumps(all_maps))



def combine_maps_and_resources():
    resource_df = pd.DataFrame(columns=["resource_code", "x", "y", "drop_chance", "map_code", "level", "skill"])
    resources_file = open("./resources.json")
    resources = json.loads(resources_file.read()) 


    maps_file = open("./maps/all_maps.json")
    maps = json.loads(maps_file.read())

    skipped = []
    for resource in resources["data"]:
        # Find the locations of this resource in the maps
        if resource["code"] in maps:
            locations = maps[resource["code"]]

            # For each drop, add row to the df
            for drop in resource["drops"]:
                for location in locations:
                    resource_df.loc[len(resource_df)] = [drop["code"], location["x"], location["y"], drop["rate"], location["content"]["code"], resource["level"], resource["skill"]]
        else:
            skipped.append(resource["code"])

    resource_df.to_csv("./resources.csv", index=False)
    print(skipped)


def combine_items():
    all_items = {}
    for i in range(1, 5):
        item_file = open(f"./items/Items_{i}.json")
        items = json.loads(item_file.read())

        for item in items["data"]:
            all_items[item["code"]] = {
                "code": item["code"],
                "level": item["level"],
                "type": item["type"],
                "effects": item["effects"],
            }
            if item["craft"] != None:
                all_items[item["code"]]["recipe"] = {
                    "skill": item["craft"]["skill"],
                    "level": item["craft"]["level"],
                    "items": item["craft"]["items"],
                }

    output_file = open("./items/all_items.json", "w+")
    output_file.write(json.dumps(all_items))

def combine_maps_and_monsters():
    monster_file = open("./monsters/monsters_1.json")
    monsters = json.loads(monster_file.read())

    monster_df = pd.DataFrame(columns=["resource_code", "x", "y", "drop_chance", "map_code", "attack_fire","attack_earth","attack_water","attack_air","res_fire","res_earth","res_water","res_air"])
    for monster in monsters["data"]:
        maps_file = open("./maps/all_maps.json")
        maps = json.loads(maps_file.read())

        skipped = []
        # Find the locations of this resource in the maps
        if monster["code"] in maps:
            locations = maps[monster["code"]]

            # For each drop, add row to the df
            for drop in monster["drops"]:
                for location in locations:
                    monster_df.loc[len(monster_df)] = [drop["code"], location["x"], location["y"], drop["rate"], location["content"]["code"],
                                                      monster["attack_fire"],monster["attack_earth"],monster["attack_water"],monster["attack_air"],monster["res_fire"],monster["res_earth"],monster["res_water"],monster["res_air"] ]
        else:
            skipped.append(monster["code"])



    monster_df.to_csv("./monsters.csv", index=False)
    print(skipped)


def convert_json_to_csv(input_file, output_file):
    # Load the JSON data
    with open(input_file, 'r') as f:
        data = json.load(f)
    
    # Prepare a list to store rows for the DataFrame
    rows = []
    
    # Iterate over the JSON data and extract relevant fields
    for content_code, items in data.items():
        for item in items:
            row = {
                "x": item["x"],
                "y": item["y"],
                "content_type": item["content"]["type"],
                "content_code": content_code
            }
            rows.append(row)
    
    # Create a DataFrame
    df = pd.DataFrame(rows)
    
    # Write to CSV
    df.to_csv(output_file, index=False)

# convert_json_to_csv("./maps/all_maps.json", "all_maps.csv")

if __name__ == "__main__":
    combine_maps_and_monsters()
import json
import time
import pandas as pd
import requests

# Convert all_maps.json -> all_interesting_maps.json
def map_combination():
    def extract_map(input):
        return {
            "x" : input["x"],
            "y" : input["y"],
            "content" : input["content"]
        }

    # Combine maps
    all_maps = {}
    unfiltered_maps = open(f"./maps/all_maps.json")
    parsed = json.loads(unfiltered_maps.read())
    unfiltered_maps.close()
    interesting_maps = [m for m in parsed if m["content"] != None]
    cleaned_maps = map(extract_map, interesting_maps)
    for m in cleaned_maps:
        if m["content"]["code"] in all_maps:
            all_maps[m["content"]["code"]].append(m)
        else:
            all_maps[m["content"]["code"]] = [m]
                

    output = open(f"./maps/all_interesting_maps.json", "w+")
    output.write(json.dumps(all_maps))
    output.close()


# all_resources.json + all_interesting_maps.json -> resources.csv
def combine_maps_and_resources():
    resource_df = pd.DataFrame(columns=["resource_code", "x", "y", "drop_chance", "map_code", "level", "skill"])
    resources_file = open("./resources/all_resources.json", "r")
    resources = json.loads(resources_file.read()) 
    resources_file.close()


    maps_file = open("./maps/all_interesting_maps.json")
    maps = json.loads(maps_file.read())
    maps_file.close()

    skipped = []
    for resource in resources:
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

# all_monsters + all_interesting_maps.json -> monsters.csv
def combine_maps_and_monsters():
    monster_file = open("./monsters/all_monsters.json")
    monsters = json.loads(monster_file.read())
    monster_file.close()

    monster_df = pd.DataFrame(columns=["level","resource_code", "x", "y", "drop_chance", "map_code", "hp", "attack_fire","attack_earth","attack_water","attack_air","res_fire","res_earth","res_water","res_air"])
    maps_file = open("./maps/all_interesting_maps.json")
    maps = json.loads(maps_file.read())
    maps_file.close()
    skipped = []
    for monster in monsters:
        # Find the locations of this resource in the maps
        if monster["code"] in maps:
            locations = maps[monster["code"]]

            # For each drop, add row to the df
            for drop in monster["drops"]:
                for location in locations:
                    monster_df.loc[len(monster_df)] = [monster["level"], drop["code"], location["x"], location["y"], drop["rate"], location["content"]["code"],
                                                      monster["hp"], monster["attack_fire"],monster["attack_earth"],monster["attack_water"],monster["attack_air"],monster["res_fire"],monster["res_earth"],monster["res_water"],monster["res_air"] ]
        else:
            skipped.append(monster["code"])



    monster_df.to_csv("./monsters.csv", index=False)
    print(skipped)


# all_interesting_maps.json -> all_maps.csv
def convert_maps_from_json_to_csv():
    # Load the JSON data
    with open("./maps/all_interesting_maps.json", 'r') as f:
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
    df.to_csv("./all_maps.csv", index=False)


def fetch_all_pages_and_save(url, output):
    payload = {}
    headers = {
        'Accept': 'application/json'
    }
    page = 1
    pages = 2
    all_results = []
    iters = 0
    while page <= pages and iters < 10:
        print(url + "&page=" + str(page))
        print(f"page: {page}. Results so far: {len(all_results)}")
        response = requests.request("GET", url + "&page=" + str(page), headers=headers, data=payload)
        data = response.json()
        page = data["page"] + 1
        pages = data["pages"]

        data_payload = data["data"]
        all_results.extend(data_payload)
        iters += 1
        time.sleep(3)

    output_file = open(output, "w+")
    output_file.write(json.dumps(all_results))
    output_file.close()



def load_maps_items_monsters_resources():
    fetch_all_pages_and_save("https://api.artifactsmmo.com/maps?size=100", './maps/all_maps.json')
    fetch_all_pages_and_save("https://api.artifactsmmo.com/items?size=100", './items/all_items.json')
    fetch_all_pages_and_save("https://api.artifactsmmo.com/monsters?size=100", './monsters/all_monsters.json')
    fetch_all_pages_and_save("https://api.artifactsmmo.com/resources?size=100", './resources/all_resources.json')


def convert_items_to_dict():
    data_file = open("./items/all_items.json")
    data = json.loads(data_file.read())
    data_file.close()

    output = {}
    for item in data:
        output[item["code"]] = item

    output_file = open("./all_items.json", "w+")
    output_file.write(json.dumps(output))
    output_file.close()

if __name__ == "__main__":
    # load_maps_items_monsters_resources()
    # map_combination()
    # convert_maps_from_json_to_csv()
    # combine_maps_and_monsters()
    # combine_maps_and_resources()
    convert_items_to_dict()
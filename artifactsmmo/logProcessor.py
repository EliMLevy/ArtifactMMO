from collections import defaultdict
import json

def analyze_log(file_path):
    # Nested defaultdict to track data
    character_fights = defaultdict(lambda: defaultdict(int))  # Monster counts for ATTACK
    character_collections = defaultdict(lambda: defaultdict(int))  # Resource counts for COLLECT
    character_crafts = defaultdict(lambda: defaultdict(int))  # Crafted items for CRAFT
    
    try:
        with open(file_path, 'r') as file:
            for line in file:
                # Split the line into parts
                parts = line.strip().split('|')
                
                # Ensure the line has the correct number of parts (6)
                if len(parts) != 6:
                    continue  # Skip malformed lines
                
                # Extract relevant fields
                timestamp, action, character_name, request_body, response_body, outcome = parts
                
                if action == "ATTACK":
                    # Handle ATTACK action (monster name in request_body)
                    target = request_body.strip()
                    if target:
                        character_fights[character_name][target] += 1
                
                elif action == "COLLECT":
                    # Handle COLLECT action (resource name in request_body)
                    resource = request_body.strip()
                    if resource:
                        character_collections[character_name][resource] += 1

                elif action == "CRAFT":
                    # Handle CRAFT action (crafted item name in request_body)
                    item = json.loads(request_body.strip())
                    if item:
                        character_crafts[character_name][item['code']] += item['quantity']
        
        # Print the results
        print("Character Activity Summary:")
        for character in set(character_fights) | set(character_collections) | set(character_crafts):
            print(f"Character: {character}")
            
            # Print monsters fought
            if character in character_fights:
                print("  Monsters:")
                for monster, count in character_fights[character].items():
                    print(f"    {monster}: {count}")
            
            # Print resources collected
            if character in character_collections:
                print("  Resources:")
                for resource, count in character_collections[character].items():
                    print(f"    {resource}: {count}")
            
            # Print crafted items
            if character in character_crafts:
                print("  Crafted Items:")
                for item, count in character_crafts[character].items():
                    print(f"    {item}: {count}")
            
            print()

    except FileNotFoundError:
        print(f"Error: File not found at {file_path}. Please check the path and try again.")
    except Exception as e:
        print(f"An error occurred: {e}")

# Call the function with your log file name
log_file_path = 'events.log'
analyze_log(log_file_path)

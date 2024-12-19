from collections import defaultdict
import json

def analyze_log(file_path):
    # Nested defaultdict to track data
    character_fights = defaultdict(lambda: defaultdict(int))  # Monster counts for ATTACK
    character_collections = defaultdict(lambda: defaultdict(int))  # Resource counts for COLLECT
    character_crafts = defaultdict(lambda: defaultdict(int))  # Crafted items for CRAFT
    net_items = defaultdict(int)  # Net items for DEPOSIT/WITHDRAW
    
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
                
                # Skip processing if request_body is empty or contains '||'
                if request_body.strip() == "||":
                    request_body = ""
                
                # Handle ATTACK action (monster name in request_body)
                if action == "ATTACK":
                    target = request_body.strip()
                    if target:
                        character_fights[character_name][target] += 1
                
                # Handle COLLECT action (resource name in request_body)
                elif action == "COLLECT":
                    resource = request_body.strip()
                    if resource:
                        character_collections[character_name][resource] += 1

                # Handle CRAFT action (crafted item name in request_body)
                elif action == "CRAFT":
                    try:
                        if request_body.strip():
                            item = json.loads(request_body.strip())
                            if item:
                                character_crafts[character_name][item['code']] += item['quantity']
                    except json.JSONDecodeError:
                        print(f"Error decoding JSON in CRAFT action: {request_body}")
                
                # Handle DEPOSIT action (single item code and quantity in request_body)
                elif action == "DEPOSIT":
                    try:
                        if request_body.strip():
                            item = json.loads(request_body.strip())  # Parse as a single item
                            code = item['code']
                            quantity = item['quantity']
                            net_items[code] += quantity  # Subtract from net items
                    except json.JSONDecodeError:
                        print(f"Error decoding JSON in DEPOSIT action: {request_body}")

                # Handle WITHDRAW action (single item code and quantity in request_body)
                elif action == "WITHDRAW":
                    try:
                        if request_body.strip():
                            item = json.loads(request_body.strip())  # Parse as a single item
                            code = item['code']
                            quantity = item['quantity']
                            net_items[code] -= quantity  # Add to net items
                    except json.JSONDecodeError:
                        print(f"Error decoding JSON in WITHDRAW action: {request_body}")
        
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
        
        # Print the net items deposited/withdrawn globally
        print("Net Items Deposited/Withdrawn:")
        for item, net_quantity in net_items.items():
            if net_quantity != 0:
                print(f"  {item}: {net_quantity}")
    
    except FileNotFoundError:
        print(f"Error: File not found at {file_path}. Please check the path and try again.")
    except Exception as e:
        print(f"An error occurred: {e}")

# Call the function with your log file name
log_file_path = 'events.log'
analyze_log(log_file_path)

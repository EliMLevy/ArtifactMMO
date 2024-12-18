from collections import defaultdict

def count_monster_fights(file_path):
    # Nested defaultdict to track monster fights by character
    character_fights = defaultdict(lambda: defaultdict(int))
    
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
                
                # Process only ATTACK actions
                if action == "ATTACK":
                    target = request_body.strip()  # The monster's name is in the request body
                    if target:
                        # Increment the count for the monster
                        character_fights[character_name][target] += 1
        
        # Print the results
        for character, monsters in character_fights.items():
            print(f"Character: {character}")
            for monster, count in monsters.items():
                print(f"  {monster}: {count}")
            print()

    except FileNotFoundError:
        print(f"Error: File not found at {file_path}. Please check the path and try again.")
    except Exception as e:
        print(f"An error occurred: {e}")

# Call the function with your log file name
log_file_path = 'events.log'
count_monster_fights(log_file_path)

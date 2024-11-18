import threading
from Character import Character

def main():
    names = ["Bobby", "Stuart", "George", "Tim", "Joe"]
    characters = [Character(n) for n in names]
    for c in characters: c.load_data()

    # craft_armor(characters[4])
    # for c in characters: c.equip_new_gear("shield", "slime_shield")
    start_threads(characters)



def start_threads(characters):
    # Create threads for each character's task loop
    threads = []
    for character in characters:
        thread = threading.Thread(target=character.complete_monster_tasks_loop)

        thread.daemon = True  # Allow the program to exit even if threads are running
        threads.append(thread)

    # Start all threads
    for thread in threads:
        thread.start()

    # Wait for all threads to complete (they won't in this case since they're infinite loops)
    try:
        while True:
            for thread in threads:
                thread.join(timeout=1.0)  # Check each thread every second
    except KeyboardInterrupt:
        print("\nShutting down gracefully...")


def craft_armor(joe: Character):
    plan = [
        {'action': 'withdraw', 'code': 'spruce_wood', 'quantity': 80} ,
        {'action': 'move', 'x': -2, 'y': -3} ,
        {'action': 'craft', 'code': 'spruce_plank', 'quantity': 10} ,
        {'action': 'withdraw', 'code': 'spruce_wood', 'quantity': 80} ,
        {'action': 'move', 'x': -2, 'y': -3} ,
        {'action': 'craft', 'code': 'spruce_plank', 'quantity': 10} ,
        {'action': 'withdraw', 'code': 'spruce_wood', 'quantity': 80} ,
        {'action': 'move', 'x': -2, 'y': -3} ,
        {'action': 'craft', 'code': 'spruce_plank', 'quantity': 10} ,
        {'action': 'withdraw', 'code': 'red_slimeball', 'quantity': 15} ,
        {'action': 'withdraw', 'code': 'yellow_slimeball', 'quantity': 15} ,
        {'action': 'withdraw', 'code': 'green_slimeball', 'quantity': 15} ,
        {'action': 'withdraw', 'code': 'blue_slimeball', 'quantity': 15} ,
        {'action': 'move', 'x': 3, 'y': 1} ,
        {'action': 'craft', 'code': 'slime_shield', 'quantity': 5} ,
    ]

    joe.execute_plan(plan)

if __name__ == "__main__":
    main()

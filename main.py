import threading
from Character import Character

def main():
    names = ["Bobby", "Stuart", "George", "Tim", "Joe"]
    characters = [Character(n) for n in names]
    for c in characters: c.load_data()


    # Create threads for each character's task loop
    threads = []
    for character in characters:
        # thread = threading.Thread(target=character.execute_plan, args=[plan])
        thread = threading.Thread(target=character.complete_resource_collect_loop)
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

if __name__ == "__main__":
    main()

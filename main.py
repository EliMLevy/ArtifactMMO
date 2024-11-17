import asyncio
from Character import Character
from enhanced_actions import *



async def main():
    names = ["Bobby", "Stuart", "George", "Tim", "Joe"]
    characters = [Character(n) for n in names]
    for c in characters: c.load_data()

    # Create tasks for each character's task loop
    tasks = [asyncio.create_task(character.complete_resource_collect_loop()) for character in characters]

    # Run all tasks concurrently
    await asyncio.gather(*tasks)
    

if __name__ == "__main__":
    asyncio.run(main())



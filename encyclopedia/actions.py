import json
import time
from typing import Any

# Import your utility functions
from util import send_request, send_request_to_url, delay

async def move(character: str, x: int, y: int) -> Any:
    raw = json.dumps({"x": x, "y": y})
    result = await send_request(character, "/action/move", "POST", raw)
    if result and result.get("error") and result["error"].get("code") == 490:
        print("Already here!")
        result["data"] = {
            "cooldown": {
                "remaining_seconds": 0,
            },
        }
    return result

async def move_with_cooldown(character: str, x: int, y: int) -> Any:
    move_result = await move(character, x, y)
    if move_result:
        remaining_seconds = move_result["data"]["cooldown"]["remaining_seconds"]
        print("Waiting for", remaining_seconds)
        await delay(remaining_seconds * 1000)
        return move_result

async def attack(character: str) -> Any:
    return await send_request(character, "/action/fight", "POST")

async def rest(character: str) -> Any:
    return await send_request(character, "/action/rest", "POST")

async def collect(character: str) -> Any:
    return await send_request(character, "/action/gathering", "POST")

async def craft(character: str, item_code: str, quantity: int) -> Any:
    raw = json.dumps({"code": item_code, "quantity": quantity})
    return await send_request(character, "/action/crafting", "POST", raw)

async def recycle(character: str, code: str, quantity: int) -> Any:
    raw = json.dumps({"code": code, "quantity": quantity})
    return await send_request(character, "/action/recycling", "POST", raw)

async def unequip(character: str, slot: str) -> Any:
    raw = json.dumps({"slot": slot})
    return await send_request(character, "/action/unequip", "POST", raw)

async def equip(character: str, code: str, slot: str) -> Any:
    raw = json.dumps({"code": code, "slot": slot})
    return await send_request(character, "/action/equip", "POST", raw)

async def get_character(name: str) -> Any:
    return await send_request_to_url(f"https://api.artifactsmmo.com/characters/{name}", "", "GET")

async def deposit_item(character: str, item: dict) -> Any:
    body = json.dumps({"code": item["code"], "quantity": item["quantity"]})
    return await send_request(character, "/action/bank/deposit", "POST", body)

async def withdraw_item(character: str, code: str, quantity: int) -> Any:
    body = json.dumps({"code": code, "quantity": quantity})
    return await send_request(character, "/action/bank/withdraw", "POST", body)

async def use_item(character: str, code: str, quantity: int) -> Any:
    body = json.dumps({"code": code, "quantity": quantity})
    return await send_request(character, "/action/use", "POST", body)

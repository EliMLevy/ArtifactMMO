from datetime import datetime
import json
import threading
import time
from typing import Any

# Import your utility functions
from data_classes import InventoryItem
from util import send_request, send_request_to_url

def move(character: str, x: int, y: int) -> Any:
    raw = {"x": x, "y": y}
    result = send_request(character, "/action/move", "POST", raw)
    return result

def attack(character: str) -> Any:
    return send_request(character, "/action/fight", "POST", None)

def rest(character: str) -> Any:
    return send_request(character, "/action/rest", "POST", None)

def collect(character: str) -> Any:
    return send_request(character, "/action/gathering", "POST", None)

def craft(character: str, item_code: str, quantity: int) -> Any:
    raw = {"code": item_code, "quantity": quantity}
    return send_request(character, "/action/crafting", "POST", raw)

def recycle(character: str, code: str, quantity: int) -> Any:
    raw = {"code": code, "quantity": quantity}
    return send_request(character, "/action/recycling", "POST", raw)

def unequip(character: str, slot: str) -> Any:
    raw = {"slot": slot}
    return send_request(character, "/action/unequip", "POST", raw)

def equip(character: str, code: str, slot: str, quantity: int = 1) -> Any:
    raw = {"code": code, "slot": slot, "quantity": quantity}
    return send_request(character, "/action/equip", "POST", raw)

def get_character(name: str) -> Any:
    return send_request_to_url(f"https://api.artifactsmmo.com/characters/{name}", "", "GET", None)


last_fetched_bank_items = None
bank_item_fetch_refresh = 30 # seconds
cached_bank_items = None
lock = threading.Lock()
def get_bank_items():
    global cached_bank_items, last_fetched_bank_items, bank_item_fetch_refresh
    lock.acquire()
    if cached_bank_items is None or last_fetched_bank_items is None or (datetime.now() - last_fetched_bank_items).seconds > bank_item_fetch_refresh:
        print("Getting bank items")
        page = 1
        all_results = []
        done = False

        while not done:
            result = send_request_to_url(f"https://api.artifactsmmo.com/my/bank/items?page={page}", "", "GET", None)
            print(len(result["data"]))
            all_results.extend(result["data"])

            if result["pages"] > page:
                page += 1
            else:
                break
            time.sleep(1)
        cached_bank_items = all_results
        last_fetched_bank_items = datetime.now()
        lock.release()
        return all_results
    lock.release()
    return cached_bank_items

def get_bank_quantity(item_code: str):
    bank_items = get_bank_items()
    return next((item["quantity"] for item in bank_items if item["code"] == item_code), 0)

def deposit_item(character: str, item: InventoryItem) -> Any:
    body = {"code": item.code, "quantity": item.quantity}
    return send_request(character, "/action/bank/deposit", "POST", body)

def withdraw_item(character: str, code: str, quantity: int) -> Any:
    body = {"code": code, "quantity": quantity}
    return send_request(character, "/action/bank/withdraw", "POST", body)

def use_item(character: str, code: str, quantity: int) -> Any:
    body = {"code": code, "quantity": quantity}
    return send_request(character, "/action/use", "POST", body)

def accept_new_task(character: str):
    return send_request(character, "/action/task/new", "POST", None)

def complete_task(character: str):
    return send_request(character, "/action/task/complete", "POST", None)

def trade_with_task_master(character: str, code: str, quantity: int):
    body = {"code": code, "quantity": quantity}
    return send_request(character, "/action/task/trade", "POST", body)
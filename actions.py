import json
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

def equip(character: str, code: str, slot: str) -> Any:
    raw = {"code": code, "slot": slot}
    return send_request(character, "/action/equip", "POST", raw)

def get_character(name: str) -> Any:
    return send_request_to_url(f"https://api.artifactsmmo.com/characters/{name}", "", "GET", None)

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
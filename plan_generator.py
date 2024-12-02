import pandas as pd
import json

from actions import get_bank_items
from encyclopedia import get_item_by_name


def get_quantity_of_item_in_bank(character, item_code):   
    return next((item["quantity"] for item in get_bank_items(character) if item["code"] == item_code), 0)

def calculate_batch_size(quantity_needed, item_requirements, max_inventory_items):
    """
    Calculate how many items we can craft in one batch given inventory constraints.
    
    Args:
        quantity_needed: How many items we want to craft in total
        item_requirements: List of ingredients and their quantities needed per craft
        max_inventory_items: Maximum items that can be held in inventory
        
    Returns:
        Maximum number of items we can craft in one batch
    """
    if not item_requirements:  # If no requirements (base item), just return what fits in inventory
        return min(quantity_needed, max_inventory_items)
    
    # Calculate how many items we need in inventory per craft
    total_items_per_craft = sum(ingredient["quantity"] for ingredient in item_requirements)
    
    # Calculate max items we can craft based on inventory space
    max_craftable = max_inventory_items // total_items_per_craft
    
    # Return the smaller of what we need and what we can fit
    return min(quantity_needed, max_craftable)

def generate_crafting_plan(item_code, quantity_needed, max_inventory_items, use_bank=True):
    """
    Generates a plan for crafting items, considering items available in bank
    and inventory space limitations.
    
    Args:
        item_code: The code of the item to craft
        quantity_needed: How many of the item are needed
        all_items: Lookup table containing item recipes
        max_inventory_items: Maximum number of items that can be held in inventory
        
    Returns:
        List of actions, where each action is a dict with keys:
        - action: "withdraw" or "craft"
        - code: item code
        - quantity: how many to withdraw/craft
    """
    plan = []
    remaining_quantity = quantity_needed
    
    while remaining_quantity > 0:
        # First check if we have any in bank
        if use_bank:
            bank_quantity = get_quantity_of_item_in_bank(item_code)
            if bank_quantity > 0:
                # Calculate how much we can withdraw based on inventory space
                withdraw_quantity = min(bank_quantity, remaining_quantity, max_inventory_items)
                
                plan.append({
                    "action": "withdraw",
                    "code": item_code,
                    "quantity": withdraw_quantity
                })
                remaining_quantity -= withdraw_quantity
        
        # If we still need more, we need to craft them
        if remaining_quantity > 0:
            item_info = get_item_by_name(item_code)
            
            # If item has no recipe, it needs to be collected
            if "recipe" not in item_info:
                plan.append({"action": "collect", "code": item_code, "quantity": remaining_quantity})
                return plan
                # raise ValueError(f"Item {item_code} needs to be collected - no recipe available")
            
            # Calculate how many we can craft in this batch based on inventory limits
            batch_size = calculate_batch_size(
                remaining_quantity,
                item_info["recipe"]["items"],
                max_inventory_items
            )
            
            if batch_size == 0:
                raise ValueError(f"Cannot craft {item_code} - ingredients exceed inventory limit of {max_inventory_items}")
            
            # Get ingredients for this batch
            for ingredient in item_info["recipe"]["items"]:
                ingredient_code = ingredient["code"]
                ingredient_quantity = ingredient["quantity"] * batch_size
                
                # Recursively get plan for this ingredient
                ingredient_plan = generate_crafting_plan(
                    ingredient_code,
                    ingredient_quantity,
                    max_inventory_items
                )
                plan.extend(ingredient_plan)
            
            # After all ingredients are handled, add the crafting step
            plan.append({
                "action": "craft",
                "code": item_code,
                "quantity": batch_size
            })
            
            remaining_quantity -= batch_size
    
    return plan

if __name__ == "__main__":
    plan = generate_crafting_plan("iron_pickaxe", 5, 120)
    plan.insert(0, {"action": "deposit all"})
    print(plan)
    

    # for step in plan:
    #     print(json.dumps(step), ",")
    print(json.dumps({"action": "deposit all"}))
    


import math
from actions import get_bank_items, get_bank_quantity
from encyclopedia import get_item_by_name, get_location_by_monster_drop, get_location_by_resource



class Plan():
    '''
    Each plan is comprised of a a list of dependencies (plans) and an action
    The asumption is that once the dependencies are completed, the action will be able to 
    succeed and it will result in the player having the required items in their inventory.

    Pass in a copy of the current bank state so that every subplan draws from a common pool of items
    The use_bank flag is used to determine whether the top level action should use th bank. When
    I am trying to improve my crafting skill I dont want to withdraw the crafted item from the bank
    but I do want to withdraw the ingredients from the bank.
    '''
    def __init__(self, code, quantity, max_inventory_space, bank, use_bank=True):
        # Figure out dependendies
        self.code = code
        self.quantity = quantity # this will be used to withdraw the final amount (includes stuff in bank)
        self.quantity_needed = quantity # This will be used to determine how much needs to be acquired (doesnt include stuff in bank)
        self.item = get_item_by_name(code)
        self.dependencies: list[Plan] = []
        self.final_action = None
        self.max_inventory_space = max_inventory_space
        self.bank = bank

        if self.item is None:
            raise Exception(f"Failed to find item {code}")

        def get_bank_quantity(item_code):
            return next((item["quantity"] for item in bank if item["code"] == item_code), 0)

        def set_bank_quantity(item_code, quantity):
            item = next((item for item in bank if item["code"] == item_code), None)
            if item is not None:
                item["quantity"] = quantity
        if use_bank:
            quantity_in_bank = get_bank_quantity(self.code) # Get the quantity of {code} in bank
            # If we have more than enough, final action is noop
            if quantity_in_bank >= self.quantity_needed:
                set_bank_quantity(self.code, quantity_in_bank - self.quantity_needed)
                self.final_action = {"action": "noop", "desc": f"we had enough {self.code} in the bank. Remaining { quantity_in_bank - self.quantity_needed}", "quantity": self.quantity, "code": self.code }
                self.quantity_needed = 0
            # If we have some but not enough, just subtract it from quantity (it will be there when we withdraw)
            else:
                self.quantity_needed -= quantity_in_bank

        
        if self.quantity_needed > 0:
            # We need more than we have in the bank.
            if "recipe" in self.item:
                sum_of_ingredients = 0
                for ingredient in self.item["recipe"]["items"]:
                    new_plan = Plan(ingredient["code"], ingredient["quantity"] * self.quantity_needed, self.max_inventory_space, bank)
                    sum_of_ingredients += new_plan.final_action["quantity"]
                    self.dependencies.append(new_plan)
                self.final_action = {"action": "craft", "code": self.code, "quantity": self.quantity_needed}
                if sum_of_ingredients > self.max_inventory_space:
                    self.split_step()
            else:
                # If it isnt craftable, the final action will be to collect it
                locations = get_location_by_resource(code)
                if len(locations) > 0:
                    location = locations.iloc[0]
                    # It is a resource
                    self.final_action = {"action": "collect", "code": self.code, "quantity": self.quantity_needed}
                else:
                    locations = get_location_by_monster_drop(code)
                    if len(locations) > 0:
                        location = locations.iloc[0]
                        # It is a monster drop
                        self.final_action = {"action": "monster drop", "monster code": location["map_code"], "code": self.code, "quantity": self.quantity_needed}
                    else:
                        # It isnt a mob drop or resource and it isnt craftable. We are stumped...
                        raise Exception(f"Cannot determine plan to acquire {self.code}")


    def split_step(self):
        '''
        Turn this plan into a NO-OP plan where the dependencies
        are plans for self.code but the quantity is an amount that respects inventory space
        '''
        if "recipe" not in self.item:  # If no requirements (base item), just return what fits in inventory
            return min(self.quantity, self.max_inventory_space)
    
        # Calculate how many items we need in inventory per craft
        total_items_per_craft = sum(ingredient["quantity"] for ingredient in self.item["recipe"]["items"])
        
        # Calculate max items we can craft based on inventory space
        max_craftable = self.max_inventory_space // total_items_per_craft
        
        # Return the smaller of what we need and what we can fit
        max_batch_size = min(self.quantity, max_craftable)

        self.dependencies = []
        quantity_remaining = self.quantity
        batches_needed = math.ceil(self.quantity / max_batch_size)
        for i in range(batches_needed):
            if quantity_remaining < 0:
                raise Exception(f"We have an erro in our logic {self.code} {self.quantity} {max_batch_size}")
            self.dependencies.append(Plan(self.code, min(max_batch_size, quantity_remaining), self.max_inventory_space, self.bank))
            quantity_remaining -= max_batch_size
        self.final_action = {"action": "noop", "desc": f"split {self.code} x{self.quantity} into {batches_needed} batchs", "code": self.code,  "quantity": self.quantity}


    def get_executable(self, executable):
        for d in self.dependencies:
            d.get_executable(executable)
            executable.append({"action": "deposit all"})
        if self.final_action["action"] != "noop":
            for d in self.dependencies:
                executable.append({"action": "withdraw", "code": d.final_action["code"], "quantity": d.quantity})

            executable.append(self.final_action)
        return executable

    def __str__(self):
        result = f"====PLAN FOR: {self.code}====\n"
        result += f"dependencies:\n"
        for d in self.dependencies:
            result += str(d)
        else:
            result += "No dependencies\n"
        result += f"final action: {self.final_action}\n"
        result += "==============================\n"
        return result


def generate_plan(item_code, quantity, max_inventory_space, use_bank=False):
    bank = [x for x in get_bank_items()]
    plan = Plan(item_code, quantity,max_inventory_space, bank=bank, use_bank=use_bank)
    # print(plan)
    result = plan.get_executable([])
    if len(result) == 0:
        # If we have enough in the bank it will return an empty plan
        return [{"action": "withdraw", "code": item_code, "quantity": quantity}]
    else:
        return result


if __name__ == "__main__":

    executable = generate_plan("life_ring", 5, max_inventory_space=120, use_bank=False)
    print(executable)
    for step in executable:
        print(step)
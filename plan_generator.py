import pandas as pd
import json

from actions import get_bank_items

# Given an item, create a plan for aquiring it
# - Look the item up in the items
# - if it isnt craftable, look it up in the resources and monsters
#   - if found, go there and collect
# - if it is craftable, repeat for ingredients

resources = pd.read_csv("./encyclopedia/resources.csv")
monsters = pd.read_csv("./encyclopedia/monsters.csv")


items_file = open("./encyclopedia/items/all_items.json")
items = json.loads(items_file.read())

maps_file = open("./encyclopedia/maps/all_maps.json")
maps = json.loads(maps_file.read())

bank_items = get_bank_items()


class Plan():
    def __init__(self):
        self.withdrawal = [] # for ingredients we just need to withdraw
        self.dependencies = [] # for ingredients we need to craft
        self.target_craft = None
    
    def split_plan(self):
        '''
        Split the target craft into two
        calculate the crafts that need to be done for each plan
        Calculate the withdrawals for each plan
        '''
        split = self.target_craft["quantity"] // 2 # Integer division to maintain whole number
        plan_1 = Plan()
        plan_1.target_craft = {
            "action": "craft",
            "code": self.target_craft["code"],
            "quantity": split
        }
        plan_1_proportion = split / self.target_craft["quantity"]
        plan_2 = Plan()
        plan_2.target_craft = {
            "action": "craft",
            "code": self.target_craft["code"],
            "quantity": self.target_craft["quantity"] - split
        }
        plan_2_proportion = (self.target_craft["quantity"] - split) / self.target_craft["quantity"]


        for withdrawal in self.withdrawals:
            plan_1.withdrawals.append({
                "action": "withdraw",
                "code": withdrawal["code"],
                "quantity": withdrawal["quantity"] * plan_1_proportion
            })
            plan_2.withdrawals.append({
                "action": "withdraw",
                "code": withdrawal["code"],
                "quantity": withdrawal["quantity"] * plan_2_proportion
            })
        return plan_1, plan_2

    def is_feasible(self, max_items):
        total_items = 0
        for withdrawal in self.withdrawals:
            total_items += withdrawal["quantity"] 
        return total_items <= max_items

    def split_if_infeasible(self, max_items):
        if not self.is_feasible(max_items):
            


    def __str__(self):
        result = f"===={self.target_craft['code']} x{self.target_craft['quantity']}===\n" 
        for d in self.dependencies:
            result += str(d) + "\n"
        for w in self.withdrawal:
            result +=  f"withdraw {w['code']} x{w['quantity']}\n"
        result += f"===={self.target_craft['code']}===="
        return result

def get_quantity_of_item_in_bank(item_code):   
    return next((item["quantity"] for item in bank_items if item["code"] == item_code), 0)
    

def check_monsters_or_resources(df, item_code):
    locations = df[df["resource_code"] == item_code]
    if len(locations) > 0:
        locations = locations.sort_values(by=['drop_chance'], ascending=True)
        target_location = locations.iloc[0]
        return (int(target_location['x']), int(target_location['y']))
    else:
        return None
            
def gather_craftable_item(target_item, quantity):
    plan = []
    for ingredient in target_item["recipe"]["items"]:
        plan.extend(create_plan(ingredient["code"], quantity * ingredient["quantity"]))
    skill_needed = target_item["recipe"]["skill"]
    if skill_needed in maps:
        plan.extend([{"action": "craft", "code": target_item['code'], "quantity": quantity }])
    else:
        plan.append({"action": "error", "message": f"Cannot find {skill_needed}"})
    return plan

def collect_resource(item_code, quantity):
    # Check resources
    resource_location = check_monsters_or_resources(resources, item_code)
    if resource_location != None:
        return [{"action": "collect", "repeat": quantity, "code": item_code}]
    # Check monsters
    monster_location = check_monsters_or_resources(monsters, item_code)
    if monster_location != None:
        return [{"action": "attack", "repeat": quantity, "code": item_code}]
    else:
        return [{"action": "error", "message": f"cannot find {item_code}"}]

'''
Each call to create_plan will create a plan.
a plan has one or more withdrawals
and ONE craft
This becomes very easy to determine if the plan is feasible or needs to be split -> just sum up the withdrawals
This also makes it easier to split -> one plan becomes two plans -> craft a portion of the target and withdraw a portion
'''
def create_plan(plan: Plan, item_code, quantity) -> list[str]:
    if get_quantity_of_item_in_bank(item_code) >= quantity:
        print(f"withdrawing {item_code}")
        plan.target_craft = {"code": item_code, "quantity": quantity}
        plan.withdrawal.append({"action": "withdraw", "code": item_code, "quantity": quantity})
        return plan
        

    if item_code in items:
        target_item = items[item_code]
        if "recipe" in target_item:
            for ingredient in target_item["recipe"]["items"]:
                plan.dependencies.append(create_plan(Plan(), ingredient["code"], quantity * ingredient["quantity"]))
        else:
            return f"UNIMPLEMENTED: {item_code} needs collection"
        plan.target_craft = {"action": "craft", "code": target_item['code'], "quantity": quantity }
        return plan
    else:
        return f"Can not attain {item_code}"
    

def run_create_plan(code, quantity):
    # plan = [{"action": "deposit all"}]
    # plan.extend(create_plan(Plan(), code, quantity))
    # plan.append({"action": "deposit all"})
    return create_plan(Plan(), code, quantity)

if __name__ == '__main__':
    plan = run_create_plan("greater_wooden_staff", 5)
    print(plan)
    # for item in plan:
    #     print(json.dumps(item), ",")
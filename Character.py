import math
import time
import logging
from datetime import datetime, timezone
from dataclasses import dataclass, field
from actions import accept_new_task, attack, complete_task, craft, equip, get_bank_quantity, get_character, move, recycle, rest, trade_with_task_master, unequip
from data_classes import InventoryItem
import encyclopedia as ency
from plan_generator import generate_crafting_plan
from util import handle_result_cooldown
from gear_analyzer import find_best_armor_for_monster, find_best_weapon_for_monster

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

LOW_HP_THRESHOLD = 0.6
LOW_INVENTORY_SPACE_THRESHOLD = 0.9

GEAR_SLOTS = ["shield","helmet","body_armor","leg_armor","boots","ring", "amulet"]

class Character:
    def __init__(self, name):
        self.logger = logging.getLogger(f"{name}")
        self.logger.info(f"Character {name} created!")
        self.name = name
        self.default_action = "idle" # "tasks"
        self.default_subaction = "monsters"
        self.plan = []
        self.last_loaded_data = None

    def load_data(self):
        if self.last_loaded_data != None and (datetime.now() - self.last_loaded_data).total_seconds() <= 5:
            # Our data is likely up to date. Lets not risk sending too many requests
            return
        result = get_character(self.name)
        self.last_loaded_data = datetime.now()
        self.set_data(result["data"])
    
    def set_data(self, data: dict):
        # Basic info
        self.name = data["name"]
        self.account = data["account"]
        self.skin = data["skin"]
        self.level = data["level"]
        self.xp = data["xp"]
        self.max_xp = data["max_xp"]
        self.gold = data["gold"]
        
        # Stats
        self.speed = data["speed"]
        self.hp = data["hp"]
        self.max_hp = data["max_hp"]
        self.haste = data["haste"]
        self.critical_strike = data["critical_strike"]
        self.stamina = data["stamina"]
        
        # Attributes
        self.attack_fire = data["attack_fire"]
        self.attack_earth = data["attack_earth"]
        self.attack_water = data["attack_water"]
        self.attack_air = data["attack_air"]
        self.dmg_fire = data["dmg_fire"]
        self.dmg_earth = data["dmg_earth"]
        self.dmg_water = data["dmg_water"]
        self.dmg_air = data["dmg_air"]
        self.res_fire = data["res_fire"]
        self.res_earth = data["res_earth"]
        self.res_water = data["res_water"]
        self.res_air = data["res_air"]
        
        # Coordinates
        self.x = data["x"]
        self.y = data["y"]
        
        # Cooldowns
        self.cooldown = data["cooldown"]
        self.cooldown_expiration = datetime.strptime(data["cooldown_expiration"], "%Y-%m-%dT%H:%M:%S.%fZ")
        # Slots
        self.weapon_slot = data["weapon_slot"]
        self.shield_slot = data["shield_slot"]
        self.helmet_slot = data["helmet_slot"]
        self.body_armor_slot = data["body_armor_slot"]
        self.leg_armor_slot = data["leg_armor_slot"]
        self.boots_slot = data["boots_slot"]
        self.ring1_slot = data["ring1_slot"]
        self.ring2_slot = data.get("ring2_slot", "")
        self.amulet_slot = data.get("amulet_slot", "")
        self.artifact1_slot = data.get("artifact1_slot", "")
        self.artifact2_slot = data.get("artifact2_slot", "")
        self.artifact3_slot = data.get("artifact3_slot", "")
        self.utility1_slot = data.get("utility1_slot", "")
        self.utility1_slot_quantity = data.get("utility1_slot_quantity", 0)
        self.utility2_slot = data.get("utility2_slot", "")
        self.utility2_slot_quantity = data.get("utility2_slot_quantity", 0)
        
        # Task
        self.task = data["task"]
        self.task_type = data["task_type"]
        self.task_progress = data["task_progress"]
        self.task_total = data["task_total"]
        
        # Inventory
        self.inventory_max_items = data["inventory_max_items"]
        self.inventory = [
            InventoryItem(slot=item["slot"], code=item["code"], quantity=item["quantity"])
            for item in data["inventory"]
        ]
        
        # Skills
        self.mining_level = data["mining_level"]
        self.mining_xp = data["mining_xp"]
        self.mining_max_xp = data["mining_max_xp"]
        self.woodcutting_level = data["woodcutting_level"]
        self.woodcutting_xp = data["woodcutting_xp"]
        self.woodcutting_max_xp = data["woodcutting_max_xp"]
        self.fishing_level = data["fishing_level"]
        self.fishing_xp = data["fishing_xp"]
        self.fishing_max_xp = data["fishing_max_xp"]
        self.weaponcrafting_level = data["weaponcrafting_level"]
        self.weaponcrafting_xp = data["weaponcrafting_xp"]
        self.weaponcrafting_max_xp = data["weaponcrafting_max_xp"]
        self.gearcrafting_level = data["gearcrafting_level"]
        self.gearcrafting_xp = data["gearcrafting_xp"]
        self.gearcrafting_max_xp = data["gearcrafting_max_xp"]
        self.jewelrycrafting_level = data["jewelrycrafting_level"]
        self.jewelrycrafting_xp = data["jewelrycrafting_xp"]
        self.jewelrycrafting_max_xp = data["jewelrycrafting_max_xp"]
        self.cooking_level = data["cooking_level"]
        self.cooking_xp = data["cooking_xp"]
        self.cooking_max_xp = data["cooking_max_xp"]
        self.alchemy_level = data["alchemy_level"]
        self.alchemy_xp = data["alchemy_xp"]
        self.alchemy_max_xp = data["alchemy_max_xp"]


    def run_agent(self):
        from enhanced_actions import go_and_collect_item, deposit_all_items
        while True:
            self.load_data()
            if self.plan != None and len(self.plan) > 0:
                self.execute_action(self.plan.pop(0))
            elif self.default_action == "idle":
                time.sleep(10)
            elif self.default_action == "tasks":
                self.complete_tasks(self.default_subaction)
            elif self.default_action == "collect loop": # Collects the highest resource unlocked for the lowest skill
                self.improve_collect_skills()
            elif self.default_action == "collecting": # Collects the high resource unlocked for a specific skill
                self.complete_resource_collect(self.default_subaction)
            elif self.default_action == "collect": # Collects a specific resource
                if self.needs_to_deposit():
                    self.logger.debug(f"needs to deposit")
                    deposit_all_items(self)
                go_and_collect_item(self, self.default_subaction)
            elif self.default_action == "attack":
                if self.needs_to_deposit():
                    self.logger.debug(f"needs to deposit")
                    deposit_all_items(self)
                self.attack_monster(self.default_subaction)
            elif self.default_action == "craft":
                deposit_all_items(self)
                plan = generate_crafting_plan(self.default_subaction["code"], self.default_subaction["quantity"], math.floor(self.inventory_max_items * 0.75), use_bank=False)
                self.logger.info(f"Plan to acquire {self.default_subaction['code']}: {plan}")
                self.execute_plan(plan)
            

    def execute_action(self, action):
        from enhanced_actions import withdraw_from_bank, deposit_all_items,go_and_craft_item,go_and_collect_item
        self.load_data()
        if self.cooldown > 0 and datetime.now(timezone.utc) < self.cooldown_expiration.astimezone(timezone.utc):
            time.sleep(self.cooldown)

        if action["action"] == "deposit all":
            self.logger.info(f"Depositing all")
            deposit_all_items(self)
        elif action["action"] == "withdraw":
            self.logger.info(f"Withdrawing {action['code']} x{action['quantity']}")
            withdraw_from_bank(self, action["code"], action["quantity"])
        elif action["action"] == "move":
            self.logger.info(f"Moving to {action['x']},{action['y']}")
            result = move(self.name, action["x"], action["y"])
            handle_result_cooldown(result)
        elif action["action"] == "craft":
            self.logger.info(f"Crafting {action['code']} x{action['quantity']}")
            go_and_craft_item(self, action["code"], action["quantity"])
        elif action["action"] == "recycle":
            self.logger.info(f"Recycling {action['code']} x{action['quantity']}")
            result = recycle(self.name, action["code"], action["quantity"])
            handle_result_cooldown(result)
        elif action["action"] == "collect":
            self.logger.info(f"Colecting {action['code']} x{action['quantity']}")
            for i in range(int(action["quantity"])):
                go_and_collect_item(self, action["code"])
        elif action["action"] == "monster drop":
            self.logger.info(f"Acquiring monster drop {action['code']} x{action['quantity']}")
            while self.get_quantity_of_inv_item(action["code"]) < action["quantity"]:
                self.load_data()
                if self.needs_to_deposit():
                    self.logger.debug(f"needs to deposit")
                    deposit_all_items(self)
                self.logger.info("Attacking monster")
                self.attack_monster(action["monster code"])
        else:
            time.sleep(5)

    def execute_plan(self, plan):
        for item in plan:
            self.execute_action(item)
    
    def complete_resource_collect(self, skill):
        from enhanced_actions import collect_highest_unlocked_resource, deposit_all_items
        self.load_data()
        if self.cooldown > 0 and datetime.now(timezone.utc) < self.cooldown_expiration.astimezone(timezone.utc):
            time.sleep(self.cooldown)

        if self.needs_to_deposit():
            self.logger.debug(f"needs to deposit")
            deposit_all_items(self)

        if skill == "mining" and self.weapon_slot != 'iron_pickaxe':
            self.equip_new_gear("weapon", "iron_pickaxe")

        self.logger.info(f"Collecting {skill}")
        collect_highest_unlocked_resource(self, skill)
            
    def improve_collect_skills(self):
        # Find the most untrained skill
        self.load_data()
        skills = {
            "mining": self.mining_level,
            "woodcutting": self.woodcutting_level,
            "fishing": self.fishing_level,
            "alchemy": self.alchemy_level
        }
        most_untrained = min(skills, key=skills.get)
        # self.complete_resource_collect
        self.complete_resource_collect(most_untrained)

    def complete_tasks(self, task_type):
        from enhanced_actions import move_to_location, deposit_all_items
        self.logger.info(f"Completing {task_type} tasks")
        self.load_data()

        if self.cooldown > 0 and datetime.now(timezone.utc) < self.cooldown_expiration.astimezone(timezone.utc):
            time.sleep(self.cooldown)
        
        
        if self.task is None or len(self.task) == 0:
            # get a task from task master
            self.logger.info(f"Getting task {task_type}")
            move_to_location(self, task_type)
            result = accept_new_task(self.name)
            handle_result_cooldown(result)
            return


        # If progress is finished, complete the task
        if self.task_progress >= self.task_total:
            self.logger.info(f"Completing task")
            move_to_location(self, self.task_type)
            complete_task(self.name)
            return


        if self.needs_to_deposit():
            deposit_all_items(self)
            return
        

        if self.task_type == "monsters":
            self.attack_monster(self.task)
        elif self.task_type == "items":
            self.complete_item_tasks()
        else:
            self.logger.warning(f"Invalid task type: {task_type}")

    def complete_item_tasks(self):
        from enhanced_actions import withdraw_from_bank, deposit_all_items,move_to_location


        deposit_all_items(self)
        self.logger.info("Depsited")

        # Withdraw as much of this item as I can from the bank and trade it
        withdraw_amount = min(self.inventory_max_items, get_bank_quantity(self.task), self.task_total - self.task_progress)
        if withdraw_amount > 0:
            self.logger.info(f"Trading {withdraw_amount} to task master (out of {self.task_total - self.task_progress} needed)")
            # Withdraw it
            withdraw_from_bank(self, self.task, withdraw_amount)
            # Trade it
            move_to_location(self, "items")
            result = trade_with_task_master(self.name, self.task, withdraw_amount)
            handle_result_cooldown(result)
            return
        

        # If we get here, we are not finished with task but there is no more to withdraw
        # We need to create a plan for acquiring it, then proceed
        acquire_amount = min(self.task_total - self.task_progress, math.floor(self.inventory_max_items * 0.75)) # we may need to do several trips
        plan = generate_crafting_plan(self.task, acquire_amount, math.floor(self.inventory_max_items * 0.75))
        self.logger.info(f"Plan to acquire {self.task}: {plan}")

        self.execute_plan(plan)
        return # On the next visit to this method there should be some of that item in the bank


    def attack_monster(self, monster_code):
        from enhanced_actions import move_to_location

        # Rest before unequipping any gear because its possible that we are very low hp
        if self.needs_rest():
            self.logger.debug(f"Resting!")
            result = rest(self.name)
            handle_result_cooldown(result)


        best_weapon = find_best_weapon_for_monster(monster_code, self.level, available_in_bank=True,  current_weapon=self.weapon_slot, current_inventory=self.inventory)
        if best_weapon != False and best_weapon["code"] != self.weapon_slot:
            self.logger.info(f"The {best_weapon['code']} is better against {monster_code} than {self.weapon_slot}")
            self.equip_new_gear("weapon", best_weapon["code"])

        for gear_slot in GEAR_SLOTS:
            best_gear = find_best_armor_for_monster(monster_code, gear_slot, self.level, available_in_bank=True, current_armor=self.get_active_gear(gear_slot), current_inventory=self.inventory )
            # self.logger.info(f"Found best gear: {best_gear}")
            if best_gear != False and best_gear["code"] != self.get_active_gear(gear_slot):
                self.logger.info(f"The {best_gear['code']} is better against {monster_code} than {self.get_active_gear(gear_slot)}")
                if gear_slot == "ring":
                    self.equip_new_gear("ring1", best_gear["code"])
                else:
                    self.equip_new_gear(gear_slot, best_gear["code"])



        # If above is false
        # Move to location of task
        self.logger.info(f"Moving to tasks {monster_code}")
        move_to_location(self, monster_code)

        
            
        # attack
        self.logger.info(f"attacking {monster_code}")
        result = attack(self.name)
        handle_result_cooldown(result)

    def get_skill_level(self, skill: str):
        if skill == "fishing":
            return self.fishing_level
        if skill == "mining":
            return self.mining_level
        if skill == "woodcutting":
            return self.woodcutting_level

    def needs_rest(self):
        return self.hp / self.max_hp < LOW_HP_THRESHOLD
    
    def needs_to_deposit(self):
        items = sum(item.quantity for item in self.inventory)
        return items / self.inventory_max_items >= LOW_INVENTORY_SPACE_THRESHOLD

    def get_active_gear(self, slot):
        if slot == "weapon":
            return self.weapon_slot
        if slot == "shield":
            return self.shield_slot
        if slot == "helmet":
            return self.helmet_slot
        if slot == "body_armor":
            return self.body_armor_slot
        if slot == "leg_armor":
            return self.leg_armor_slot
        if slot == "boots":
            return self.boots_slot
        if slot == "ring1" or slot == "ring":
            return self.ring1_slot
        if slot == "ring2":
            return self.ring2_slot
        if slot == "amulet":
            return self.amulet_slot
        if slot == "utility1":
            return self.utility1_slot, self.utility1_slot_quantity
        if slot == "utility2":
            return self.utility2_slot, self.utility2_slot_quantity

    def equip_utility(self, slot, utility_code, quantity, extra_in_inv):
        '''
        Args:
            slot: the utility slot to put item into. Either utility1 or utility2
            utility_code: the item code for the item
            quantity: the amount that we want in our utility slot
            extra_in_inv: if we need to craft more, how much should we craft
        '''
        util, util_quantity = self.get_active_gear(slot)
        if util == utility_code and quantity <= util_quantity:
            return

        self.logger.info(f"Equipping more {utility_code} x{quantity}")
        quantity_to_equip = quantity    
        if util != utility_code:
            # Unequip the undesired utility
            result = unequip(self.name, slot)
            handle_result_cooldown(result)
        else:
            quantity_to_equip -= util_quantity

        quantity_in_inventory = self.get_quantity_of_inv_item(util_quantity)
        if quantity_in_inventory > 0:
            result = equip(self.name, utility_code, slot) 
            quantity_to_equip -= quantity_in_inventory
            handle_result_cooldown(result)


        # If we get here, we dont have any more in our inv, so we need a plan to acquire some
        if quantity_to_equip > 0:
            plan = generate_crafting_plan(utility_code, max(quantity_to_equip, extra_in_inv), math.floor(self.inventory_max_items * 0.75))
            self.logger.info(f"Acquiring more {utility_code} with this plan: {plan}")
            self.execute_plan(plan)
            # At this point we can return and the next time it is called it should be in our inventory

        

    def equip_new_gear(self, slot, new_gear_code):
        from enhanced_actions import deposit_all_items, withdraw_from_bank

        if self.get_active_gear(slot) == new_gear_code:
            self.logger.info(f"{new_gear_code} already active in {slot}")
            return

        # Unequip old slot
        self.logger.info(f"Unequipping {slot}")
        result = unequip(self.name, slot)
        handle_result_cooldown(result)
        # reload data so that the inventory is accurate
        self.load_data()
        # deposit the gear
        self.logger.info(f"Depositing all")
        deposit_all_items(self)

        # withdraw the new gear
        withdraw_from_bank(self, new_gear_code, 1)

        # equip new gear
        self.logger.info(f"equipping {new_gear_code} into {slot}")
        result = equip(self.name, new_gear_code, slot)
        handle_result_cooldown(result)

    def get_quantity_of_inv_item(self, item_code):
        return next((item.quantity for item in self.inventory if item.code == item_code), 0)
    
    def improve_gear_crafting_stat():
        # Find the gear at the highest level unlocked that is the cheapest to make
        pass
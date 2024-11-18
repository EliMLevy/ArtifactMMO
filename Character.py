import time
from datetime import datetime
from dataclasses import dataclass, field
from actions import accept_new_task, attack, complete_task, craft, equip, get_character, move, recycle, rest, unequip
from data_classes import InventoryItem
import encyclopedia as ency
from util import handle_result_cooldown

LOW_HP_THRESHOLD = 0.5
LOW_INVENTORY_SPACE_THRESHOLD = 0.9

class Character:
    def __init__(self, name):
        print(f"Character {name} created!")
        self.name = name
        self.current_action = "collecting"
        self.current_subaction = "woodcutting"

    def load_data(self):
        result = get_character(self.name)
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
        while True:
            if self.current_action == "idle":
                time.sleep(3)
                continue
            elif self.current_action == "collecting":
                self.complete_resource_collect(self.current_subaction)


    def execute_plan(self, plan):
        from enhanced_actions import withdraw_from_bank, deposit_all_items
        for item in plan:
            self.load_data()
            if self.cooldown > 0 and datetime.now() < self.cooldown_expiration:
                time.sleep(self.cooldown)

            if item["action"] == "deposit all":
                print(f"[{self.name}] Depositing all")
                deposit_all_items(self)
            elif item["action"] == "withdraw":
                print(f"[{self.name}] Withdrawing {item['code']} x{item['quantity']}")
                withdraw_from_bank(self, item["code"], item["quantity"])
            elif item["action"] == "move":
                print(f"[{self.name}] Moving to {item['x']},{item['y']}")
                result = move(self.name, item["x"], item["y"])
                handle_result_cooldown(result)
            elif item["action"] == "craft":
                print(f"[{self.name}] Crafting {item['code']} x{item['quantity']}")
                result = craft(self.name, item["code"], item["quantity"])
                handle_result_cooldown(result)
            elif item["action"] == "recycle":
                print(f"[{self.name}] Recycling {item['code']} x{item['quantity']}")
                result = recycle(self.name, item["code"], item["quantity"])
                handle_result_cooldown(result)
    
    def complete_resource_collect(self, skill):
        from enhanced_actions import collect_highest_unlocked_resource, deposit_all_items
        self.load_data()
        if self.cooldown > 0 and datetime.now() < self.cooldown_expiration:
            time.sleep(self.cooldown)

        if self.needs_to_deposit():
            print(f"[{self.name}] needs to deposit")
            deposit_all_items(self)

        
        print(f"[{self.name}] Collecting {skill}")
        collect_highest_unlocked_resource(self, skill)
            

    def complete_monster_tasks_loop(self):
        from enhanced_actions import move_to_location, deposit_all_items

        while True:
            self.load_data()
            # Check for cooldown
            if self.cooldown > 0 and datetime.now() < self.cooldown_expiration:
                time.sleep(self.cooldown)

            # Check for task
            if self.task is None or len(self.task) == 0:
                # get a task from task master
                print(f"[{self.name}] Getting task")
                move_to_location(self, "monsters")
                result = accept_new_task(self.name)
                handle_result_cooldown(result)
                continue
            
            # If progress is finished, complete the task
            if self.task_progress >= self.task_total:
                print(f"[{self.name}] Completing task")
                move_to_location(self, "monsters")
                complete_task(self.name)
                continue

            if self.needs_to_deposit():
                deposit_all_items(self)
                continue
        
            # If above is false
            # Move to location of task
            print(f"[{self.name}] Moving to tasks {self.task}")
            move_to_location(self, self.task)

            if self.needs_rest():
                print(f"[{self.name}] Resting!")
                result = rest(self.name)
                handle_result_cooldown(result)
                
            # attack
            print(f"[{self.name}] attacking {self.task}")
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

    def equip_new_gear(self, slot, new_gear_code):
        from enhanced_actions import deposit_all_items, withdraw_from_bank

        # Unequip old slot
        result = unequip(self.name, slot)
        handle_result_cooldown(result)
        # deposit the gear
        deposit_all_items(self)

        # withdraw the new gear
        withdraw_from_bank(self, new_gear_code, 1)

        # equip new gear
        result = equip(self.name, new_gear_code, slot)
        handle_result_cooldown(result)
import time
import logging
from datetime import datetime
from dataclasses import dataclass, field
from actions import accept_new_task, attack, complete_task, craft, equip, get_character, move, recycle, rest, unequip
from data_classes import InventoryItem
import encyclopedia as ency
from util import handle_result_cooldown
from gear_analyzer import find_best_weapon_for_monster

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

LOW_HP_THRESHOLD = 0.5
LOW_INVENTORY_SPACE_THRESHOLD = 0.9

class Character:
    def __init__(self, name):
        self.logger = logging.getLogger(f"{name}")
        self.logger.info(f"Character {name} created!")
        self.name = name
        self.default_action = "monster tasks"
        self.default_subaction = ""
        self.plan = []

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
            if self.plan != None and len(self.plan) > 0:
                self.execute_action(self.plan.pop(0))
            elif self.default_action == "idle":
                self.logger.info("Performing idle sleep")
                time.sleep(3)
            elif self.default_action == "collecting":
                self.complete_resource_collect(self.default_subaction)
            elif self.default_action == "monster tasks":
                self.complete_monster_tasks()

    def execute_action(self, action):
        from enhanced_actions import withdraw_from_bank, deposit_all_items
        self.load_data()
        if self.cooldown > 0 and datetime.now() < self.cooldown_expiration:
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
            result = craft(self.name, action["code"], action["quantity"])
            handle_result_cooldown(result)
        elif action["action"] == "recycle":
            self.logger.info(f"Recycling {action['code']} x{action['quantity']}")
            result = recycle(self.name, action["code"], action["quantity"])
            handle_result_cooldown(result)

    def execute_plan(self, plan):
        for item in plan:
            self.execute_action(item)
    
    def complete_resource_collect(self, skill):
        from enhanced_actions import collect_highest_unlocked_resource, deposit_all_items
        self.load_data()
        if self.cooldown > 0 and datetime.now() < self.cooldown_expiration:
            time.sleep(self.cooldown)

        if self.needs_to_deposit():
            self.logger.debug(f"needs to deposit")
            deposit_all_items(self)

        self.logger.info(f"Collecting {skill}")
        collect_highest_unlocked_resource(self, skill)
            

    def complete_monster_tasks(self):
        from enhanced_actions import move_to_location, deposit_all_items
        self.load_data()
        # Check for cooldown
        if self.cooldown > 0 and datetime.now() < self.cooldown_expiration:
            time.sleep(self.cooldown)

        # Check for task
        if self.task is None or len(self.task) == 0:
            # get a task from task master
            self.logger.info(f"Getting task")
            move_to_location(self, "monsters")
            result = accept_new_task(self.name)
            handle_result_cooldown(result)
            return
        
        # If progress is finished, complete the task
        if self.task_progress >= self.task_total:
            self.logger.info(f"Completing task")
            move_to_location(self, "monsters")
            complete_task(self.name)
            return

        if self.needs_to_deposit():
            deposit_all_items(self)
            return
    
        best_weapon = find_best_weapon_for_monster(self.task, self.level, self.weapon_slot, self.inventory)
        if best_weapon != False and best_weapon["code"] != self.weapon_slot:
            self.logger.info(f"The {best_weapon['code']} is better against {self.task} than {self.weapon_slot}")
            self.equip_new_gear("weapon", best_weapon["code"])

        # If above is false
        # Move to location of task
        self.logger.info(f"Moving to tasks {self.task}")
        move_to_location(self, self.task)

        if self.needs_rest():
            self.logger.debug(f"Resting!")
            result = rest(self.name)
            handle_result_cooldown(result)
            
        # attack
        self.logger.info(f"attacking {self.task}")
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
        if slot == "ring1":
            return self.ring1_slot
        if slot == "ring2":
            return self.ring2_slot
        if slot == "amulet":
            return self.amulet_slot

    def equip_new_gear(self, slot, new_gear_code):
        from enhanced_actions import deposit_all_items, withdraw_from_bank

        if self.get_active_gear(slot) == new_gear_code:
            self.logger.info(f"{new_gear_code} already active in {slot}")
            return

        # Unequip old slot
        self.logger.info(f"Unequipping {slot}")
        result = unequip(self.name, slot)
        handle_result_cooldown(result)
        # deposit the gear
        self.logger.info(f"Depositing all")
        deposit_all_items(self)

        # withdraw the new gear
        withdraw_from_bank(self, new_gear_code, 1)

        # equip new gear
        self.logger.info(f"equipping {new_gear_code} into {slot}")
        result = equip(self.name, new_gear_code, slot)
        handle_result_cooldown(result)

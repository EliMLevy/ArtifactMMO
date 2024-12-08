package com.elimelvy.artifacts.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.AtomicActions;
import com.elimelvy.artifacts.Bank;
import com.elimelvy.artifacts.GearManager;
import com.elimelvy.artifacts.PlanGenerator.PlanStep;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.item.RecipeIngredient;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.MapTile;
import com.elimelvy.artifacts.model.map.Monster;
import com.elimelvy.artifacts.model.map.Resource;
import com.elimelvy.artifacts.util.HTTPRequester;
import com.elimelvy.artifacts.util.InstantTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Character implements Runnable {

    private final Logger logger;
    private CharacterData data;
    private volatile PlanStep currentTask = new PlanStep("idle", "", 0, "Idle around");
    private final double INVENTORY_FULL_THRESHOLD = 0.9;

    private final BlockingQueue<PlanStep> pendingTasks = new LinkedBlockingQueue<>();

    private final ReentrantLock lock = new ReentrantLock();
    private volatile CountDownLatch depositLatch;

    public Character(CharacterData data) {
        this.data = data;
        this.logger = LoggerFactory.getLogger("Character." + this.data.name);
    }

    @Override
    public String toString() {
        return "Character{" +
                "name='" + data.name + '\'' +
                ", level=" + data.level +
                ", xp=" + data.xp + "/" + data.maxXp +
                ", gold=" + data.gold +
                '}';
    }

    public int getLevel() {
        return this.data.level;
    }

    public String getName() {
        return this.data.name;
    }

    public int getInventoryMaxItems() {
        return this.data.inventoryMaxItems;
    }

    // Method to parse JSON directly
    public static Character fromJson(JsonObject jsonObject) {
        Gson gson = InstantTypeAdapter.createGsonWithInstant();
        return new Character(gson.fromJson(jsonObject.get("data"), CharacterData.class));
    }

    public void updateData(JsonObject jsonObject) {
        if (jsonObject == null) {
            this.logger.error("DATA IS NULL!!!");
            return;
        }
        Gson gson = InstantTypeAdapter.createGsonWithInstant();
        this.data = gson.fromJson(jsonObject, CharacterData.class);
        this.logger.debug("Character data updated!");
    }

    public int getInventoryQuantity(String code) {
        GameItem item = GameItemManager.getInstance().getItem(code);
        int equippedQuantity = 0;
        if(GearManager.allGearTypes.contains(item.type())) {
            // TODO make this work for utilities too
            if(!item.type().equals("ring")) {
                if(code.equals(this.getGearInSlot(item.type() + "_slot"))) {
                    equippedQuantity += 1;
                }
            } else {
                if (code.equals(this.getGearInSlot("ring1_slot"))) {
                    equippedQuantity += 1;
                }
                if (code.equals(this.getGearInSlot("ring2_slot"))) {
                    equippedQuantity += 1;
                }
            }
        }


        for (InventoryItem i : this.data.inventory) {
            if (code.equals(i.getCode())) {
                return equippedQuantity + i.getQuantity();
            }
        }
        return equippedQuantity;
    }

    public void collectResource(String code) {
        // Get resource map
        List<Resource> maps = MapManager.getInstance().getResouce(code);
        if (maps == null || maps.isEmpty()) {
            this.logger.info("Invalid resource code: {}", code);
            return;
        }
        // TODO choose the correct one
        Resource target = this.getClosestMap(maps);
        // Equip the correct tool if we havent already
        switch (target.getSkill()) {
            case "mining" -> this.equipGear("weapon_slot", "iron_pickaxe");
            case "woodcutting" -> this.equipGear("weapon_slot", "iron_axe");
        }
        // Move to the right spot if we arent there already
        this.moveToMap(target.getMapCode());

        // Collect
        JsonObject result = AtomicActions.collect(this.data.name);
        this.handleActionResult(result);
    }

    public void attackMonster(String code) {
        // Get resource map
        List<Monster> maps = MapManager.getInstance().getByMonsterCode(code);
        if (maps == null || maps.isEmpty()) {
            this.logger.info("Invalid monster code: {}", code);
            return;
        }
        // TODO choose the correct one
        Monster target = this.getClosestMap(maps);
        // Equip the correct gear if we havent already
        String selection = GearManager.getBestWeaponAgainstMonster(this, code,
                MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
        this.equipGear("weapon_slot", selection);
        for (String gearType : GearManager.allNonWeaponTypes) {
            if (!gearType.equals("ring")) {
                selection = GearManager.getBestAvailableGearAgainstMonster(this, gearType, code,
                        MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
                this.equipGear(gearType + "_slot", selection);
            } else {
                // There are two ring slots
                selection = GearManager.getBestAvailableGearAgainstMonster(this, gearType + "1", code,
                        MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
                this.equipGear(gearType + "1_slot", selection);
                selection = GearManager.getBestAvailableGearAgainstMonster(this, gearType + "2", code,
                        MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
                this.equipGear(gearType + "2_slot", selection);
            }
        }

        // Move to the right spot if we arent there already
        this.moveToMap(target.getMapCode());

        // Rest if we need to rest
        this.healIfNecessary();

        // Deposit inventory if we need to deposit
        this.depositInventoryIfNecessary();

        // Attack
        this.logger.info("Attacking {}!", code);
        JsonObject result = AtomicActions.attack(this.data.name);
        this.handleActionResult(result);
    }

    private void healIfNecessary() {
        this.logger.info("HP: {}. Max: {}.", this.data.hp, this.data.maxHp);
        if (this.data.hp / this.data.maxHp < 0.5) {
            JsonObject result = AtomicActions.rest(this.data.name);
            handleActionResult(result);
        }
    }

    private void depositInventoryIfNecessary() {
        double currentHolding = this.data.inventory.stream().mapToInt(item -> item.getQuantity()).sum();
        if (currentHolding / (double) this.data.inventoryMaxItems > this.INVENTORY_FULL_THRESHOLD) {
            this.depositInventory();
        }
    }

    public void depositInventory() {
        // Move to bank if necessary
        this.moveToClosestBank();
        // For each item in inv, deposit
        for (InventoryItem item : new ArrayList<>(this.data.inventory)) {
            if (item.getQuantity() > 0) {
                JsonObject result = AtomicActions.depositItem(this.data.name, item.getCode(), item.getQuantity());
                handleActionResult(result);
            }
        }
        // This is useful for synchronizing actions after all characters have deposited.
        // The latch is a one shot use so we can null it out afterwards
        if (this.depositLatch != null) {
            this.depositLatch.countDown();
            this.depositLatch = null;
        }
    }

    public void seDepositLatch(CountDownLatch latch) {
        this.depositLatch = latch;
    }

    private void handleActionResult(JsonObject result) {
        if (result.has("data")) {
            if (result.get("data").getAsJsonObject().has("character")) {
                this.updateData(result.get("data").getAsJsonObject().get("character").getAsJsonObject());
            }
            if (result.get("data").getAsJsonObject().has("bank") && result.has("timestamp")) {
                Bank.getInstance().updateBankContents(result.get("data").getAsJsonObject().get("bank"), result.get("timestamp").getAsLong());
            }
        }

        HTTPRequester.handleResultCooldown(result);
    }

    public void craft(String code, int quantity) {
        this.logger.info("Attempting to craft {} x{}", code, quantity);

        // Get the GameItem
        GameItem item = GameItemManager.getInstance().getItem(code);
        if(item == null) {
            logger.error("Request item not found {}", code);
            return;
        }
        // Ensure that I have all ingredients in my bank
        if (item.recipe() != null) {
            for (RecipeIngredient ingredient : item.recipe().items()) {
                if (ingredient.quantity() > this.getInventoryQuantity(ingredient.code())) {
                    logger.error("I am missing the necessary x{} of {}. I only have x{}", ingredient.quantity(),
                            ingredient.code(), this.getInventoryQuantity(ingredient.code()));
                    return;
                }
            }
        } else {
            logger.error("The requested item is not craftable: {}", code);
            return;
        }
        // Move to the correct workshop
        String skill = item.recipe().skill();
        this.moveToMap(skill);
        // Call atomicactions.craft
        JsonObject result = AtomicActions.craft(this.data.name, code, quantity);
        this.handleActionResult(result);
    }

    public void train(String type) {
        // Type could be: mining, woodcutting, fishing, combat, or lowest
        // TODO
    }

    public void tasks(String type) {
        // Type could be: monsters or items
        // TODO
    }

    /**
     * Equip gear in the specified slot.
     * Unequips and deposits other gear if necessary. Withdraws the specified gear if necessary.
     * @param slot the name of the slot ex. weapon_slot
     * @param code item code
     */
    public void equipGear(String slot, String code) {
        // Check if it is already equipped
        if (this.getGearInSlot(slot) != null && this.getGearInSlot(slot).equals(code)) {
            return;
        }

        this.logger.info("Need to equip {} in {}", code, slot);

        // Unequip if necessary
        if (this.getGearInSlot(slot) != null && !this.getGearInSlot(slot).isEmpty()) {
            this.logger.info("To equip {} we need to unequip {}", code, this.getGearInSlot(slot));
            JsonObject result = AtomicActions.unequip(this.data.name, slot.replace("_slot", ""));
            handleActionResult(result);
        }

        // Withdraw item if necessary
        if (this.getInventoryQuantity(code) == 0) {
            // Withdraw it from the bank
            this.logger.info("{} not found in inventory so I need to withdraw it", code);
            if (Bank.getInstance().getBankQuantity(code) > 0) {
                this.depositInventory();
                this.withdrawFromBank(code, 1);
            } else {
                logger.warn("Attempted to equip gear that is not available: {}", code);
            }
        }
        // If the withdrawal was successful or if we already had it, equip
        if (this.getInventoryQuantity(code) > 0) {
            this.logger.info("Equipping {} into {}", code, slot);
            JsonObject result = AtomicActions.equip(this.data.name, code, slot.replace("_slot", ""));
            handleActionResult(result);
        } else {
            this.logger.warn("I was expecting to have {} in my inventory but found none", code);
        }
    }

    public void withdrawFromBank(String code, int quantity) {
        moveToClosestBank();

        JsonObject result = AtomicActions.withdrawItem(this.data.name, code, quantity);
        handleActionResult(result);
    }

    private void moveToClosestBank() {
        List<MapTile> banks = MapManager.getInstance().getMap("bank");
        MapTile closestBank = getClosestMap(banks);
        if (this.data.x != closestBank.getX() || this.data.y != closestBank.getY()) {
            JsonObject result = AtomicActions.move(this.data.name, closestBank.getX(), closestBank.getY());
            this.handleActionResult(result);
        }
    }

    private <T extends MapTile> T getClosestMap(List<T> maps) {
        T closestMap = maps.get(0);
        int closestDist = Integer.MAX_VALUE;
        for (T m : maps) {
            int dist = Math.abs(m.getX() - this.data.x) + Math.abs(m.getY() - this.data.y);
            if (dist < closestDist) {
                closestMap = m;
                closestDist = dist;
            }
        }
        return closestMap;
    }

    public String getGearInSlot(String slot) {
        return switch (slot) {
            case "weapon_slot" -> this.data.weaponSlot;
            case "shield_slot" -> this.data.shieldSlot;
            case "helmet_slot" -> this.data.helmetSlot;
            case "body_armor_slot" -> this.data.bodyArmorSlot;
            case "leg_armor_slot" -> this.data.legArmorSlot;
            case "boots_slot" -> this.data.bootsSlot;
            case "ring1_slot" -> this.data.ring1Slot;
            case "ring2_slot" -> this.data.ring2Slot;
            case "amulet_slot" -> this.data.amuletSlot;
            default -> null;
        };
    }

    public void moveToMap(String mapCode) {
        List<MapTile> targets = MapManager.getInstance().getMap(mapCode);
        if (!targets.isEmpty()) {
            MapTile target = this.getClosestMap(targets);
            if (this.data.x != target.getX() || this.data.y != target.getY()) {
                JsonObject response = AtomicActions.move(this.data.name, target.getX(), target.getY());
                handleActionResult(response);
            }
        } else {
            this.logger.warn("Tried to move to invalid map: {}", mapCode);
        }
    }

    @Override
    public void run() {
        while (true) {
            PlanStep task;
            this.lock.lock();
            try {
                task = this.currentTask;
            } finally {
                this.lock.unlock();
            }
            // TODO if we have an active cooldown, sleep

            if(!task.action().equals("idle")) {
                this.logger.info("Doing task: {}. {}", task.action(), task.description());
            }

            if (this.pendingTasks.peek() != null) {
                this.doTask(this.pendingTasks.poll());
            } else {
                this.doTask(this.currentTask);
            }


        }
    }

    public void doTask(PlanStep task) {
        switch (task.action()) {
            case "idle" -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    logger.warn("I have been interupted while doing an idle sleep!");
                }
            }
            case "attack" -> this.attackMonster(task.code());
            case "craft" -> this.craft(task.code(), task.quantity());
            case "collect" -> this.collectResource(task.code());
            case "train" -> this.train(task.code());
            case "tasks" -> this.tasks(task.code());
            case "deposit" -> this.depositInventory();
            case "withdraw" -> this.withdrawFromBank(task.code(), task.quantity());
            default -> {
                logger.error("Unknown task step: {}. Description: {}", task.action(), task.description());
            }
        }
    }


    public void setTask(PlanStep task) {
        this.lock.lock();
        try {
            this.logger.info("Setting task to {}", task);
            this.currentTask = task;
        } finally {
            this.lock.unlock();
        }
    }

    public void addTaskToQueue(PlanStep task) {
        this.pendingTasks.add(task);
    }

    public void addTasksToQueue(List<PlanStep> tasks) {
        this.pendingTasks.addAll(tasks);
    }
}
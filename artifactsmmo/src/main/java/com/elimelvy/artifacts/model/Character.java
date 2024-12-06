package com.elimelvy.artifacts.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.AtomicActions;
import com.elimelvy.artifacts.Bank;
import com.elimelvy.artifacts.GearManager;
import com.elimelvy.artifacts.model.item.GameItemManager;
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
    private String currentTask = "idle";
    private String currentTaskParam1 = "";
    private int currentTaskParam2= 0;
    private final double INVENTORY_FULL_THRESHOLD = 0.9;

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

    // Method to parse JSON directly
    public static Character fromJson(JsonObject jsonObject) {
        Gson gson = InstantTypeAdapter.createGsonWithInstant();
        return new Character(gson.fromJson(jsonObject.get("data"), CharacterData.class));
    }

    public void updateData(JsonObject jsonObject) {
        if(jsonObject == null) {
            this.logger.error("DATA IS NULL!!!");
            return;
        }
        Gson gson = InstantTypeAdapter.createGsonWithInstant();
        this.data = gson.fromJson(jsonObject, CharacterData.class);
        this.logger.debug("Character data updated!");
    }

    public int getInventoryQuantity(String code) {
        for (InventoryItem i : this.data.inventory) {
            if (code.equals(i.getCode())) {
                return i.getQuantity();
            }
        }
        return 0;
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
            case "mining" -> this.equipGear("weapon", "pickaxe");
            case "woodcutting" -> this.equipGear("weapon", "axe");
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
        if(currentHolding / (double)this.data.inventoryMaxItems > this.INVENTORY_FULL_THRESHOLD) {
            this.depositInventory();
        }
    }

    public void depositInventory() {
        // Move to bank if necessary
        this.moveToClosestBank();
        // For each item in inv, deposit
        for(InventoryItem item : new ArrayList<>(this.data.inventory)) {
            if(item.getQuantity() > 0) {
                JsonObject result = AtomicActions.depositItem(this.data.name, item.getCode(), item.getQuantity());
                handleActionResult(result);
            }
        }
        // This is useful for synchronizing actions after all characters have deposited.
        // The latch is a one shot use so we can null it out afterwards
        if(this.depositLatch != null) {
            this.depositLatch.countDown();
            this.depositLatch = null;
        }
    }

    public void seDepositLatch(CountDownLatch latch) {
        this.depositLatch = latch;
    }

    private void handleActionResult(JsonObject result) {
        if (result.has("data")) {
            if(result.get("data").getAsJsonObject().has("character")) {
                this.updateData(result.get("data").getAsJsonObject().get("character").getAsJsonObject());
            }
            if ( result.get("data").getAsJsonObject().has("bank")) {
                Bank.getInstance().updateBankContents(result.get("data").getAsJsonObject().get("bank"));
            }
        }

        HTTPRequester.handleResultCooldown(result);
    }

    public void train(String type) {
        // Type could be: mining, woodcutting, fishing, combat, or lowest
        // TODO
    }

    public void tasks(String type) {
        // Type could be: monsters or items
        // TODO
    }

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
        if(this.data.x != closestBank.getX() || this.data.y != closestBank.getY()) {
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
            String task;
            String param1;
            this.lock.lock();
            try {
                task = this.currentTask;
                param1 = this.currentTaskParam1;
            } finally {
                this.lock.unlock();
            }
            this.logger.info("Doing task: {} {}", task, param1);

            // TODO if we have an active cooldown, sleep
            switch (task) {
                case "idle" -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        logger.warn("I have been interupted while doing an idle sleep!");
                    }
                }
                case "attack" -> this.attackMonster(param1);
                case "craft" -> {
                    this.logger.info("Attempting to craft {} x{}", this.currentTaskParam1, this.currentTaskParam2);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        logger.warn("I have been interupted while doing an idle sleep!");
                    }
                }
                case "collect" -> this.collectResource(param1);
                case "train" -> this.train(param1);
                case "tasks" -> this.tasks(param1);
                case "deposit" -> {
                    this.depositInventory();
                    this.setTask("idle");
                }
                default -> {
                }
            }

        }
    }

    public void setTask(String task) {
        this.setTask(task, "", 0);
    }

    public void setTask(String task, String param1) {
        this.setTask(task, param1, 0);
    }

    public void setTask(String task, String param1, int param2) {
        this.lock.lock();
        try {
            this.logger.info("Setting task to {} {}", task, param1);
            this.currentTask = task;
            this.currentTaskParam1 = param1;
            this.currentTaskParam2 = param2;
        } finally {
            this.lock.unlock();
        }
    }
}
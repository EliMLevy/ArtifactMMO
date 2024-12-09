package com.elimelvy.artifacts;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.PlanGenerator.PlanStep;
import com.elimelvy.artifacts.model.CharacterData;
import com.elimelvy.artifacts.model.CharacterStatSimulator;
import com.elimelvy.artifacts.model.InventoryItem;
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
    private volatile PlanStep currentTask = new PlanStep(PlanAction.IDLE, "", 0, "Initial Idling");
    private final double INVENTORY_FULL_THRESHOLD = 0.9;

    private final BlockingQueue<PlanStep> pendingTasks = new LinkedBlockingQueue<>();

    private final ReentrantLock lock = new ReentrantLock();
    private volatile CountDownLatch depositLatch;

    private final AtomicBoolean interuptLongAction = new AtomicBoolean(false);

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

    public int getMaxHp() {
        return this.data.maxHp;
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
        if (GearManager.allGearTypes.contains(item.type())) {
            // TODO make this work for utilities too
            if (!item.type().equals("ring")) {
                if (code.equals(this.getGearInSlot(item.type() + "_slot"))) {
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

        return equippedQuantity + this.getInventoryQuantityWithoutEquipped(code);
    }

    public int getInventoryQuantityWithoutEquipped(String code) {
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
            this.logger.warn("Invalid resource code: {}", code);
            return;
        }
        Resource target = this.getClosestMap(maps);
        // Equip the correct tool if we havent already
        switch (target.getSkill()) {
            case "mining" -> this.equipGear("weapon_slot", "iron_pickaxe");
            case "woodcutting" -> this.equipGear("weapon_slot", "iron_axe");
        }
        // If we dont have the required level, train this skill
        if(target.getSkill().equals("woodcutting") && target.getLevel() > this.data.woodcuttingLevel) {
            this.train("woodcutting");
            return;
        } else if(target.getSkill().equals("mining") && target.getLevel() > this.data.miningLevel) {
            this.train("mining");
            return;
        } else if (target.getSkill().equals("fishing") && target.getLevel() > this.data.fishingLevel) {
            this.train("fishing");
            return;
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
            this.logger.warn("Invalid monster code: {}", code);
            return;
        }
        Monster target = this.getClosestMap(maps);
        // Make sure I can defeat this monster, otherwise train combat
        CharacterStatSimulator simulator = new CharacterStatSimulator(this);
        simulator.optimizeForMonster(target.getMapCode(), MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
        if(!simulator.getPlayerWinsAgainstMonster(target.getMapCode())) {
            this.logger.info("Can't defeat {} so Im going to train combat", target.getMapCode());
            this.trainCombat();
            return;
        } else {
            this.logger.info("I can defeat {} with this loadout {}", target.getMapCode(), simulator.getLoadout());
        }
        // Equip the correct gear if we havent already
        String selection = GearManager.getBestWeaponAgainstMonster(this, code,
                MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
        this.equipGear("weapon_slot", selection);
        for (String gearType : GearManager.allNonWeaponTypes) {
            if (!gearType.equals("ring")) {
                selection = GearManager.getBestAvailableGearAgainstMonster(this, this.getGearInSlot("weapon_slot"),
                        gearType, code,
                        MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
                this.equipGear(gearType + "_slot", selection);
            } else {
                // There are two ring slots
                selection = GearManager.getBestAvailableGearAgainstMonster(this, this.getGearInSlot("weapon_slot"),
                        gearType + "1", code,
                        MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
                this.equipGear(gearType + "1_slot", selection);
                selection = GearManager.getBestAvailableGearAgainstMonster(this, this.getGearInSlot("weapon_slot"),
                        gearType + "2", code,
                        MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
                this.equipGear(gearType + "2_slot", selection);
            }
        }

        // Deposit inventory if we need to deposit
        this.depositInventoryIfNecessary();

        // Fill up on consumables if necessary
        List<String> candidateConsumables = List.of("cooked_wolf_meat", "cooked_chicken");
        int targetQuantity = 25;
        for(String food : candidateConsumables) {
            if(this.getInventoryQuantity(food) == 0 && Bank.getInstance().getBankQuantity(food) >= targetQuantity) {
                this.withdrawFromBank(food, Math.min(Bank.getInstance().getBankQuantity(food), targetQuantity));
            } 
        }

        // Move to the right spot if we arent there already
        this.moveToMap(target.getMapCode());

        // Rest if we need to rest
        this.healIfNecessary();

        // Attack
        this.logger.info("Attacking {}!", code);
        JsonObject result = AtomicActions.attack(this.data.name);
        this.handleActionResult(result);
    }

    private void healIfNecessary() {
        // Attempt to use consumables first
        // Find consumables in our inventory
        List<GameItem> consumables = new LinkedList<>();
        for (InventoryItem i : this.data.inventory) {
            GameItem item = GameItemManager.getInstance().getItem(i.getCode());
            if (item != null && item.type().equals("consumable")) {
                consumables.add(item);
            }
        }
        // Find the largest gain that doesnt go over our max hp
        consumables.sort((a, b) -> {
            return (int) (GearManager.getEffectValue(b, "heal") - GearManager.getEffectValue(a, "heal"));
        });
        for (GameItem food : consumables) {
            // If the healing abilities is less than the amount we need, eat a bunch of it
            double healAmount = GearManager.getEffectValue(food, "heal");
            int eatAmount = Math.min((int)Math.floor((this.data.maxHp - this.data.hp) / healAmount), this.getInventoryQuantity(food.code()));
            if(eatAmount > 0) {
                JsonObject result = AtomicActions.useItem(this.data.name, food.code(), eatAmount);
                this.handleActionResult(result);
            }
        }

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

    public void setDepositLatch(CountDownLatch latch) {
        this.depositLatch = latch;
    }

    private void handleActionResult(JsonObject result) {
        if(result == null) {
            logger.warn("Result that got passed in was null");
            return;
        }
        if (result.has("data")) {
            if (result.get("data").getAsJsonObject().has("character")) {
                this.updateData(result.get("data").getAsJsonObject().get("character").getAsJsonObject());
            }
            if (result.get("data").getAsJsonObject().has("bank") && result.has("timestamp")) {
                Bank.getInstance().updateBankContents(result.get("data").getAsJsonObject().get("bank"),
                        result.get("timestamp").getAsLong());
            }
        }

        HTTPRequester.handleResultCooldown(result);
    }

    public void craft(String code, int quantity) {
        this.logger.info("Attempting to craft {} x{}", code, quantity);

        // Get the GameItem
        GameItem item = GameItemManager.getInstance().getItem(code);
        if (item == null) {
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
        if(type == null) {
            this.logger.error("Cant train a null skill");
            return;
        }
        if (type.equals("combat")) {
            this.trainCombat();
            return;
        } else if(type.equals("lowest")) {
            type = "mining";
            if(this.data.woodcuttingLevel < this.data.miningLevel && this.data.woodcuttingLevel < this.data.fishingLevel) {
                type = "woodcutting";
            } else if(this.data.fishingLevel < this.data.miningLevel && this.data.fishingLevel < this.data.woodcuttingLevel) {
                type = "fishing";
            }
        }

        List<Resource> maps = MapManager.getInstance().getMapsBySkill(type);
        if(maps == null || maps.isEmpty()) {
            this.logger.warn("Tried training a skill that doesnt have maps. {}", type);
            return;
        }

        int level = switch (type) {
            case "mining" -> this.data.miningLevel;
            case "woodcutting" -> this.data.woodcuttingLevel;
            case "fishing" -> this.data.fishingLevel;
            default -> 0;
        };

        Resource highestUnlocked = null;
        for (Resource r : maps) {
            if(r.getLevel() <= level && (highestUnlocked == null || r.getLevel() > highestUnlocked.getLevel())) {
                highestUnlocked = r;
            }
        }
        if(highestUnlocked != null) {
            this.logger.info("Training {} by collecting {}", type, highestUnlocked.getResourceCode());
            this.collectResource(highestUnlocked.getResourceCode());
        } else {
            this.logger.error("Cant train {} becuase there are no places to train", type);
        }
    }

    private void trainCombat() {
        // Get all monsters up the current level (no more than 10 less than current)
        // Sort in descending ordre of level
        // Find the first one we can defeat and battle him
        Monster target = this.getHighestMonsterDefeatable();
        this.attackMonster(target.getMapCode());
    }

    public Monster getHighestMonsterDefeatable() {
        List<Monster> monsters = MapManager.getInstance().getMonstersByLevel(this.getLevel() - 10, this.getLevel());
        if(monsters == null || monsters.isEmpty()) {
            logger.warn("Cant find any monsters on my level. level: {}", this.getLevel());
            return null;
        }
        // Sort in descending ordre of level
        monsters = new ArrayList<>(monsters); // Copy over list so that we can sort it. Otherwise unsupported operation
        monsters.sort((a, b) -> b.getLevel() - a.getLevel());
        // Find the first one we can defeat and battle him
        for (Monster m : monsters) {
            CharacterStatSimulator simulator = new CharacterStatSimulator(this);
            simulator.optimizeForMonster(m.getContentCode(), MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
            if(simulator.getPlayerWinsAgainstMonster(m.getContentCode())) {
                return m;
            } 
        }
        return null;
    }

    public void tasks(String type) {
        // Type could be: monsters or items
        // If we have no task, move to task spot and get new task
        if (this.data.task == null || this.data.task.isEmpty()) {
            this.logger.info("Getting new {} task", type);
            this.moveToMap(type);
            JsonObject result = AtomicActions.acceptNewTask(this.data.name);
            this.handleActionResult(result);
            return;
        }
        // If we are done with our tasks, move to task spot and complete tasks
        if (this.data.taskProgress >= this.data.taskTotal) {
            this.logger.info("Done with task {}", this.data.task);
            String completedTaskType = this.data.taskType;
            this.moveToMap(completedTaskType);
            JsonObject result = AtomicActions.completeTask(this.data.name);
            this.handleActionResult(result);
            // If there is a bunch of task coins, withdraw them and exchange them
            int taskCoins = Bank.getInstance().getBankQuantity("tasks_coin") + this.getInventoryQuantity("tasks_coin");
            if(taskCoins > 30) {
                withdrawFromBank("tasks_coin", Bank.getInstance().getBankQuantity("tasks_coin"));
                this.moveToMap(completedTaskType); // The task type we just completed is the closest task master
                while(this.getInventoryQuantity("tasks_coin") >= 6) {
                    result = AtomicActions.exchangeCoinsWithTaskMaster(this.data.name);
                    this.handleActionResult(result);
                }
                this.depositInventory();
            }
            return;
        }

        // If inv full, deposit
        this.depositInventoryIfNecessary();

        // Do task
        if (this.data.taskType.equals("items")) {
            this.doItemTask();
        } else {
            this.attackMonster(this.data.task);
        }
    }

    private void doItemTask() {

        // Withdraw as much from the bank as I can and trade it
        int withdrawAmount = Math.min(this.data.inventoryMaxItems, Bank.getInstance().getBankQuantity(this.data.task));
        withdrawAmount = Math.min(withdrawAmount, this.data.taskTotal - this.data.taskProgress);
        if (withdrawAmount > 0) {
            this.logger.info("Trading {} {} for a task", withdrawAmount, this.data.task);
            this.depositInventory();
            this.withdrawFromBank(this.data.task, withdrawAmount);
            this.moveToMap("items");
            JsonObject result = AtomicActions.tradeWithTaskMaster(this.data.name, this.data.task, withdrawAmount);
            this.handleActionResult(result);
            return;
        }

        // If we have enough in my inventory to complete the task, complete it
        if (this.getInventoryQuantity(this.data.task) >= this.data.taskTotal - this.data.taskProgress) {
            this.moveToMap("items");
            JsonObject result = AtomicActions.tradeWithTaskMaster(this.data.name, this.data.task,
                    this.data.taskTotal - this.data.taskProgress);
            this.handleActionResult(result);
            return;
        }

        // Get gameItem
        GameItem target = GameItemManager.getInstance().getItem(this.data.task);
        if (target.recipe() != null && target.recipe().items() != null && !target.recipe().items().isEmpty()) {
            // If it has a recipe, generate plan to get it
            this.depositInventory();
            List<PlanStep> plan = PlanGenerator.generatePlan(this.data.task,
                    this.data.taskTotal - this.data.taskProgress, (int) (this.data.inventoryMaxItems * 0.9));
            this.logger.info("Plan to gather {} {}: {}", this.data.taskTotal - this.data.taskProgress, target.code(),
                    plan);
            this.addTasksToQueue(plan);
        } else {
            // Otherwise, find where to collect it
            List<Resource> resources = MapManager.getInstance().getResouce(this.data.task);
            if (resources != null && !resources.isEmpty()) {
                moveToMap(resources.get(0).getMapCode());
                this.collectResource(this.data.task);
            } else {
                // Otherwise, log an error, go into idle
                this.logger.error("Unable to collect {}", this.data.task);
                this.setTask(new PlanStep(PlanAction.IDLE, "", 0, "Failed to complete task so moving into idle"));
            }

        }

    }

    /**
     * Equip gear in the specified slot.
     * Unequips and deposits other gear if necessary. Withdraws the specified gear
     * if necessary.
     * 
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
            this.healIfNecessary();
            JsonObject result = AtomicActions.unequip(this.data.name, slot.replace("_slot", ""));
            handleActionResult(result);
        }

        // Withdraw item if necessary
        if (this.getInventoryQuantityWithoutEquipped(code) == 0) {
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
        if (this.getInventoryQuantityWithoutEquipped(code) > 0) {
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
            if(this.data.cooldown > 0 && Instant.now().isBefore(this.data.cooldownExpiration)) {
                try {
                    Duration d = Duration.between(this.data.cooldownExpiration, Instant.now()).abs();
                    this.logger.info("Sleeping off cooldown {} sec", d.toSeconds());
                    Thread.sleep(d.toMillis());
                } catch (InterruptedException e) {
                    logger.error("Interuppted from sleep!");
                    continue;
                }
            }

            if (task.action() != PlanAction.IDLE) {
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
            case IDLE -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    logger.warn("I have been interupted while doing an idle sleep!");
                }
            }
            case ATTACK -> this.attackMonster(task.code());
            case CRAFT -> this.craft(task.code(), task.quantity());
            case COLLECT -> {
                for (int i = 0; i < task.quantity() && !interuptLongAction.get(); i++) {
                    this.collectResource(task.code());
                }
            }
            case TRAIN -> this.train(task.code());
            case TASKS -> this.tasks(task.code());
            case DEPOSIT -> this.depositInventory();
            case WITHDRAW -> {
                this.withdrawFromBank(task.code(), task.quantity());
            }
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
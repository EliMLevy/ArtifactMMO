package com.elimelvy.artifacts;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.ArtifactCharacter.CharacterCombatService;
import com.elimelvy.artifacts.ArtifactCharacter.CharacterGearService;
import com.elimelvy.artifacts.ArtifactCharacter.CharacterInventoryService;
import com.elimelvy.artifacts.ArtifactCharacter.CharacterMovementService;
import com.elimelvy.artifacts.ArtifactCharacter.CharacterTaskService;
import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.model.CharacterData;
import com.elimelvy.artifacts.model.PlanStep;
import com.elimelvy.artifacts.model.item.GameItem;
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
    private volatile PlanStep currentTask = new PlanStep(PlanAction.IDLE, "", 0, "Initial Idling");

    private final BlockingQueue<PlanStep> pendingTasks = new LinkedBlockingQueue<>();
    private final BlockingQueue<PlanStep> pausedTasks = new LinkedBlockingQueue<>();

    private final ReentrantLock lock = new ReentrantLock();

    public final CharacterInventoryService inventoryService;
    public final CharacterMovementService movementService;
    public final CharacterGearService gearService;
    public final CharacterCombatService combatService;
    public final CharacterTaskService taskService;
    private final AtomicBoolean isInterupted = new AtomicBoolean(false);

    public Character(CharacterData data) {
        this.data = data;
        this.logger = LoggerFactory.getLogger("Character." + this.data.name);
        this.movementService = new CharacterMovementService(this);
        this.gearService = new CharacterGearService(this);
        this.inventoryService = new CharacterInventoryService(this);
        this.combatService = new CharacterCombatService(this);
        this.taskService = new CharacterTaskService(this);
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

    public CharacterData getData() {
        return this.data;
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

    /**
     * 
     * @param code the resource code to collect ex. ash_wood, coal
     * @return true if the resource was successfully collected, false if we needed
     *         to train
     */
    private boolean collectResource(String code) {
        this.inventoryService.depositAllItemsIfNecessary(movementService);

        // Get resource map
        List<MapTile> maps = MapManager.getInstance().getMapByResource(code);
        Resource resource = MapManager.getInstance().getResouce(code);
        if(resource == null) { // If the drop was passed in
            resource = MapManager.getInstance().getResourceByDrop(code);
        }
        if(maps == null && resource != null) {
            maps = MapManager.getInstance().getMap(resource.getCode());
        }
        if (maps == null || maps.isEmpty() || resource == null) {
            this.logger.warn("Invalid resource code: {}. {} {}", code, maps, resource);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
            return false;
        }
        MapTile target = movementService.getClosestMap(maps);
        String skill = resource.getSkill();
        int level = resource.getLevel();
        // Equip the correct tool if we havent already
        switch (skill) {
            case "mining" ->
                gearService.equipGear("weapon_slot", "gold_pickaxe", inventoryService, movementService, combatService);
            case "woodcutting" ->
                gearService.equipGear("weapon_slot", "gold_axe", inventoryService, movementService, combatService);
            case "fishing" ->
                gearService.equipGear("weapon_slot", "gold_fishing_rod", inventoryService, movementService, combatService);

        }
        // If we dont have the required level, train this skill
        if (skill.equals("woodcutting") && level > this.data.woodcuttingLevel) {
            while (level > this.data.woodcuttingLevel && !this.isInterupted.get()) {
                this.train("woodcutting");
            }
        } else if (skill.equals("mining") && level > this.data.miningLevel) {
            while (level > this.data.miningLevel && !this.isInterupted.get()) {
                this.train("mining");
            }
        } else if (skill.equals("fishing") && level > this.data.fishingLevel) {
            while (level > this.data.fishingLevel && !this.isInterupted.get()) {
                this.train("fishing");
            }
        }

        if(this.isInterupted.get()) {
            this.logger.info("Interupted! returning from collect routine!");
            return false;
        }

        // Move to the right spot if we arent there already
        movementService.moveToMap(target.getContentCode());

        // Collect
        JsonObject result = AtomicActions.collect(this.data.name, resource.getCode());
        this.handleActionResult(result);
        return true;
    }

    public void craft(String code, int quantity) {
        // Get the GameItem
        GameItem item = GameItemManager.getInstance().getItem(code);
        if (!inventoryService.hasIngredientsForCrafting(item, gearService)) {
            this.logger.warn("I dont have the ingredients to craft {}.", code);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
            return;
        }
        this.logger.info("Attempting to craft {} x{}", code, quantity);
        // Move to the correct workshop
        String skill = item.craft().skill();
        movementService.moveToMap(skill);
        // Call atomicactions.craft
        JsonObject result = AtomicActions.craft(this.data.name, code, quantity);
        this.handleActionResult(result);
    }

    public void recycle(String code, int quantity) {
        // Get the GameItem
        GameItem item = GameItemManager.getInstance().getItem(code);
        if (inventoryService.getInventoryQuantity(code, gearService) < quantity) {
            this.logger.warn("I dont have enough to recyle {} x{}.", code, quantity);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
            return;
        }
        this.logger.info("Attempting to recycle {} x{}", code, quantity);
        // Move to the correct workshop
        if(item.craft() != null) {
            String skill = item.craft().skill();
            movementService.moveToMap(skill);
            // Call atomicactions.craft
            JsonObject result = AtomicActions.recycle(this.data.name, code, quantity);
            this.handleActionResult(result);
        } else {
            this.logger.warn("{} is not recycle-able.", code);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
        }
    }

    public void train(String type) {
        if (type == null) {
            this.logger.error("Cant train a null skill");
            return;
        }
        if (type.equals("combat")) {
            combatService.trainCombat(movementService, gearService, inventoryService);
            return;
        } else if (type.equals("lowest")) {
            type = "mining";
            if (this.data.woodcuttingLevel < this.data.miningLevel
                    && this.data.woodcuttingLevel < this.data.fishingLevel) {
                type = "woodcutting";
            } else if (this.data.fishingLevel < this.data.miningLevel
                    && this.data.fishingLevel < this.data.woodcuttingLevel) {
                type = "fishing";
            }
        }

        List<Resource> maps = MapManager.getInstance().getMapsBySkill(type);
        if (maps == null || maps.isEmpty()) {
            this.logger.warn("Tried training a skill that doesnt have maps. {}", type);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
            return;
        }

        int level = 0;
        int xp = 0;
        int maxXp = 0;
        switch (type) {
            case "mining" -> {
                level = this.data.miningLevel;
                xp = this.data.miningXp;
                maxXp = this.data.miningMaxXp;
            }
            case "woodcutting" -> {
                level = this.data.woodcuttingLevel;
                xp = this.data.woodcuttingXp;
                maxXp = this.data.woodcuttingMaxXp;
            }
            case "fishing" -> {
                level = this.data.fishingLevel;
                xp = this.data.fishingXp;
                maxXp = this.data.fishingMaxXp;
            }
        }

        Resource highestUnlocked = null;
        for (Resource r : maps) {
            if (r.getLevel() <= level && (highestUnlocked == null || r.getLevel() > highestUnlocked.getLevel())) {
                highestUnlocked = r;
            }
        }
        if (highestUnlocked != null) {
            this.logger.info("Training {} by collecting {}. Level: {}. XP: {}/{}.", type,
                    highestUnlocked.getCode(), level, xp, maxXp);
            this.collectResource(highestUnlocked.getCode());
        } else {
            this.logger.error("Cant train {} becuase there are no places to train", type);
        }
    }

    public void handleActionResult(JsonObject result) {
        if (result == null) {
            logger.warn("Result that got passed in was null");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
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

    @Override
    public void run() {
        while (true) {
            if (this.pendingTasks.peek() != null) {
                PlanStep step = this.pendingTasks.poll();
                this.logger.debug("Removing task from queue: {} {}. {}", step.action, step.code, step.description);
                this.doTask(step);
                step.completeStep();
            } else {
                if (this.currentTask.action != PlanAction.IDLE) {
                    this.logger.debug("Doing task: {} {}. {}", this.currentTask.action, this.currentTask.code,
                            this.currentTask.description);
                }
                this.doTask(this.currentTask);
                this.currentTask.completeStep();
            }

        }
    }

    public void doTask(PlanStep task) {
        if (this.data.cooldown > 0 && Instant.now().isBefore(this.data.cooldownExpiration)) {
            try {
                Duration d = Duration.between(this.data.cooldownExpiration, Instant.now()).abs();
                this.logger.info("Sleeping off cooldown {} sec. {} ms", d.toSeconds(), d.toMillis());
                Thread.sleep(d.toMillis() + 1000);
            } catch (InterruptedException e) {
                logger.error("Interuppted from sleep!");
                return;
            }
        }

        switch (task.action) {
            case IDLE -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    logger.warn("I have been interupted while doing an idle sleep!");
                }
            }
            case ATTACK -> combatService.attackMonster(task.code, movementService, gearService, inventoryService);
            case GET_DROP -> {
                // If we have enoug in inv, return
                if(this.inventoryService.getInventoryQuantity(task.code, gearService) >= task.quantity) {
                    break;
                }
                // If we have enough in bank + inv, withdraw
                if(this.inventoryService.getInventoryQuantity(task.code, gearService) + Bank.getInstance().getBankQuantity(task.code) >= task.quantity) {
                    int quantity = task.quantity - this.inventoryService.getInventoryQuantity(task.code, gearService);
                    this.inventoryService.withdrawFromBank(task.code, quantity, movementService);
                    break;
                }
                // Attack until we have enough in inv + bank
                while(this.inventoryService.getInventoryQuantity(task.code, gearService) < task.quantity) {
                    if (this.isInterupted.get()) {
                        this.logger.info("Interupted! stopping getting drops");
                        this.isInterupted.set(false);
                        break;
                    }
                    if(MapManager.getInstance().isMonsterDrop(task.code)) {
                        Monster m = MapManager.getInstance().getMonsterByDrop(task.code);
                        this.combatService.attackMonster(m.getCode(), movementService, gearService, inventoryService);
                    } else {
                        this.collectResource(task.code);
                    }
                }
            }
            case EVENT -> combatService.attackMonster(task.code, movementService, gearService, inventoryService);
            case CRAFT -> this.craft(task.code, task.quantity);
            case RECYCLE -> this.recycle(task.code, task.quantity);
            case COLLECT -> {
                // This is necessary because plan generation requires that we respect the
                // quantity parameter
                // of the collect action.
                // This is also being used for task completion.
                for (int i = 0; i < task.quantity; i++) {
                    if (this.isInterupted.get()) {
                        this.logger.info("Interupted! stopping collecting");
                        this.isInterupted.set(false);
                        break;
                    }
                    this.logger.debug("Collecting: {} ({}/{})", task.code, i, task.quantity);
                    this.collectResource(task.code);
                }
            }
            case TRAIN -> this.train(task.code);
            case TASKS -> taskService.tasks(task.code, movementService, inventoryService, gearService, combatService);
            case DEPOSIT -> inventoryService.depositAllItems(movementService);
            case WITHDRAW -> {
                inventoryService.withdrawFromBank(task.code, task.quantity, movementService);
            }
            default -> {
                logger.error("Unknown task step: {}. Description: {}", task.action, task.description);
            }
        }
        if(this.isInterupted.get()) {
            this.isInterupted.set(false);
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

    public void interuptCharacter(PlanStep task) {
        // This will stop the character if he is doing long running task
        // and to empty his task queue.
        this.pendingTasks.clear();
        this.pendingTasks.add(task);
        this.isInterupted.set(true);
    }

    public void interuptCharacter(List<PlanStep> tasks) {
        // This will stop the character if he is doing long running task
        // and to empty his task queue.
        this.pendingTasks.clear();
        this.pendingTasks.addAll(tasks);
        this.isInterupted.set(true);
    }

    public void interuptCharacter() {
        // This will stop the character if he is doing long running task
        // and to empty his task queue.
        this.pendingTasks.clear();
        this.isInterupted.set(true);
    }

    public void pausePendingTasks() {
        this.pausedTasks.clear();
        this.pausedTasks.addAll(this.pendingTasks);
        this.pendingTasks.clear();
    }
    public void resumePausedTasks() {
        this.pendingTasks.addAll(pendingTasks);
    }



    public PlanStep getCurrentTask() {
        return this.currentTask;
    }
}
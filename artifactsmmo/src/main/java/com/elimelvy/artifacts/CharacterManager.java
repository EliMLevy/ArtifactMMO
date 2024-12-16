package com.elimelvy.artifacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.crafting.CraftingManager;
import com.elimelvy.artifacts.crafting.GearCraftingFilter;
import com.elimelvy.artifacts.crafting.GearCraftingSorter;
import com.elimelvy.artifacts.model.OwnershipQuantity;
import com.elimelvy.artifacts.model.PlanStep;
import com.elimelvy.artifacts.model.item.GameItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CharacterManager implements OwnershipQuantity, Runnable {

    private final Map<String, Character> characters = new HashMap<>();
    private final List<Thread> threads = new ArrayList<>(5);
    private final String weaponCrafter = "Joe";
    private final String armorCrafter = "Joe";
    private final String jewelryCrafter = "Joe";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    private final Logger logger = LoggerFactory.getLogger(CharacterManager.class);

    private enum Goal {
        CRAFTING_NEW_GEAR, // Keep track of a list of gear, keep track of craftable gear
        UPGRADING_COMBAT_LEVEL, // Attack the highest monster that we can beat
        UPGRADING_CRAFTING_LEVEL, // Find the cheapest item at the highest level unlocked
    }

    private final Set<GameItem> unlockedGear = new HashSet<>();
    private final Queue<String> gearToCraft = new LinkedList<>();
    private String currentlyCrafting;
    private int currentCraftingQuantity;
    private Goal currentGoal = Goal.CRAFTING_NEW_GEAR;
    private CraftingManager craftingMgr = null;

    public void loadCharacters() {
        JsonObject allCharactersRaw = AtomicActions.getAllCharacters();
        if (allCharactersRaw != null && allCharactersRaw.has("data") && allCharactersRaw.get("data").isJsonArray()) {
            JsonArray charactersArray = allCharactersRaw.get("data").getAsJsonArray();
            for (JsonElement characterRaw : charactersArray) {
                JsonObject characterPackage = new JsonObject();
                characterPackage.add("data", characterRaw);
                Character c = Character.fromJson(characterPackage);
                this.logger.info("Loaded {}", c.getName());
                this.characters.put(c.getName(), c);
            }
        }
    }

    public void runCharacters() {
        for (Character c : this.characters.values()) {
            Thread t = new Thread(c);
            t.setDaemon(true);
            t.start();
            threads.add(t);
        }
    }

    public void setCraftingItem(String item, int quantity) {
        this.currentlyCrafting = item;
        this.currentCraftingQuantity = quantity;
    }

    public String pickItemToCraft() {
        // Update the unlocked Gear set
        // 1. get a list of all unlocked gear.
        Set<GameItem> newGear = GearManager.getGearUpToLevel(characters.get(weaponCrafter).getData().weaponcraftingLevel,
                GearManager.allGearTypes);
        // 2. for each item check it if is in the set and not crafted
        // 3. if not, add it
        for (GameItem g : newGear) {
            if (!unlockedGear.contains(g) && this.getOwnershipQuantity(g.code()) < 5) {
                unlockedGear.add(g);
            }
        }

        List<GameItem> sortedItems = new ArrayList<>(unlockedGear).stream()
                // filter out items that we cant get
                .filter(new GearCraftingFilter(this.getHighestLevel()))
                // Sort by easiest to get
                .sorted(new GearCraftingSorter())
                // Convert set to list
                .collect(Collectors.toList());
        logger.info("Gear that needs crafting: {}",
                sortedItems.stream()
                        .map(item -> String.format("Item: %s. Level: %d. Monster: %d", item.code(), item.level(),
                                GearCraftingSorter.getHighestLevelMonsterIngredient(item.craft().items())))
                        .collect(Collectors.toList()));
        if (!sortedItems.isEmpty()) {
            this.currentlyCrafting = sortedItems.get(0).code();
            this.currentCraftingQuantity = 5; // TODO make this correct. Rings are 10. Dont over craft
            this.logger.info("New crafting goal: {}", this.currentlyCrafting);
            return this.currentlyCrafting;
        } else {
            // Increase character combat levels
            return null;
        }
    }

    public void launchCraftingManager() {
        Map<String, Integer> itemsNeeded = GearManager.getInredientsForGear(this.currentlyCrafting,
                this.currentCraftingQuantity);
        this.craftingMgr = new CraftingManager(itemsNeeded);
        this.craftingMgr.updateProgress(this);
        if (!this.craftingMgr.isFinished()) {
            this.logger.info("Assigning all users crafting {}", this.currentlyCrafting);
            this.craftingMgr.assignCharacters(new ArrayList<>(this.characters.values()));
        }
    }

    /**
     * 
     * @return true if the crafting manager has finished collecting all ingredients
     */
    public boolean runCraftingManager() {
        this.craftingMgr.updateProgress(this);
        if (!this.craftingMgr.isFinished()) {
            List<String> charactersForReassignment = this.craftingMgr.getCharactersForReassignment();
            this.logger.info("Progress update: {}", this.craftingMgr.toString());
            this.logger.info("Characters for reassginment: {}", charactersForReassignment);
            // Turn the list of strngs into a list of characters and pass to assign
            // characters
            this.craftingMgr.assignCharacters(charactersForReassignment.stream()
                    .map(c -> this.characters.get(c))
                    .collect(Collectors.toList()));
            this.logger.info("Characters reassigned!");
            return false;
        } else {
            return true;
        }
    }
    public void finishCraftingManager() {
        this.logger.info("We have the necessary ingredients to craft {}", this.currentlyCrafting);
        // Instruct all characters to deposit
        PlanStep depositStep = new PlanStep(PlanAction.DEPOSIT, "", 0, "Deposit all items");
        for (Character c : this.characters.values()) {
            c.interuptCharacter(depositStep); 
        }
        this.logger.info("Everyone has been asked to deposit. Waiting for synchronization...");
        try {
            // Wait for all five characters to deposit
            depositStep.waitForCompletion();
            depositStep.waitForCompletion();
            depositStep.waitForCompletion();
            depositStep.waitForCompletion();
            depositStep.waitForCompletion();
        } catch (InterruptedException e) {
            this.logger.warn("Interupted while waiting for deposits");
        }
        // Instruct our crafter to craft the item
        this.logger.info("All characters deposited! Assigniing {} to craft {} x{}", this.armorCrafter,
                this.currentlyCrafting, this.currentCraftingQuantity);
        Character crafter = this.characters.get(this.armorCrafter);
        List<PlanStep> planToCraft = PlanGenerator.generatePlan(this.currentlyCrafting,
                this.currentCraftingQuantity,
                (int) Math.floor((double) crafter.getInventoryMaxItems() * 0.8));
        this.logger.info("Add this plan to {}'s queue {}", crafter.getName(), planToCraft);
        crafter.addTasksToQueue(planToCraft);
        try {
            this.logger.info("Plan submitted! Waiting for crafting to complete");
            planToCraft.get(planToCraft.size() - 1).waitForCompletion();
            this.logger.info("Crafting is completed!");
        } catch (InterruptedException e) {
            this.logger.warn("Interupted from waiting for completion of crafting!");
        }
    }

    public void forceAllCharactersToDeposit() {
        PlanStep depositStep = new PlanStep(PlanAction.DEPOSIT, "", 0, "Deposit all items");
        for (Character c : this.characters.values()) {
            c.interuptCharacter(depositStep);
        }
    }

    /**
     * This returns the total amount of that Item I have in the game (including all
     * players inventories)
     * 
     * @param code
     * @return
     */
    @Override
    public int getOwnershipQuantity(String code) {
        // Get quantity in bank
        int result = Bank.getInstance().getBankQuantity(code);
        // Sum up quantity in every characters inventory
        for (Character c : this.characters.values()) {
            result += c.inventoryService.getInventoryQuantity(code, c.gearService);
        }
        return result;
    }

    public int getHighestLevel() {
        int highest = 0;
        for (Character c : this.characters.values()) {
            if (c.getLevel() > highest)
                highest = c.getLevel();
        }
        return highest;
    }

    public void standbyMode()  {
        this.logger.info("Entering standby mode");
        BlockingQueue<String> waiting = new ArrayBlockingQueue<>(5);
        while(true) {
            try {
                waiting.take();
            } catch (InterruptedException e) {
                this.logger.error("{} {}", e.getMessage(), e.getStackTrace());
            }
        }
    }

    public void assignAllToTask(PlanStep task) {
        for(Character character: this.characters.values()) {
            character.setTask(task);
        }
    }
    public void addToAllQueues(PlanStep task) {
        for(Character character : this.characters.values()) {
            character.addTaskToQueue(task);
        }
    }

    public Map<String, PlanStep> getAllAssignedTasks() {
        Map<String, PlanStep> result = new HashMap<>();
        for(Character c : this.characters.values()) {
            result.put(c.getName(), c.getCurrentTask());
        }
        return result;
    }

    public void scheduleAssignToTask(String character, PlanStep task, Long delay, TimeUnit unit) {
        this.logger.info("Scheduling task {} to be done in {} {}", task, delay, unit);
        scheduler.schedule(() -> {
            this.assignSpecificCharacterToTask(character, task);
        }, delay, unit);
    }

    public void assignSpecificCharacterToTask(String character, PlanStep task) {
        this.characters.get(character).setTask(task);
    }

    @Override
    public void run() {
        while (true) {
            // switch on the current goal
            switch (this.currentGoal) {
                case CRAFTING_NEW_GEAR -> {
                }
                case UPGRADING_COMBAT_LEVEL -> {
                }
                case UPGRADING_CRAFTING_LEVEL -> {
                }
            }
        }
    }

    public Character getJewelryCrafter() {
        return this.characters.get(this.jewelryCrafter);
    }

    public Character getGearCrafter() {
        return this.characters.get(this.armorCrafter);
    }
}

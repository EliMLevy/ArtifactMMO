package com.elimelvy.artifacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.PlanGenerator.PlanStep;
import com.elimelvy.artifacts.model.Character;
import com.elimelvy.artifacts.model.OwnershipQuantity;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CharacterManager implements OwnershipQuantity, Runnable {

    private Map<String, Character> characters = new HashMap<>();
    private final List<Thread> threads = new ArrayList<>(5);
    private final String weaponCrafter = "Joe";
    private final String armorCrafter = "Joe";
    private final String jewelryCrafter = "Joe";

    private final Logger logger = LoggerFactory.getLogger(CharacterManager.class);

    private enum Goal {
        CRAFTING_NEW_GEAR, // Keep track of a list of gear, keep track of craftable gear
        UPGRADING_COMBAT_LEVEL, // Attack the highest monster that we can beat
        UPGRADING_CRAFTING_LEVEL, // Find the cheapest item at the highest level unlocked
    }

    private final Set<GameItem> unlockedGear = new HashSet<>();
    private final Queue<String> gearToCraft = new LinkedList<>();
    private String currentlyCrafting;
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

    public String pickItemToCraft() {
        // Update the unlocked Gear set
        // 1. get a list of all unlocked gear.
        Set<GameItem> newGear = GearManager.getGearUpToLevel(characters.get(weaponCrafter).getLevel(),
                List.of("weapon"));
        // 2. for each item check it if is in the set and not crafted
        // 3. if not, add it
        for (GameItem g : newGear) {
            if (!unlockedGear.contains(g) && this.getOwnershipQuantity(g.code()) < 5) {
                unlockedGear.add(g);
            }
        }
        Set<String> itemsToIgnore = Set.of("wooden_stick", "wooden_staff", "spruce_fishing_rod",
                "multislimes_sword", "mushstaff",
                "mushmush_bow");
        List<GameItem> sortedItems = new ArrayList<>(unlockedGear).stream()
                // filter out items that we cant get
                .filter(item -> {
                    if (itemsToIgnore.contains(item.code())) {
                        return false;
                    }
                    // Get the ingredients of this item.
                    Map<String, Integer> ingredients = GearManager.getInredientsForGear(item.code(), 5);
                    // For each ingredient determine if we can get it
                    boolean canCraft = true;
                    for (Map.Entry<String, Integer> ingredient : ingredients.entrySet()) {
                        // If the ingredient is a drop, simulate a fight with best gear agains the
                        // mosnter
                        List<Monster> m = MapManager.getInstance().getMonster(ingredient.getKey());
                        if (m != null && !m.isEmpty()) {
                            // Make sure we have the level
                            if (m.get(0).getLevel() <= this.getHighestLevel()) {
                                // TODO simulate the fight
                                boolean canWeDefeatMonster = true;
                                if (!canWeDefeatMonster) {
                                    canCraft = false;
                                }
                            } else {
                                canCraft = false;
                            }
                        }
                    }
                    return canCraft;
                })
                // Sort by easiest to get
                .sorted((a, b) -> {
                    if (a.level() != b.level()) {
                        return a.level() - b.level();
                    }
                    return 0;
                })
                // Convert set to list
                .collect(Collectors.toList());
        logger.info("Gear that needs crafting: {}",
                sortedItems.stream().map(item -> item.code()).collect(Collectors.toList()));
        if (!sortedItems.isEmpty()) {
            this.currentlyCrafting = sortedItems.get(0).code();
            this.logger.info("New crafting goal: {}", this.currentlyCrafting);
            return this.currentlyCrafting;
        } else {
            // Increase character combat levels
            return null;
        }
    }

    public void launchCraftingManager() {
        Map<String, Integer> itemsNeeded = GearManager.getInredientsForGear(this.currentlyCrafting, 5);
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
            this.logger.info("Progress update: {}", this.craftingMgr.getIngredientProgress());
            this.logger.info("Characters for reassginment: {}", charactersForReassignment);
            // Turn the list of strngs into a list of characters and pass to assign
            // characters
            this.craftingMgr.assignCharacters(charactersForReassignment.stream()
                    .map(c -> this.characters.get(c))
                    .collect(Collectors.toList()));
            return false;
        } else {
            this.logger.info("We have the necessary ingredients to craft {}", this.currentlyCrafting);
            // Instruct all characters to deposit
            CountDownLatch latch = new CountDownLatch(5);
            for (Character c : this.characters.values()) {
                c.seDepositLatch(latch);
                c.addTaskToQueue(new PlanStep("deposit", "", 0, "Deposit all items"));
            }

            // Wait until everyone has deposited
            this.logger.info("Everyone has been asked to deposit. Waiting for synchronization...");
            try {
                latch.await();
            } catch (InterruptedException e) {
                this.logger.error("Interupted waiitinf for deposits");
            }
            // Instruct our crafter to craft the item
            this.logger.info("All characters deposited! Assigniing {} to craft {} x{}", this.armorCrafter,
                    this.currentlyCrafting, 5);
            Character crafter = this.characters.get(this.armorCrafter);
            List<PlanStep> planToCraft = PlanGenerator.generatePlan(crafter, this.currentlyCrafting, 5,
                    (int) Math.floor((double) crafter.getInventoryMaxItems() * 0.8));
            this.logger.info("Add this plan to {}'s queue {}", crafter.getName(), planToCraft);
            crafter.addTasksToQueue(planToCraft);
            return true;
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
            result += c.getInventoryQuantity(code);
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

    public void standbyMode() throws InterruptedException {
        this.logger.info("Entering standby mode");
        this.threads.get(0).join();
    }

    @Override
    public void run() {
        while (true) {
            // switch on the current goal
            switch (this.currentGoal) {
                case CRAFTING_NEW_GEAR -> {}
                case UPGRADING_COMBAT_LEVEL -> {
                }
                case UPGRADING_CRAFTING_LEVEL -> {
                }
            }
        }
    }
}

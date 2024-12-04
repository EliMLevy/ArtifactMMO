package com.elimelvy.artifacts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.elimelvy.artifacts.model.Character;
import com.elimelvy.artifacts.model.OwnershipQuantity;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;

public class CharacterManager implements OwnershipQuantity {

    private Map<String, Character> characters = new HashMap<>();
    private String weaponCrafter = "Joe";
    private String armorCrafter = "Joe";
    private String jewelryCrafter = "Joe";


    private enum Goal {
        CRAFTING_NEW_GEAR, // Keep track of a list of gear, keep track of craftable gear
        UPGRADING_COMBAT_LEVEL, // Attack the highest monster that we can beat
        UPGRADING_CRAFTING_LEVEL, // Find the cheapest item at the highest level unlocked
    }

    private Set<String> unlockedGear = new HashSet<>();
    private Queue<String> gearToCraft = new LinkedList<>();
    private String currentlyCrafting;
    private Goal currentGoal = Goal.CRAFTING_NEW_GEAR;
    
    private void manageCraftingNewGear() {
        if (currentlyCrafting == null) {
            // Update the unlocked Gear set
            // 1. get a list of all unlocked gear.
            Set<GameItem> newGear = GearManager.getGearAtLevel(characters.get(weaponCrafter).getLevel(), List.of("weapon")); 
            // 2. for each item check it if is in the set and not crafted
            // 3. if not, add it
            for (GameItem g : newGear) {
                if (!unlockedGear.contains(g.code()) && this.getOwnershipQuantity(g.code()) < 5) {
                    unlockedGear.add(g.code());
                }
            }
            List<GameItem> sortedItems = newGear.stream()
                // filter out items that we cant get
                .filter(item -> {
                    // Get the ingredients of this item.
                    Map<String, Integer> ingredients = GearManager.getInredientsForGear(item.code(), 5);
                    // For each ingredient determine if we can get it
                    boolean canCraft = true;
                    for (Map.Entry<String, Integer> ingredient : ingredients.entrySet()) {
                        // If the ingredient is a drop, simulate a fight with best gear agains the mosnter
                        List<Monster> m = MapManager.getInstance().getMonster(ingredient.getKey());
                        if (m != null && !m.isEmpty()) {
                            // Make sure we have the level
                            if(m.get(0).getLevel() <= this.getHighestLevel()) {
                                // TODO simulate the fight
                                boolean canWeDefeatMonster = true;
                                if(!canWeDefeatMonster) {
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
                // TODO
                // Convert set to list
                .collect(Collectors.toList());
    
            if (!sortedItems.isEmpty()) {
                this.currentlyCrafting = sortedItems.get(0).code();
                this.currentGoal = Goal.CRAFTING_NEW_GEAR;
                // Assign jobs to each character
            } else {
                // Increase character combat levels
                this.currentGoal = Goal.UPGRADING_COMBAT_LEVEL;
            }
        } else {
            //  Check and log the progress of our characters 
            //  If any characters have finished their assignments, they can train or help others
        }
    }

    /**
     * This returns the total amount of that Item I have in the game (including all players inventories)
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
            if (c.getLevel() > highest) highest = c.getLevel();
        }
        return highest;
    }
}

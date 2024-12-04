package com.elimelvy.artifacts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.elimelvy.artifacts.model.Character;

public class CharacterManager {

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


    private void manageCraftingNewGear() {
        // Update the unlocked Gear set
        // 1. get a list of all unlocked gear.
        // 2. for each item check it if is in the set
        // 3. if not, add it

        // Update the gear to craft queue
        // 1. for each item in the unlocked gear set
        // 2. if it isnt in our gear to craft queue
        // 3. Get the list of ingredients
        // 4. for each ingredient see if we can obtain it (TODO)

        // If we are not in the middle of crafting an item, 
        //  dequeue an item from gear to craft and start crafting it
        // Otherwise,
        //  Check and log the progress of our characters 
        //  If any characters have finished their assignments, they can train or help others

    }
    
}

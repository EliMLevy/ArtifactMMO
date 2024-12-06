package com.elimelvy.artifacts;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.PlanGenerator.PlanStep;
import com.elimelvy.artifacts.model.Character;
import com.elimelvy.artifacts.model.OwnershipQuantity;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;

/**
 * The goal of this class is to manage the state for the duration of a crafting
 * project
 */
public class CraftingManager {

    private final Logger logger = LoggerFactory.getLogger(CraftingManager.class);
    private final Map<String, Integer> itemsNeeded;
    private final Map<String, Integer> ingredientProgress;
    private final Map<String, List<String>> workAssignments; // ingredient code : list of characters collecting it
    private boolean isFinished = false;

    public CraftingManager(Map<String, Integer> itemsNeeded) {
        this.itemsNeeded = itemsNeeded;
        if (this.itemsNeeded == null) {
            this.isFinished = true;
            logger.warn("Received a null ingredients list");
        } else if (this.itemsNeeded.isEmpty()) {
            this.isFinished = true;
            logger.warn("Received an empty ingredients list");
        }
        this.ingredientProgress = new HashMap<>();
        this.workAssignments = new HashMap<>();
        if (this.itemsNeeded != null) {
            for (String i : this.itemsNeeded.keySet()) {
                this.ingredientProgress.put(i, 0);
                this.workAssignments.put(i, new LinkedList<>());
            }
        }
    }

    /**
     * Assigns the given characters to jobs based on what jobs have the fewest characters working on them.
     * Make sure not to assign a character twice because that will cause major problems. 
     * TODO keep track of which characters are assigned and dont allow double assignments.
     * @param characters
     */
    public void assignCharacters(List<Character> characters) {
        Set<String> ingredientsNotCompleted = this.getNonCompletedIngredients();
        if (!ingredientsNotCompleted.isEmpty()) {
            // If there are ingredients that are not completed

            for (Character character : characters) {
                assignCharacterToJob(character, ingredientsNotCompleted);
            }
        } else {
            this.isFinished = true;
        }
    }

    /**
     * Updates the progress tracker state by calling the
     * OwnershipQuantity.getOwnershipQuantity method.
     * When this method returns, the isFinished flag will be an accurate reflection
     * of whether all the ingredients needed to craft have been collected
     * 
     * @param mgr the character manager to access the inventories of all characters
     * @return A list of characters that need to be reassigned to new work.
     */
    public void updateProgress(OwnershipQuantity mgr) {
        this.isFinished = true;
        for (String ingredient : itemsNeeded.keySet()) {
            ingredientProgress.put(ingredient, mgr.getOwnershipQuantity(ingredient));
            if (ingredientProgress.get(ingredient) < this.itemsNeeded.get(ingredient)) {
                this.isFinished = false;
            }
        }
    }

    public List<String> getCharactersForReassignment() {
        List<String> charactersWhoNeedAssignment = new LinkedList<>();
        for (String ingredient : itemsNeeded.keySet()) {
            if (ingredientProgress.get(ingredient) >= this.itemsNeeded.get(ingredient)) {
                charactersWhoNeedAssignment.addAll(this.workAssignments.get(ingredient));
                this.workAssignments.put(ingredient, Collections.emptyList());
            }
        }
        return charactersWhoNeedAssignment;
    }

    /**
     * Assigns the given character to the job with the fewest poeple working on it.
     * Since traveling takes time, it is most efficient to have each character doing
     * one thing for a longer time rather than many characters all doing one thing
     * together for a short time.
     * 
     * @param character     The character to assign to a job
     * @param candidateJobs A set of jobs that are not completed
     */
    private void assignCharacterToJob(Character character, Set<String> candidateJobs) {
        // Create a map between ingredient and number of workers collecting it
        String resourceWithFewestCollectors = null;
        int fewestCollectorsNum = Integer.MAX_VALUE;
        for (String resource : workAssignments.keySet()) {
            int numCollectors = workAssignments.get(resource).size();
            if (fewestCollectorsNum > numCollectors && candidateJobs.contains(resource)) {
                resourceWithFewestCollectors = resource;
                fewestCollectorsNum = numCollectors;
            }
        }
        if (resourceWithFewestCollectors != null) {
            this.workAssignments.get(resourceWithFewestCollectors).add(character.getName());
            if(MapManager.getInstance().isMonsterDrop(resourceWithFewestCollectors)) {
                List<Monster> monsters = MapManager.getInstance().getMonster(resourceWithFewestCollectors);
                if(!monsters.isEmpty()) {
                    character.setTask(new PlanStep("attack", monsters.get(0).getContentCode(), 0, "Crafting manager assignment"));
                } else {
                    logger.warn("Map manager returned no instances for {}", resourceWithFewestCollectors);
                }
            } else {
                character.setTask(new PlanStep("collect", resourceWithFewestCollectors, 0, "Crafting manager assignment"));
            }
        } else {
            logger.error("Expected there to be unfinished resources but found none!\n{}\n{}", ingredientProgress, workAssignments);
        }
    }

    private Set<String> getNonCompletedIngredients() {
        return itemsNeeded.keySet().stream()
                .filter(ingredient -> ingredientProgress.get(ingredient) < itemsNeeded.get(ingredient))
                .collect(Collectors.toSet());
    }



    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public String toString() {
        return ingredientProgress.entrySet().stream()
                .map(entry -> String.format("%s => %d/%d", entry.getKey(), entry.getValue(),
                        itemsNeeded.get(entry.getKey())))
                .collect(Collectors.joining("\n"));
    }

    public Logger getLogger() {
        return logger;
    }

    public Map<String, Integer> getItemsNeeded() {
        return itemsNeeded;
    }

    public Map<String, Integer> getIngredientProgress() {
        return ingredientProgress;
    }

    public Map<String, List<String>> getWorkAssignments() {
        return workAssignments;
    }
}
package com.elimelvy.artifacts.crafting;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.Bank;
import com.elimelvy.artifacts.GearManager;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;
import com.elimelvy.artifacts.model.map.Resource;

public class GearCraftingFilter implements Predicate<GameItem> {

    private final Logger logger = LoggerFactory.getLogger(GearCraftingFilter.class);

    private final int highestLevel;
    private final Set<String> itemsToIgnore = Set.of("gold_shield", "gold_pickaxe", "leather_gloves", "wooden_stick", "wooden_staff",
            "spruce_fishing_rod", "multislimes_sword", "mushstaff", "mushmush_bow");

    public GearCraftingFilter(int highestLevel) {
        this.highestLevel = highestLevel;

    }


    @Override
    public boolean test(GameItem item) {
        if (itemsToIgnore.contains(item.code())) {
            logger.info("Ignoring {}", item.code());
            return false;
        }
        if (item.craft() == null) {
            return false;
        }
        if(item.craft().level() < 20) {
            return false;
        }
        // Get the ingredients of this item.
        Map<String, Integer> ingredients = GearManager.getInredientsForGear(item.code(), 5);
        // For each ingredient determine if we can get it
        boolean canCraft = true;
        for (Map.Entry<String, Integer> ingredient : ingredients.entrySet()) {
            // If the ingredient is a drop, simulate a fight with best gear agains the
            // mosnter
            if (MapManager.getInstance().isMonsterDrop(ingredient.getKey())) {
                Monster m = MapManager.getInstance().getMonsterByDrop(ingredient.getKey());
                // Make sure we have the level
                if (m.getLevel() <= this.highestLevel) {
                    // TODO simulate the fight
                    boolean canWeDefeatMonster = true;
                    if (!canWeDefeatMonster) {
                        canCraft = false;
                    }
                } else {
                    canCraft = false;
                }
            } else {
                // Check if it is a resource
                Resource r = MapManager.getInstance().getResourceByDrop(ingredient.getKey());
                if(r == null) {
                    // Check in the bank for it
                    if(Bank.getInstance().getBankQuantity(ingredient.getKey()) < ingredient.getValue() && !ingredient.getKey().equals("jasper_crystal")) {
                        logger.info("Cant craft {} becuase {} is inaccessible", item.code(), ingredient.getKey());
                        canCraft = false; 
                    }
                }
            }
        }
        return canCraft;
    }

}

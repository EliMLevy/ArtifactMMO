package com.elimelvy.artifacts.crafting;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.elimelvy.artifacts.GearManager;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;

public class GearCraftingFilter implements Predicate<GameItem> {

    private final int highestLevel;
    private final Set<String> itemsToIgnore = Set.of("gold_pickaxe", "leather_gloves", "wooden_stick", "wooden_staff",
            "spruce_fishing_rod", "multislimes_sword", "mushstaff", "mushmush_bow");

    public GearCraftingFilter(int highestLevel) {
        this.highestLevel = highestLevel;

    }


    @Override
    public boolean test(GameItem item) {
        if (itemsToIgnore.contains(item.code())) {
            return false;
        }
        if (item.recipe() == null) {
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
                if (m.get(0).getLevel() <= this.highestLevel) {
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
    }

}
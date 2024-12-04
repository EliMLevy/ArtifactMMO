package com.elimelvy.artifacts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.item.RecipeIngredient;


/**
 * This class will be a singleton that offers utilities for all things relating to gear.
 * - Get a list of gear for a given level
 * - Geat the ingredients for a given gear
 * - Find the best weapon for a monster
 * - Find the best [gear_slot] for a monster
 */
public class GearManager {

    public static List<String> allGearTypes = List.of("weapon", "helmet", "body_armor", "leg_armor", "boots", "amulet", "ring");
    
    public static Set<GameItem> getGearAtLevel(int level, List<String> gearTypes) {
        GameItemManager giMgr = GameItemManager.getInstance();
        return new HashSet<>(giMgr.getItems(item -> item.level() == level && gearTypes.contains(item.type())));
    }

    public static Map<String, Integer> getInredientsForGear(String code, int quantity) {
        GameItem target = GameItemManager.getInstance().getItem(code);
        if(target.recipe() != null) {
            return ingredientCollector(target, new HashMap<>(), quantity);
        } else {
            return target.recipe().items().stream().collect(Collectors.toMap(RecipeIngredient::code, RecipeIngredient::quantity));
        }
    }

    private static Map<String, Integer> ingredientCollector(GameItem item, Map<String, Integer> ingredients, Integer multiplier) {
        // Ensure ingredients map is initialized
        if (ingredients == null) {
            ingredients = new HashMap<>();
        }

        // Check if the item has a recipe
        if (item.recipe() != null) {
            List<RecipeIngredient> recipeIngredients = item.recipe().items();

            for (RecipeIngredient ingredient : recipeIngredients) {
                // Find the ingredient info
                GameItem ingredientInfo = GameItemManager.getInstance().getItem(ingredient.code());

                if (ingredientInfo != null && ingredientInfo.recipe() != null) {
                    // Recursive call if the ingredient has a recipe
                    ingredientCollector(ingredientInfo, ingredients, multiplier * ingredient.quantity());
                } else {
                    // Add to ingredients map, using computeIfAbsent to handle default value
                    ingredients.merge(
                        ingredient.code(), 
                        ingredient.quantity() * multiplier, 
                        Integer::sum
                    );
                }
            }
        } else {
            // Throw an exception if the item is not craftable
            throw new IllegalArgumentException(item + " is not craftable!");
        }

        return ingredients;
    }
}

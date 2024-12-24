package com.elimelvy.artifacts.crafting;

import java.util.Comparator;
import java.util.List;

import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.RecipeIngredient;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;

public class GearCraftingSorter implements Comparator<GameItem> {

    @Override
    public int compare(GameItem a, GameItem b) {
        // Check the recipes for the highest level monster drop
        int aMonsterLevel = getHighestLevelMonsterIngredient(a.craft().items());
        int bMonsterLevel = getHighestLevelMonsterIngredient(b.craft().items());
        if (aMonsterLevel != bMonsterLevel) {
            return aMonsterLevel - bMonsterLevel;
        } else if (a.level() != b.level()) {
            return a.level() - b.level();
        }
        return 0;
    }


    public static int getHighestLevelMonsterIngredient(List<RecipeIngredient> ingredients) {
        if (ingredients == null) {
            return 0;
        }

        int highestLevel = 0;
        for (RecipeIngredient i : ingredients) {
            // If its a monster drop
            Monster monster = MapManager.getInstance().getMonsterByDrop(i.code());
            if (monster != null) {
                int level = monster.getLevel();
                if (level > highestLevel) {
                    highestLevel = level;
                }
            }
        }
        return highestLevel;
    }

}
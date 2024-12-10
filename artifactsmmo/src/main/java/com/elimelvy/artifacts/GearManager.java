package com.elimelvy.artifacts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.model.item.Effect;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.item.RecipeIngredient;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;

/**
 * This class will be a singleton that offers utilities for all things relating
 * to gear.
 * - Get a list of gear for a given level
 * - Geat the ingredients for a given gear
 * - Find the best weapon for a monster
 * - Find the best [gear_slot] for a monster
 */
public class GearManager {

    private static final Logger logger = LoggerFactory.getLogger(GearManager.class);

    public static List<String> allGearTypes = List.of("weapon", "shield", "helmet", "body_armor", "leg_armor", "boots",
            "amulet", "ring");
    public static List<String> allNonWeaponTypes = List.of("shield", "helmet", "body_armor", "leg_armor", "boots",
            "amulet", "ring");
    public static List<String> allNonWeaponSlots = List.of("shield_slot", "helmet_slot", "body_armor_slot",
            "leg_armor_slot", "boots_slot", "amulet_slot", "ring1_slot", "ring2_slot");

    public static Set<GameItem> getGearUpToLevel(int level, List<String> gearTypes) {
        GameItemManager giMgr = GameItemManager.getInstance();
        return new HashSet<>(giMgr.getItems(item -> item.level() <= level && gearTypes.contains(item.type())));
    }

    public static Map<String, Integer> getInredientsForGear(String code, int quantity) {
        GameItem target = GameItemManager.getInstance().getItem(code);
        if (target.craft() != null) {
            return ingredientCollector(target, new HashMap<>(), quantity);
        } else {
            return Map.of(code, quantity);
        }
    }

    private static Map<String, Integer> ingredientCollector(GameItem item, Map<String, Integer> ingredients,
            Integer multiplier) {
        // Ensure ingredients map is initialized
        if (ingredients == null) {
            ingredients = new HashMap<>();
        }

        // Check if the item has a recipe
        if (item.craft() != null) {
            List<RecipeIngredient> recipeIngredients = item.craft().items();

            for (RecipeIngredient ingredient : recipeIngredients) {
                // Find the ingredient info
                GameItem ingredientInfo = GameItemManager.getInstance().getItem(ingredient.code());

                if (ingredientInfo != null && ingredientInfo.craft() != null) {
                    // Recursive call if the ingredient has a recipe
                    ingredientCollector(ingredientInfo, ingredients, multiplier * ingredient.quantity());
                } else {
                    // Add to ingredients map, using computeIfAbsent to handle default value
                    ingredients.merge(
                            ingredient.code(),
                            ingredient.quantity() * multiplier,
                            Integer::sum);
                }
            }
        } else {
            // Throw an exception if the item is not craftable
            throw new IllegalArgumentException(item + " is not craftable!");
        }

        return ingredients;
    }

    public static String getBestAvailableGearAgainstMonster(Character character, String weaponToUse, String gearType,
            String monster,
            MapManager mapMgr,
            GameItemManager gameItemMgr, Bank bank) {
        // Get the monster
        List<Monster> monsters = mapMgr.getByMonsterCode(monster);
        if (monsters == null || monsters.isEmpty()) {
            logger.warn("Failed to retrieve monster: {}", monster);
            return null;
        }
        Monster target = monsters.get(0);
        logger.debug("Monster: {}. ", target);

        // Get all candidate armor pieces
        String currentArmor = character.getGearInSlot(gearType + "_slot");
        List<GameItem> candidateArmors = gameItemMgr.getItems(item -> {
            int bankQuantity = bank.getBankQuantity(item.code());
            int invQuantity = character.getInventoryQuantity(item.code());
            String type = gearType.startsWith("ring") ? "ring" : gearType;
            return item.type().equals(type) &&
                    (bankQuantity > 0 || invQuantity > 0
                            || (currentArmor != null && currentArmor.equals(item.code())))
                    &&
                    item.level() <= character.getLevel();
        });

        if (candidateArmors == null || candidateArmors.isEmpty()) {
            logger.warn("Failed to retrive any {}s for {}", gearType, character.toString());
            return null;
        }
        GameItem currentWeapon = gameItemMgr.getItem(weaponToUse);
        // For each piece, calculate the extra damage it does and the resistance it adds
        double bestBoost = 0;
        GameItem bestArmor = candidateArmors.get(0);
        for (GameItem armor : candidateArmors) {
            double total_dmg = getArmorDamage(armor, currentWeapon);
            double total_res = getArmorResistance(target, armor);
            logger.debug("Total damage of {} against {} is {} and total resistance is {}",
                    armor.code(), monster, total_dmg, total_res);
            if (total_dmg + total_res > bestBoost) {
                bestBoost = total_dmg + total_res;
                bestArmor = armor;
            }
        }
        // Return the piece that has the highest (damage + resistance)
        return bestArmor.code();
    }

    public static String getBestWeaponAgainstMonster(Character character, String monster, MapManager mapMgr,
            GameItemManager gameItemMgr, Bank bank) {
        // Get the monster
        List<Monster> monsters = mapMgr.getByMonsterCode(monster);
        if (monsters == null || monsters.isEmpty()) {
            logger.warn("Failed to retrieve monster: {}", monster);
            return null;
        }
        Monster target = monsters.get(0);
        logger.debug("Monster: {}. ", target);

        // Get all the candidate weapons
        // - in bank or inv or equipped
        // - type is weapon
        // - level <= character level
        String currentWeapon = character.getGearInSlot("weapon_slot");
        List<GameItem> candidateWeapons = gameItemMgr.getItems(item -> {
            int bankQuantity = bank.getBankQuantity(item.code());
            int invQuantity = character.getInventoryQuantity(item.code());
            boolean result = item.type().equals("weapon") &&
                    (bankQuantity > 0 || invQuantity > 0
                            || (currentWeapon != null && currentWeapon.equals(item.code())))
                    &&
                    item.level() <= character.getLevel();

            return result;
        });

        if (candidateWeapons == null || candidateWeapons.isEmpty()) {
            logger.warn("Failed to retrive any weapons for {}", character.toString());
            return null;
        }

        // For each weapon calc dmg
        double highestDmg = 0;
        GameItem bestWeapon = candidateWeapons.get(0);
        // If we have an equipped weapon, it should be the default
        if (currentWeapon != null && !currentWeapon.isBlank()) {
            bestWeapon = gameItemMgr.getItem(currentWeapon);
            highestDmg = getWeaponDamage(target, bestWeapon);
        }
        for (GameItem weapon : candidateWeapons) {
            double total_dmg = getWeaponDamage(target, weapon);
            logger.debug("Total damage of {} against {} is {}", weapon.code(), monster, total_dmg);
            if (total_dmg > highestDmg) {
                highestDmg = total_dmg;
                bestWeapon = weapon;
            }

        }
        // Choose the one with the highest damage
        return bestWeapon.code();

    }

    public static double getWeaponDamage(Monster target, GameItem weapon) {
        double damage_fire = getEffectValue(weapon, "attack_fire") * (1 - target.getResFire() / 100);
        double damage_earth = getEffectValue(weapon, "attack_earth") * (1 - target.getResEarth() / 100);
        double damage_water = getEffectValue(weapon, "attack_water") * (1 - target.getResWater() / 100);
        double damage_air = getEffectValue(weapon, "attack_air") * (1 - target.getResAir() / 100);
        double total_dmg = damage_fire + damage_earth + damage_water + damage_air;
        return total_dmg;
    }

    public static double getArmorDamage(GameItem armor, GameItem weapon) {
        double damage_fire = (1 + getEffectValue(armor, "dmg_fire") / 100);
        double damage_earth = (1 + getEffectValue(armor, "dmg_earth") / 100);
        double damage_water = (1 + getEffectValue(armor, "dmg_water") / 100);
        double damage_air = (1 + getEffectValue(armor, "dmg_air") / 100);
        if (weapon != null) {
            damage_fire *= getEffectValue(weapon, "attack_fire");
            damage_earth *= getEffectValue(weapon, "attack_earth");
            damage_water *= getEffectValue(weapon, "attack_water");
            damage_air *= getEffectValue(weapon, "attack_air");
        }
        double total_dmg = damage_fire + damage_earth + damage_water + damage_air;
        return total_dmg;
    }

    public static double getArmorResistance(Monster target, GameItem armor) {
        double res_fire = (getEffectValue(armor, "res_fire") / 100) * target.getAttackFire();
        double res_earth = (getEffectValue(armor, "res_earth") / 100) * target.getAttackEarth();
        double res_water = (getEffectValue(armor, "res_water") / 100) * target.getAttackWater();
        double res_air = (getEffectValue(armor, "res_air") / 100) * target.getAttackAir();
        double total_res = res_fire + res_earth + res_water + res_air;
        return total_res;
    }

    public static double getEffectValue(GameItem item, String effect) {
        if (item == null || item.effects() == null) {
            return 0;
        }
        for (Effect e : item.effects()) {
            if (e.name().equals(effect)) {
                return e.value();
            }
        }
        return 0;
    }
}

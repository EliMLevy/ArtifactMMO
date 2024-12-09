package com.elimelvy.artifacts.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.Bank;
import com.elimelvy.artifacts.Character;
import com.elimelvy.artifacts.GearManager;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;

public class CharacterStatSimulator {

    public Map<String, GameItem> equippedGear = new HashMap<>(); // Map slot name to game item instance of gear

    private final Character character;

    private final Logger logger = LoggerFactory.getLogger(CharacterStatSimulator.class);

    public CharacterStatSimulator(Character character) {
        this.character = character;
    }

    // API to set the gear

    public void optimizeForMonster(String monster, MapManager mapMgr, GameItemManager gameItemMgr, Bank bank) {
        List<Monster> maps = MapManager.getInstance().getByMonsterCode(monster);
        if (maps == null || maps.isEmpty()) {
            return;
        }
        // Equip the correct gear if we havent already
        String selection = GearManager.getBestWeaponAgainstMonster(this.character, monster, mapMgr, gameItemMgr, bank);
        this.setGear("weapon_slot", selection);
        for (String gearType : GearManager.allNonWeaponTypes) {
            if (!gearType.equals("ring")) {
                selection = GearManager.getBestAvailableGearAgainstMonster(this.character,
                        this.equippedGear.get("weapon_slot").code(), gearType, monster, mapMgr, gameItemMgr, bank);

                this.setGear(gearType + "_slot", selection);
            } else {
                // There are two ring slots
                selection = GearManager.getBestAvailableGearAgainstMonster(this.character,
                        this.equippedGear.get("weapon_slot").code(), gearType + "1", monster, mapMgr, gameItemMgr,
                        bank);
                this.setGear(gearType + "1_slot", selection);
                selection = GearManager.getBestAvailableGearAgainstMonster(this.character,
                        this.equippedGear.get("weapon_slot").code(), gearType + "2", monster, mapMgr, gameItemMgr,
                        bank);
                this.setGear(gearType + "2_slot", selection);
            }
        }
    }

    public void setGear(String slot, String selection) {
        this.logger.debug("Setting {} to {}", slot, selection);
        this.equippedGear.put(slot, GameItemManager.getInstance().getItem(selection));
    }

    public boolean getPlayerWinsAgainstMonster(String monster) {
        // Get the monster
        List<Monster> monsters = MapManager.getInstance().getByMonsterCode(monster);
        if (monsters == null || monsters.isEmpty()) {
            logger.warn("Could not find monster: {}", monster);
            return false;
        }
        Monster target = monsters.get(0);
        // Get player damage per turn
        double playerAttack = this.computePlayerAttack(target);
        // Get monster damage per turn
        double monsterAttack = this.computeMonsterAttack(target);

        // Simulate the fight
        double playerHealth = this.character.getMaxHp();
        double monsterHealth = target.getHp();

        boolean monsterWon = true;
        for (int i = 0; i < 100; i++) {
            monsterHealth -= playerAttack;
            logger.debug("Player did {} damage", playerAttack);
            if (monsterHealth <= 0) {
                monsterWon = false;
                break;
            }
            playerHealth -= monsterAttack;
            logger.debug("Monster did {} damage", monsterAttack);
            if (playerHealth <= 0) {
                monsterWon = true;
                break;
            }
        }
        logger.debug("{} won! with {} health remaining", monsterWon ? "Monster" : "Player",
                monsterWon ? monsterHealth : playerHealth);
        return !monsterWon;
    }

    private double computeAttackOfElement(String element, Monster monster) {
        // Get attack fire from the weapon
        GameItem weapon = this.equippedGear.get("weapon_slot");
        if (weapon == null) {
            return 0;
        }
        double attack = GearManager.getEffectValue(weapon, "attack_" + element);
        // For each gear slot, sum up the fire boost
        double boost = 0;
        for (String gearSlot : GearManager.allNonWeaponSlots) {
            GameItem gear = this.equippedGear.get(gearSlot);
            double additional = GearManager.getEffectValue(gear, "dmg_" + element);
            boost += additional;
            logger.debug("{} adds {} to {} boost", gear.code(), additional, element);
        }
        // Get monster resistance
        double monsterRes = switch (element) {
            case "fire" -> monster.getResFire();
            case "earth" -> monster.getResEarth();
            case "water" -> monster.getResWater();
            case "air" -> monster.getResAir();
            default -> 0;
        };
        // reutrn attack fire * fire boost * monster resistance
        logger.debug("{} boost is {}", element, boost);
        return attack * (1 + boost / 100) * (1 - monsterRes / 100);
    }

    private double computePlayerAttack(Monster monster) {
        List<String> elements = List.of("fire", "earth", "water", "air");

        double result = 0;
        for (String e : elements) {
            result += computeAttackOfElement(e, monster);
        }
        return result;
    }

    private double getPlayerResistance(String element) {
        double result = 0;
        for (String gearSlot : GearManager.allNonWeaponSlots) {
            GameItem gear = this.equippedGear.get(gearSlot);
            result += GearManager.getEffectValue(gear, "res_" + element);
        }
        return result;
    }

    private double computeMonsterAttack(Monster monster) {
        // sum for each element, player res * monster attack
        double fireAttack = monster.getAttackFire() * (1 - getPlayerResistance("fire") / 100);
        double earthAttack = monster.getAttackEarth() * (1 - getPlayerResistance("earth") / 100);
        double waterAttack = monster.getAttackWater() * (1 - getPlayerResistance("water") / 100);
        double airAttack = monster.getAttackAir() * (1 - getPlayerResistance("air") / 100);
        return fireAttack + earthAttack + waterAttack + airAttack;
    }

}

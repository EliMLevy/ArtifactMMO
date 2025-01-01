package com.elimelvy.artifacts.ArtifactCharacter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.AtomicActions;
import com.elimelvy.artifacts.Bank;
import com.elimelvy.artifacts.Character;
import com.elimelvy.artifacts.model.CharacterStatSimulator;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.MapTile;
import com.elimelvy.artifacts.model.map.Monster;
import com.google.gson.JsonObject;

public class CharacterCombatService {

    private final Character character;
    private final Logger logger;

    public CharacterCombatService(Character character) {
        this.character = character;
        this.logger = LoggerFactory.getLogger("CharacterCombatService." + this.character.getName());

    }

    public void attackMonster(String code, CharacterMovementService movementService, CharacterGearService gearService,
            CharacterInventoryService inventoryService) {
        // Get resource map
        List<MapTile> maps = MapManager.getInstance().getMap(code);
        if (maps == null || maps.isEmpty()) {
            this.logger.warn("Invalid monster code: {}", code);
            try {
                Thread.sleep(10000); // This error sometimes happens when there is a lag in updating the monsters on
                                     // the map for an event
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
            return;
        }
        // Make sure I can defeat this monster, otherwise train combat
        CharacterStatSimulator simulator = new CharacterStatSimulator(character);
        simulator.optimizeForMonster(code, MapManager.getInstance(), GameItemManager.getInstance(),
                Bank.getInstance());
        if (!simulator.getPlayerWinsAgainstMonster(code)) {
            this.logger.info("Cant defeat {} without potion", code);
            // Try to equip a boost potion
            double fireDmg = simulator.computeAttackOfElement("fire", MapManager.getInstance().getMonster(code));
            double waterDmg = simulator.computeAttackOfElement("water", MapManager.getInstance().getMonster(code));
            double airDmg = simulator.computeAttackOfElement("air", MapManager.getInstance().getMonster(code));
            double earthDmg = simulator.computeAttackOfElement("earth", MapManager.getInstance().getMonster(code));
            double max = Math.max(fireDmg, Math.max(waterDmg, Math.max(airDmg, earthDmg)));
            String potion;
            if(fireDmg == max) {
                simulator.setElementPotionBoost("fire", 1.12);
                potion = "fire_boost_potion";
            } else if(waterDmg == max) {
                simulator.setElementPotionBoost("water", 1.12);
                potion = "water_boost_potion";
            } else if (airDmg == max) {
                simulator.setElementPotionBoost("air", 1.12);
                potion = "air_boost_potion";
            } else {
                simulator.setElementPotionBoost("earth", 1.12);
                potion = "earth_boost_potion";
            }
            if ((Bank.getInstance().getBankQuantity(potion) == 0 && inventoryService.getInventoryQuantity(potion, gearService) == 0) || !simulator.getPlayerWinsAgainstMonster(code)) {
                if(!simulator.getPlayerWinsAgainstMonster(code)) {
                    this.logger.info("Even with potion cant defeat {} so Im going to train combat", code);
                } else {
                    this.logger.info("I can defeat {} with {} but there arent any so training combat", code, potion);
                }
                this.trainCombat(movementService, gearService, inventoryService);
                return;
            } else {
                // This method call will withdraw item if necessary
                gearService.equipGear("utility1", potion, inventoryService, movementService, this);
            }

        } else {
            this.logger.info("I can defeat {} with this loadout {}", code, simulator.getLoadout());
        }
        // Equip the correct gear
        gearService.equipGearForBattle(code, inventoryService, movementService, this);

        // If we are fighting the lich, equip a water boost potion

        // Deposit inventory if we need to deposit
        inventoryService.depositAllItemsIfNecessary(movementService);

        // Fill up on consumables if necessary
        inventoryService.fillUpOnConsumables(
                List.of("cooked_wolf_meat", "cooked_chicken", "cooked_trout", "gingerbread", "apple_pie", "cooked_bass"), gearService,
                movementService);

        // Move to the right spot if we arent there already
        movementService.moveToMap(code);

        // Rest if we need to rest
        healIfNecessary(inventoryService, gearService);

        // Attack
        this.logger.info("Attacking {}!", code);
        JsonObject result = AtomicActions.attack(character.getName(), code);
        character.handleActionResult(result);
    }

    public void trainCombat(CharacterMovementService movementService, CharacterGearService gearService,
            CharacterInventoryService inventoryService) {
        // Get all monsters up the current level (no more than 10 less than current)
        // Sort in descending ordre of level
        // Find the first one we can defeat and battle him
        Monster target = this.getHighestMonsterDefeatable();
        this.logger.info("Training combat by fighting {}", target.getCode());
        this.attackMonster(target.getCode(), movementService, gearService, inventoryService);
    }

    public Monster getHighestMonsterDefeatable() {
        List<Monster> monsters = MapManager.getInstance().getMonstersByLevel(character.getLevel() - 10,
                character.getLevel());
        if (monsters == null || monsters.isEmpty()) {
            logger.warn("Cant find any monsters on my level. level: {}", character.getLevel());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
            return null;
        }
        // Sort in descending ordre of level
        monsters = new ArrayList<>(monsters); // Copy over list so that we can sort it. Otherwise unsupported operation
        monsters.sort((a, b) -> b.getLevel() - a.getLevel());
        // Find the first one we can defeat and battle him
        for (Monster m : monsters) {
            if (MapManager.getInstance().getMap(m.getCode()) == null)
                continue;
            CharacterStatSimulator simulator = new CharacterStatSimulator(character);
            simulator.optimizeForMonster(m.getCode(), MapManager.getInstance(), GameItemManager.getInstance(),
                    Bank.getInstance());
            if (simulator.getPlayerWinsAgainstMonster(m.getCode())) {
                return m;
            }
        }
        return null;
    }

    public void healIfNecessary(CharacterInventoryService inventoryService, CharacterGearService gearService) {
        // Attempt to use consumables first
        // Find consumables in our inventory

        if ((double) character.getData().hp / (double) character.getData().maxHp < 0.6) {
            inventoryService.useConsumablesForHealing(gearService);
            JsonObject result = AtomicActions.rest(character.getName());
            character.handleActionResult(result);
        }
    }

}

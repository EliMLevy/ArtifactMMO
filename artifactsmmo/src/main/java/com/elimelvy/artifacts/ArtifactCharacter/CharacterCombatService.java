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
import com.elimelvy.artifacts.model.map.Monster;
import com.google.gson.JsonObject;

public class CharacterCombatService {

    private final Character character;
    private final Logger logger;

    public CharacterCombatService(Character character) {
        this.character = character;
        this.logger = LoggerFactory.getLogger("CharacterCombatService." + this.character.getName());

    }

    public void attackMonster(String code, CharacterMovementService movementService, CharacterGearService gearService, CharacterInventoryService inventoryService) {
        // Get resource map
        List<Monster> maps = MapManager.getInstance().getByMonsterCode(code);
        if (maps == null || maps.isEmpty()) {
            this.logger.warn("Invalid monster code: {}", code);
            return;
        }
        Monster target = movementService.getClosestMap(maps);
        // Make sure I can defeat this monster, otherwise train combat
        CharacterStatSimulator simulator = new CharacterStatSimulator(character);
        simulator.optimizeForMonster(target.getMapCode(), MapManager.getInstance(), GameItemManager.getInstance(),
                Bank.getInstance());
        if (!simulator.getPlayerWinsAgainstMonster(target.getMapCode())) {
            this.logger.info("Can't defeat {} so Im going to train combat", target.getMapCode());
            this.trainCombat(movementService, gearService, inventoryService);
            return;
        } else {
            this.logger.info("I can defeat {} with this loadout {}", target.getMapCode(), simulator.getLoadout());
        }
        // Equip the correct gear
        gearService.equipGearForBattle(code, inventoryService, movementService, this);

        // Deposit inventory if we need to deposit
        inventoryService.depositAllItemsIfNecessary(movementService);

        // Fill up on consumables if necessary
        inventoryService.fillUpOnConsumables(List.of("cooked_wolf_meat", "cooked_chicken"), gearService, movementService);

        // Move to the right spot if we arent there already
        movementService.moveToMap(target.getMapCode());

        // Rest if we need to rest
        healIfNecessary(inventoryService, gearService);

        // Attack
        this.logger.info("Attacking {}!", code);
        JsonObject result = AtomicActions.attack(character.getName());
        character.handleActionResult(result);
    }

    public void trainCombat(CharacterMovementService movementService, CharacterGearService gearService,
            CharacterInventoryService inventoryService) {
        // Get all monsters up the current level (no more than 10 less than current)
        // Sort in descending ordre of level
        // Find the first one we can defeat and battle him
        Monster target = this.getHighestMonsterDefeatable();
        this.attackMonster(target.getMapCode(), movementService, gearService, inventoryService);
    }

    public Monster getHighestMonsterDefeatable() {
        List<Monster> monsters = MapManager.getInstance().getMonstersByLevel(character.getLevel() - 10, character.getLevel());
        if(monsters == null || monsters.isEmpty()) {
            logger.warn("Cant find any monsters on my level. level: {}", character.getLevel());
            return null;
        }
        // Sort in descending ordre of level
        monsters = new ArrayList<>(monsters); // Copy over list so that we can sort it. Otherwise unsupported operation
        monsters.sort((a, b) -> b.getLevel() - a.getLevel());
        // Find the first one we can defeat and battle him
        for (Monster m : monsters) {
            CharacterStatSimulator simulator = new CharacterStatSimulator(character);
            simulator.optimizeForMonster(m.getContentCode(), MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
            if(simulator.getPlayerWinsAgainstMonster(m.getContentCode())) {
                return m;
            } 
        }
        return null;
    }

    public void healIfNecessary(CharacterInventoryService inventoryService, CharacterGearService gearService) {
        // Attempt to use consumables first
        // Find consumables in our inventory
        inventoryService.useConsumablesForHealing(gearService);

        if ((double)character.getData().hp / (double)character.getData().maxHp < 0.5) {
            JsonObject result = AtomicActions.rest(character.getName());
            character.handleActionResult(result);
        }
    }

}

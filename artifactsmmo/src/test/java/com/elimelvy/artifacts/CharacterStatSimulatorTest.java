package com.elimelvy.artifacts;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.elimelvy.artifacts.model.CharacterStatSimulator;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;
import com.google.gson.JsonObject;

public class CharacterStatSimulatorTest {

    @Test
    public void simpleTest() {
        JsonObject characterJoe = AtomicActions.getCharacter("Bobby");
        Character joe = Character.fromJson(characterJoe);

        CharacterStatSimulator simulator = new CharacterStatSimulator(joe);
        Bank.getInstance().refreshBankItems();
        simulator.optimizeForMonster("cyclops", MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
        assertTrue(simulator.getPlayerWinsAgainstMonster("cyclops"));
    }

    @Test
    public void deathKnightTest() {
        JsonObject characterJoe = AtomicActions.getCharacter("Bobby");
        Character joe = Character.fromJson(characterJoe);
        String monster = "death_knight";
        CharacterStatSimulator simulator = new CharacterStatSimulator(joe);
        Bank.getInstance().refreshBankItems();
        simulator.optimizeForMonster(monster, MapManager.getInstance(), GameItemManager.getInstance(),
                Bank.getInstance());
        assertTrue(simulator.getPlayerWinsAgainstMonster(monster));
    }

    @Test
    public void testFindHighestDefeatableMonster() {
        JsonObject characterJoe = AtomicActions.getCharacter("George");
        Character joe = Character.fromJson(characterJoe);
        Bank.getInstance().refreshBankItems();
        Long start = System.currentTimeMillis();
        List<Monster> monsters = MapManager.getInstance().getMonstersByLevel(joe.getLevel() - 10, joe.getLevel());
        assertNotNull(monsters, "Monsters null");
        assertFalse(monsters.isEmpty(), "Monsters empty");
        // Sort in descending ordre of level
        monsters = new ArrayList<>(monsters); // Copy over list so that we can sort it. Otherwise unsupported operation
        monsters.sort((a, b) -> b.getLevel() - a.getLevel());
        // Find the first one we can defeat and battle him
        Monster target = null;
        for (Monster m : monsters) {
            CharacterStatSimulator simulator = new CharacterStatSimulator(joe);
            simulator.optimizeForMonster(m.getContentCode(), MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
            if(simulator.getPlayerWinsAgainstMonster(m.getContentCode())) {
                target = m;
                break;
            } else {
                System.out.println("Character lost to " + m);
            }
        }
        System.out.println(target);
        System.out.println("Test took: " + (System.currentTimeMillis() - start));
    }
    
}

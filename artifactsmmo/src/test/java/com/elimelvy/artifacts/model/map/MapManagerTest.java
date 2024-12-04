package com.elimelvy.artifacts.model.map;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class MapManagerTest {

    private MapManager mapManager;

    @BeforeEach
    public void setUp() {
        mapManager = new MapManager();
    }

    @Test
    public void testReadResourcesFromCSV_FileNotEmpty() {
        List<Resource> resources = MapManager.readResourcesFromCSV("./src/main/resources/resources.csv");
        assertFalse(resources.isEmpty(), "Resources list should not be empty");
    }

    @Test
    public void testReadMonstersFromCSV_FileNotEmpty() {
        List<Monster> monsters = MapManager.readMonstersFromCSV("./src/main/resources/monsters.csv");
        assertFalse(monsters.isEmpty(), "Monsters list should not be empty");
    }

    @Test
    public void testReadResourcesFromCSV_CorrectParsing() {
        List<Resource> resources = MapManager.readResourcesFromCSV("./src/main/resources/resources.csv");

        // Check first resource to verify parsing
        Resource firstResource = resources.get(0);
        assertNotNull(firstResource, "First resource should not be null");

        // Verify specific fields are parsed correctly
        assertNotNull(firstResource.getResourceCode(), "Resource code should not be null");
        assertTrue(firstResource.getDropChance() >= 0 && firstResource.getDropChance() <= 100,
                "Drop chance should be between 0 and 100");
        assertNotNull(firstResource.getMapCode(), "Map code should not be null");
        assertTrue(firstResource.getLevel() > 0, "Level should be positive");
        assertNotNull(firstResource.getSkill(), "Skill should not be null");
    }

    @Test
    public void testReadMonstersFromCSV_CorrectParsing() {
        List<Monster> monsters = MapManager.readMonstersFromCSV("./src/main/resources/monsters.csv");

        // Check first monster to verify parsing
        Monster firstMonster = monsters.get(0);
        assertNotNull(firstMonster, "First monster should not be null");

        // Verify specific fields are parsed correctly
        assertTrue(firstMonster.getLevel() > 0, "Monster level should be positive");
        assertNotNull(firstMonster.getResourceCode(), "Resource code should not be null");
        assertTrue(firstMonster.getX() >= 0, "X coordinate should be non-negative");
        assertTrue(firstMonster.getY() >= 0, "Y coordinate should be non-negative");
        assertTrue(firstMonster.getDropChance() >= 0 && firstMonster.getDropChance() <= 100,
                "Drop chance should be between 0 and 100");
        assertNotNull(firstMonster.getMapCode(), "Map code should not be null");

        // Check attack and resistance values
        int[] attackValues = {
                firstMonster.getAttackFire(), firstMonster.getAttackEarth(),
                firstMonster.getAttackWater(), firstMonster.getAttackAir()
        };

        int[] resistanceValues = {
                firstMonster.getResFire(), firstMonster.getResEarth(),
                firstMonster.getResWater(), firstMonster.getResAir()
        };

        for (int attackValue : attackValues) {
            assertTrue(attackValue >= 0, "Attack values should be non-negative");
        }

        for (int resistanceValue : resistanceValues) {
            assertTrue(resistanceValue >= 0, "Resistance values should be non-negative");
        }
    }

    @Test
    public void testGetResource_ValidResourceCode() {
        String testResourceCode = "copper_ore"; 
        List<Resource> resources = mapManager.getResouce(testResourceCode);

        assertFalse(resources.isEmpty(), "Should find resources with the given resource code");
        assertTrue(resources.stream().allMatch(r -> r.getResourceCode().equals(testResourceCode)),
                "All returned resources should have the matching resource code");
    }

    @Test
    public void testGetMonster_ValidResourceCode() {
        String testResourceCode = "wolf_hair"; 
        List<Monster> monsters = mapManager.getMonster(testResourceCode);

        assertFalse(monsters.isEmpty(), "Should find monsters with the given resource code");
        assertTrue(monsters.stream().allMatch(m -> m.getResourceCode().equals(testResourceCode)),
                "All returned monsters should have the matching resource code");
    }

    @Test
    public void testGetResource_NonExistentResourceCode() {
        List<Resource> resources = mapManager.getResouce("NONEXISTENT");

        assertTrue(resources.isEmpty(), "Should return an empty list for non-existent resource code");
    }

    @Test
    public void testGetMonster_NonExistentResourceCode() {
        List<Monster> monsters = mapManager.getMonster("NONEXISTENT");

        assertTrue(monsters.isEmpty(), "Should return an empty list for non-existent resource code");
    }
}
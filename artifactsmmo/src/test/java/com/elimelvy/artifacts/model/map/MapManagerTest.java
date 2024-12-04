package com.elimelvy.artifacts.model.map;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class MapManagerTest {


    @Test
    public void testGetResource_ValidResourceCode() {
        String testResourceCode = "copper_ore"; 
        List<Resource> resources = MapManager.getInstance().getResouce(testResourceCode);

        assertFalse(resources.isEmpty(), "Should find resources with the given resource code");
        assertTrue(resources.stream().allMatch(r -> r.getResourceCode().equals(testResourceCode)),
                "All returned resources should have the matching resource code");
    }

    @Test
    public void testGetMonster_ValidResourceCode() {
        String testResourceCode = "wolf_hair"; 
        List<Monster> monsters = MapManager.getInstance().getMonster(testResourceCode);

        assertFalse(monsters.isEmpty(), "Should find monsters with the given resource code");
        assertTrue(monsters.stream().allMatch(m -> m.getResourceCode().equals(testResourceCode)),
                "All returned monsters should have the matching resource code");
    }

    @Test
    public void testGetResource_NonExistentResourceCode() {
        List<Resource> resources = MapManager.getInstance().getResouce("NONEXISTENT");

        assertTrue(resources.isEmpty(), "Should return an empty list for non-existent resource code");
    }

    @Test
    public void testGetMonster_NonExistentResourceCode() {
        List<Monster> monsters = MapManager.getInstance().getMonster("NONEXISTENT");

        assertTrue(monsters.isEmpty(), "Should return an empty list for non-existent resource code");
    }
}
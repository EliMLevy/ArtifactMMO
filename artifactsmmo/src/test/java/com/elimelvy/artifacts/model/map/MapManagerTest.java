package com.elimelvy.artifacts.model.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class MapManagerTest {


    @Test
    public void testGetResource_ValidResourceCode() {
        String testResourceCode = "copper_ore"; 
        Resource resource = MapManager.getInstance().getResourceByDrop(testResourceCode);

        assertFalse(resource == null, "Should find resources with the given resource code");
        assertEquals("copper_rocks", resource.getCode());
    }

    @Test
    public void testGetMonster_ValidResourceCode() {
        String testResourceCode = "wolf_hair"; 
        Monster monster = MapManager.getInstance().getMonsterByDrop(testResourceCode);

        assertFalse(monster == null, "Should find monsters with the given resource code");
        assertEquals("wolf", monster.getCode());
    }

    @Test
    public void testGetResource_NonExistentResourceCode() {
        Resource resource = MapManager.getInstance().getResouce("NONEXISTENT");

        assertNull(resource, "Should return an empty list for non-existent resource code");
    }

    @Test
    public void testGetMonster_NonExistentResourceCode() {
        Monster monster = MapManager.getInstance().getMonster("NONEXISTENT");

        assertNull(monster, "Should return an empty list for non-existent resource code");
    }

    @Test
    public void testIsMonsterDrop() {
        assertTrue(MapManager.getInstance().isMonsterDrop("serpent_skin"));
        assertFalse(MapManager.getInstance().isMonsterDrop("coal"));
        assertTrue(MapManager.getInstance().isMonsterDrop("blue_slimeball"));
    }

    
    @Test
    public void testGetMonster() {
        Monster expected = new Monster(12, "flying_serpent", "Flying Serpent", 360, 0, 0, 0, 34, -20, 0, -20, 40);
        Monster actual = MapManager.getInstance().getMonster("flying_serpent");
        assertEquals(expected, actual);
    }
}
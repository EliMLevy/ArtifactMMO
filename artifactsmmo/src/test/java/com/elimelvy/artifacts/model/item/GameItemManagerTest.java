package com.elimelvy.artifacts.model.item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class GameItemManagerTest {
    private GameItemManager itemManager;

    @BeforeEach
    void setUp() {
        itemManager = GameItemManager.getInstance();
    }

    @Test
    void testLoadItems() {
        assertNotNull(itemManager, "Item manager should not be null");
        assertFalse(itemManager.getAllItems().isEmpty(), "Items collection should not be empty");
    }

    @Test
    void testGetItemByName() {
        // Test existing item
        GameItem copperDagger = itemManager.getItem("copper_dagger");
        assertNotNull(copperDagger, "Should find copper_dagger");
        assertEquals("copper_dagger", copperDagger.code());
        assertEquals(1, copperDagger.level());
        assertEquals("weapon", copperDagger.type());

        // Test non-existing item
        GameItem nonExistent = itemManager.getItem("non_existent_item");
        assertNull(nonExistent, "Should return null for non-existent item");
    }

    @Test
    void testGetItemsByType() {
        List<GameItem> weapons = itemManager.getItems(item -> "weapon".equals(item.type()));
        assertFalse(weapons.isEmpty(), "Should find weapon items");
        weapons.forEach(item -> assertEquals("weapon", item.type()));
    }

    @Test
    void testGetItemsByLevel() {
        int targetLevel = 1;
        List<GameItem> level1Items = itemManager.getItems(item -> item.level() == targetLevel);
        assertFalse(level1Items.isEmpty(), "Should find level 1 items");
        level1Items.forEach(item -> assertEquals(targetLevel, item.level()));
    }

    @Test
    void testGetItemsByEffectType() {
        List<GameItem> healingItems = itemManager.getItems(item -> item.effects() != null &&
                item.effects().stream().anyMatch(effect -> "heal".equals(effect.name())));
        assertFalse(healingItems.isEmpty(), "Should find items with healing effects");
        healingItems.forEach(item -> assertTrue(
                item.effects().stream().anyMatch(effect -> "heal".equals(effect.name())),
                "Each item should have a healing effect"));
    }

    @Test
    void testGetAllItems() {
        assertNotNull(itemManager.getAllItems(), "All items collection should not be null");
        assertTrue(itemManager.getAllItems().size() > 10, "Should have multiple items");
    }

    @Test
    void testCollectionImmutability() {
        // In JUnit 5, we use assertThrows instead of the (expected = ...) annotation
        assertThrows(UnsupportedOperationException.class, () -> {
            itemManager.getAllItems().clear(); // Should throw UnsupportedOperationException
        }, "Modifying the collection should throw an UnsupportedOperationException");
    }
}
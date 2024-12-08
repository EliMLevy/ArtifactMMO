package com.elimelvy.artifacts;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.elimelvy.artifacts.crafting.CraftingManager;
import com.elimelvy.artifacts.model.Character;
import com.elimelvy.artifacts.model.OwnershipQuantity;

public class CraftingManagerTest {

    @Test
    void Constructor_NullIngredientsList_ShouldSetIsFinishedToTrue() {
        // Arrange
        Map<String, Integer> nullIngredients = null;

        // Inject a custom logger into CraftingManager for test purposes
        CraftingManager craftingManager = new CraftingManager(nullIngredients);

        // Act
        boolean isFinished = craftingManager.isFinished();

        // Assert
        assertTrue(isFinished, "isFinished should be true when ingredients list is null");
    }

    @Test
    void Constructor_EmptyIngredientsList_ShouldSetIsFinishedToTrue() {
        // Arrange
        Map<String, Integer> emptyIngredients = new HashMap<>();

        CraftingManager craftingManager = new CraftingManager(emptyIngredients);

        // Act
        boolean isFinished = craftingManager.isFinished();

        // Assert
        assertTrue(isFinished, "isFinished should be true when ingredients list is empty");
    }

    @Test
    void Constructor_ValidIngredientsList_ShouldInitializeStateCorrectly() {
        // Arrange
        Map<String, Integer> validIngredients = new HashMap<>();
        validIngredients.put("Wood", 10);
        validIngredients.put("Iron", 5);

        // Act
        CraftingManager craftingManager = new CraftingManager(validIngredients);

        // Assert
        assertFalse(craftingManager.isFinished(), "isFinished should be false when ingredients list is valid");
        assertEquals(2, craftingManager.getIngredientProgress().size(),
                "ingredientProgress should have the same size as itemsNeeded");
        assertEquals(2, craftingManager.getWorkAssignments().size(),
                "workAssignments should have the same size as itemsNeeded");
        assertEquals(0, craftingManager.getIngredientProgress().get("Wood"), "Wood should have initial progress of 0");
        assertEquals(0, craftingManager.getIngredientProgress().get("Iron"), "Iron should have initial progress of 0");
        assertNotNull(craftingManager.getWorkAssignments().get("Wood"),
                "Wood should have an empty work assignment list");
        assertNotNull(craftingManager.getWorkAssignments().get("Iron"),
                "Iron should have an empty work assignment list");
    }

    @Test
    void AssignCharacters_ValidCharacters_ShouldAssignToNonCompletedIngredients() {
        // Arrange
        Map<String, Integer> itemsNeeded = new HashMap<>();
        itemsNeeded.put("Wood", 5);
        itemsNeeded.put("Iron", 3);

        CraftingManager craftingManager = new CraftingManager(itemsNeeded);

        Character mockChar1 = mock(Character.class);
        Character mockChar2 = mock(Character.class);

        when(mockChar1.getName()).thenReturn("Alice");
        when(mockChar2.getName()).thenReturn("Bob");

        List<Character> characters = Arrays.asList(mockChar1, mockChar2);

        // Act
        craftingManager.assignCharacters(characters);

        // Assert
        assertEquals(1, craftingManager.getWorkAssignments().get("Wood").size(),
                "Wood should have one character assigned.");
        assertEquals(1, craftingManager.getWorkAssignments().get("Iron").size(),
                "Iron should have one character assigned.");
        assertTrue(
                craftingManager.getWorkAssignments().get("Wood").contains("Alice")
                        || craftingManager.getWorkAssignments().get("Wood").contains("Bob"),
                "Wood should have either Alice or Bob assigned.");
        assertTrue(
                craftingManager.getWorkAssignments().get("Iron").contains("Alice")
                        || craftingManager.getWorkAssignments().get("Iron").contains("Bob"),
                "Iron should have either Alice or Bob assigned.");
    }

    @Test
    void AssignCharacters_AllIngredientsCompleted_ShouldSetIsFinishedToTrue() {
        // Arrange
        Map<String, Integer> itemsNeeded = new HashMap<>();
        itemsNeeded.put("Wood", 5);
        itemsNeeded.put("Iron", 3);

        CraftingManager craftingManager = new CraftingManager(itemsNeeded);

        craftingManager.getIngredientProgress().put("Wood", 5); // Fully collected
        craftingManager.getIngredientProgress().put("Iron", 3); // Fully collected

        Character mockChar1 = mock(Character.class);
        Character mockChar2 = mock(Character.class);

        when(mockChar1.getName()).thenReturn("Alice");
        when(mockChar2.getName()).thenReturn("Bob");

        List<Character> characters = Arrays.asList(mockChar1, mockChar2);

        // Act
        craftingManager.assignCharacters(characters);

        // Assert
        assertTrue(craftingManager.isFinished(), "isFinished should be true when all ingredients are completed.");
        assertTrue(craftingManager.getWorkAssignments().get("Wood").isEmpty(), "Wood should not have any assignments.");
        assertTrue(craftingManager.getWorkAssignments().get("Iron").isEmpty(), "Iron should not have any assignments.");
    }

    @Test
    void AssignCharacters_EmptyCharactersList_ShouldNotModifyAssignments() {
        // Arrange
        Map<String, Integer> itemsNeeded = new HashMap<>();
        itemsNeeded.put("Wood", 5);
        itemsNeeded.put("Iron", 3);

        CraftingManager craftingManager = new CraftingManager(itemsNeeded);

        List<Character> emptyCharacters = Collections.emptyList();

        // Act
        craftingManager.assignCharacters(emptyCharacters);

        // Assert
        assertTrue(craftingManager.getWorkAssignments().get("Wood").isEmpty(), "Wood should not have any assignments.");
        assertTrue(craftingManager.getWorkAssignments().get("Iron").isEmpty(), "Iron should not have any assignments.");
        assertFalse(craftingManager.isFinished(), "isFinished should remain false when no characters are assigned.");
    }

    @Test
    void UpdateProgress_AllIngredientsCompleted_ShouldSetIsFinishedToTrue() {
        // Arrange
        Map<String, Integer> itemsNeeded = new HashMap<>();
        itemsNeeded.put("Wood", 5);
        itemsNeeded.put("Iron", 3);

        CraftingManager craftingManager = new CraftingManager(itemsNeeded);

        OwnershipQuantity mockMgr = mock(OwnershipQuantity.class);
        when(mockMgr.getOwnershipQuantity("Wood")).thenReturn(5);
        when(mockMgr.getOwnershipQuantity("Iron")).thenReturn(3);

        // Act
        craftingManager.updateProgress(mockMgr);

        // Assert
        assertTrue(craftingManager.isFinished(),
                "isFinished should be true when all ingredients meet their required quantities.");
    }

    @Test
    void UpdateProgress_SomeIngredientsIncomplete_ShouldKeepIsFinishedFalse() {
        // Arrange
        Map<String, Integer> itemsNeeded = new HashMap<>();
        itemsNeeded.put("Wood", 5);
        itemsNeeded.put("Iron", 3);

        CraftingManager craftingManager = new CraftingManager(itemsNeeded);

        OwnershipQuantity mockMgr = mock(OwnershipQuantity.class);
        when(mockMgr.getOwnershipQuantity("Wood")).thenReturn(4); // Not enough Wood
        when(mockMgr.getOwnershipQuantity("Iron")).thenReturn(3);

        // Act
        craftingManager.updateProgress(mockMgr);

        // Assert
        assertFalse(craftingManager.isFinished(),
                "isFinished should remain false when any ingredient does not meet its required quantity.");
    }

    @Test
    void UpdateProgress_ValidOwnershipQuantities_ShouldUpdateIngredientProgress() {
        // Arrange
        Map<String, Integer> itemsNeeded = new HashMap<>();
        itemsNeeded.put("Wood", 5);
        itemsNeeded.put("Iron", 3);

        CraftingManager craftingManager = new CraftingManager(itemsNeeded);

        OwnershipQuantity mockMgr = mock(OwnershipQuantity.class);
        when(mockMgr.getOwnershipQuantity("Wood")).thenReturn(4);
        when(mockMgr.getOwnershipQuantity("Iron")).thenReturn(2);

        // Act
        craftingManager.updateProgress(mockMgr);

        // Assert
        assertEquals(4, craftingManager.getIngredientProgress().get("Wood"),
                "Ingredient progress for Wood should be updated to 4.");
        assertEquals(2, craftingManager.getIngredientProgress().get("Iron"),
                "Ingredient progress for Iron should be updated to 2.");
        assertFalse(craftingManager.isFinished(),
                "isFinished should remain false as not all ingredients meet their required quantities.");
    }

    @Test
    void GetCharactersForReassignment_IngredientsCompleted_ShouldReturnCorrectCharacters() {
        // Arrange
        Map<String, Integer> itemsNeeded = new HashMap<>();
        itemsNeeded.put("Wood", 5);
        itemsNeeded.put("Iron", 3);

        CraftingManager craftingManager = new CraftingManager(itemsNeeded);

        craftingManager.getIngredientProgress().put("Wood", 5); // Completed
        craftingManager.getIngredientProgress().put("Iron", 1); // Not completed

        Character mockChar1 = mock(Character.class);
        Character mockChar2 = mock(Character.class);
        when(mockChar1.getName()).thenReturn("Alice");
        when(mockChar2.getName()).thenReturn("Bob");

        craftingManager.getWorkAssignments().put("Wood", new LinkedList<>(Arrays.asList("Alice")));
        craftingManager.getWorkAssignments().put("Iron", new LinkedList<>(Arrays.asList("Bob")));

        // Act
        List<String> reassignedCharacters = craftingManager.getCharactersForReassignment();

        // Assert
        assertTrue(reassignedCharacters.contains("Alice"), "Alice should be returned for reassignment.");
        assertFalse(reassignedCharacters.contains("Bob"), "Bob should not be returned for reassignment.");
        assertTrue(craftingManager.getWorkAssignments().get("Wood").isEmpty(), "Wood assignments should be cleared.");
        assertEquals(1, craftingManager.getWorkAssignments().get("Iron").size(),
                "Iron assignments should remain unchanged.");
    }

    @Test
    void GetCharactersForReassignment_NoCompletedIngredients_ShouldReturnEmptyList() {
        // Arrange
        Map<String, Integer> itemsNeeded = new HashMap<>();
        itemsNeeded.put("Wood", 5);
        itemsNeeded.put("Iron", 3);

        CraftingManager craftingManager = new CraftingManager(itemsNeeded);

        craftingManager.getIngredientProgress().put("Wood", 3); // Not completed
        craftingManager.getIngredientProgress().put("Iron", 2); // Not completed

        Character mockChar1 = mock(Character.class);
        Character mockChar2 = mock(Character.class);
        when(mockChar1.getName()).thenReturn("Alice");
        when(mockChar2.getName()).thenReturn("Bob");

        craftingManager.getWorkAssignments().put("Wood", new LinkedList<>(Arrays.asList("Alice")));
        craftingManager.getWorkAssignments().put("Iron", new LinkedList<>(Arrays.asList("Bob")));

        // Act
        List<String> reassignedCharacters = craftingManager.getCharactersForReassignment();

        // Assert
        assertTrue(reassignedCharacters.isEmpty(), "No characters should be returned for reassignment.");
        assertEquals(1, craftingManager.getWorkAssignments().get("Wood").size(),
                "Wood assignments should remain unchanged.");
        assertEquals(1, craftingManager.getWorkAssignments().get("Iron").size(),
                "Iron assignments should remain unchanged.");
    }
}
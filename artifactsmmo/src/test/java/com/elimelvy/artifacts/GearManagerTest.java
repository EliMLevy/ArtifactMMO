package com.elimelvy.artifacts;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;

public class GearManagerTest {


    @Test
    public void testGetGearAtLevel() {
        int level = 5;
        Set<GameItem> result = GearManager.getGearAtLevel(level);
        List<String> expected = List.of("copper_legs_armor", "feather_coat", "fire_staff", "copper_armor", "sticky_dagger", "sticky_sword", "life_amulet", "water_bow");
        Set<GameItem> expectedItems = expected.stream().map(i -> GameItemManager.getInstance().getItem(i)).collect(Collectors.toSet());

        assertEquals(expected.size(), result.size(), "Expected " + expected.size() + " items but got " + result.size() + ". " + result);
        for(GameItem item : expectedItems) {
            assertTrue(result.contains(item), "Result is missing " + item + ". " + result);
        }
    }
    
}

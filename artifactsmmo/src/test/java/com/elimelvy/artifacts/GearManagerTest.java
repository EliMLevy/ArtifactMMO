package com.elimelvy.artifacts;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.Character;
import com.elimelvy.artifacts.model.map.MapManager;
public class GearManagerTest {


    @Test
    public void testGetWeaponAtLevel() {
        int level = 5;
        Set<GameItem> result = GearManager.getGearUpToLevel(level, List.of("weapon"));
        List<String> expected = List.of("copper_dagger", "wooden_staff", "wooden_stick", "fire_staff", "sticky_dagger", "sticky_sword", "water_bow");
        Set<GameItem> expectedItems = expected.stream().map(i -> GameItemManager.getInstance().getItem(i))
                .collect(Collectors.toSet());

        assertEquals(expected.size(), result.size(),
                "Expected " + expected.size() + " items but got " + result.size() + ". " + result);
        for (GameItem item : expectedItems) {
            assertTrue(result.contains(item), "Result is missing " + item + ". " + result);
        }
    }

    @Test
    public void testGetBestWeapon() {
        Character joe = mock(Character.class);
        when(joe.getInventoryQuantity(anyString())).thenReturn(0);
        when(joe.getLevel()).thenReturn(5);
        String monster = "cow";
        MapManager mapMgr = MapManager.getInstance();
        GameItemManager itemMgr = GameItemManager.getInstance();
        Bank bank = mock(Bank.class);
        when(bank.getBankQuantity(anyString())).thenReturn(5);


        String result = GearManager.getBestWeaponAgainstMonster(joe, monster, mapMgr, itemMgr, bank);
        assertEquals("sticky_sword", result);
    }

    @Test
    public void testGetBestWeaponButNoneAvailable() {
        Character joe = mock(Character.class);
        when(joe.getInventoryQuantity(anyString())).thenReturn(0);
        when(joe.getLevel()).thenReturn(5);
        String monster = "cow";
        MapManager mapMgr = MapManager.getInstance();
        GameItemManager itemMgr = GameItemManager.getInstance();
        Bank bank = mock(Bank.class);
        when(bank.getBankQuantity(anyString())).thenReturn(0);

        String result = GearManager.getBestWeaponAgainstMonster(joe, monster, mapMgr, itemMgr, bank);
        assertNull(result, "Expected result to be null");
    }

    @Test
    public void testGetBestHelmet() {
        Character joe = mock(Character.class);
        when(joe.getInventoryQuantity(anyString())).thenReturn(0);
        when(joe.getLevel()).thenReturn(10);
        String monster = "cow";
        MapManager mapMgr = MapManager.getInstance();
        GameItemManager itemMgr = GameItemManager.getInstance();
        Bank bank = mock(Bank.class);
        when(bank.getBankQuantity(anyString())).thenReturn(5);

        String result = GearManager.getBestAvailableGearAgainstMonster(joe, "helmet", monster, mapMgr, itemMgr, bank);
        assertEquals("adventurer_helmet", result);
    }

    @Test
    public void testGetBestBodyArmor() {
        Character joe = mock(Character.class);
        when(joe.getInventoryQuantity(anyString())).thenReturn(0);
        when(joe.getLevel()).thenReturn(5);
        String monster = "cow";
        MapManager mapMgr = MapManager.getInstance();
        GameItemManager itemMgr = GameItemManager.getInstance();
        Bank bank = mock(Bank.class);
        when(bank.getBankQuantity(anyString())).thenReturn(5);

        String result = GearManager.getBestAvailableGearAgainstMonster(joe, "body_armor", monster, mapMgr, itemMgr, bank);
        assertEquals("copper_armor", result);
    }

    
}

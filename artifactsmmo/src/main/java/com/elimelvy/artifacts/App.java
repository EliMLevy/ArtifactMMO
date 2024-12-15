package com.elimelvy.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.model.CharacterStatSimulator;
import com.elimelvy.artifacts.model.PlanStep;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.map.MapManager;
import com.google.gson.JsonObject;

public class App {
    public static void main(String[] args) throws Exception {
        // ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        // scheduled.scheduleAtFixedRate(new EncyclopediaMaker(), 1, 1, TimeUnit.HOURS);
        // EventManager eventMgr = new EventManager(Map.of("bandit_camp", new PlanStep(PlanAction.ATTACK, "bandit_lizard", 1, "Bandit event is active!")), mgr);
        // scheduled.scheduleAtFixedRate(eventMgr, 1, 1, TimeUnit.HOURS);
        runAllCharactersManually();
        // runCraftingManager();
        
        // Bank.getInstance().refreshBankItems();
        // CharacterManager mgr = new CharacterManager();
        // mgr.loadCharacters();
        // mgr.runCharacters();
        // "emerald_amulet", "sapphire_amulet", serpent_skin_armor, dreadful_ring
        // doCompleteCrafting("serpent_skin_armor", 5, mgr);
        // doCompleteCrafting("dreadful_ring", 10, mgr);

        // runCraftingManagerInLoop(mgr, "steel_ring", (innerMgr) -> innerMgr.getJewelryCrafter().getData().jewelrycraftingLevel < 25);

        // getListOfCraftableGear();
        // getHighestMonsterDefeatable();
        // simulateCharacterBattle("Bobby", "imp");

    }

    public static void doCompleteCrafting(String item, int quantity, CharacterManager mgr) throws Exception {
        mgr.setCraftingItem(item, quantity);
        mgr.launchCraftingManager();
        while (!mgr.runCraftingManager()) {
            System.out.println("MAIN OPERATOR: not finished yet!");
            Thread.sleep(60 * 1000);
        }
        mgr.finishCraftingManager();
    }
    
    public static void runCraftingManagerInLoop(CharacterManager mgr, String item, Predicate<CharacterManager> until) throws Exception {
        while (until.test(mgr)) { 
            doCompleteCrafting(item ,5, mgr);
            System.out.println("MAIN OPERATOR: jewelry crafting level - " + mgr.getJewelryCrafter().getData().jewelrycraftingLevel);
            System.out.println("MAIN OPERATOR: gear crafting level - " + mgr.getGearCrafter().getData().gearcraftingLevel);
        }
    }

    public static void runAllCharactersManually() throws Exception {
        CharacterManager mgr = new CharacterManager();
        Bank.getInstance().refreshBankItems();
        mgr.loadCharacters();
        mgr.runCharacters();
        mgr.assignAllToTask(new PlanStep(PlanAction.TASKS, "monsters", 1, "Manual task complettion"));
        mgr.standbyMode();
    }


    public static void getListOfCraftableGear() {
        CharacterManager mgr = new CharacterManager();
        Bank.getInstance().refreshBankItems();
        mgr.loadCharacters();
        mgr.runCharacters();
        mgr.pickItemToCraft();
    }

    public static void getHighestMonsterDefeatable() {
        Bank.getInstance().refreshBankItems();
        List<String> characters = List.of("Bobby", "Stuart", "George", "Tim", "Joe");
        for(String characterName : characters) {
            JsonObject characterData = AtomicActions.getCharacter(characterName);
            Character character = Character.fromJson(characterData);
            System.out.println(characterName + " can defeat " + character.combatService.getHighestMonsterDefeatable());
        }

    }

    public static void simulateCharacterBattle(String characterName, String monster) {
        Bank.getInstance().refreshBankItems();
        JsonObject characterData = AtomicActions.getCharacter(characterName);
        Character character = Character.fromJson(characterData);
        CharacterStatSimulator simulator = new CharacterStatSimulator(character);
        simulator.optimizeWeaponFor(monster, MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
        // Weapon override here

        simulator.optomizeArmorFor(monster, MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
        
        // Armor overrides here
        simulator.setGear("body_armor_slot", "serpent_skin_armor");
        simulator.setGear("leg_armor_slot", "serpent_skin_legs_armor");
        
        // Potion overrides
        simulator.setElementPotionBoost("fire", 1.1);
        simulator.setElementPotionBoost("air", 1.1);

        System.out.println(simulator.getLoadout());
        System.out.println(simulator.getDamageBreakdownAgainst(MapManager.getInstance().getByMonsterCode(monster).get(0)));
        
        
        List<String> logs = new ArrayList<>();
        simulator.getPlayerWinsAgainstMonster(monster, logs);
        for(String log : logs) {
            System.out.println(log);
        }
    }

}

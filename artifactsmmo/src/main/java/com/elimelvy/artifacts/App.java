package com.elimelvy.artifacts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.model.PlanStep;
import com.google.gson.JsonObject;

public class App {
    public static void main(String[] args) throws Exception {
        runAllCharactersManually();
        // runCraftingManager();

        // CharacterManager mgr = new CharacterManager();
        // Bank.getInstance().refreshBankItems();
        // mgr.loadCharacters();
        // mgr.runCharacters();
        // "ruby_amulet", "topaz_amulet", "emerald_amulet", "sapphire_amulet", "ring_of_chance", "piggy_pants", 
        // piggy_armor, serpent_skin_legs_armor. serpent_skin_armor
        // List<String> armorToCraft = List.of("topaz_amulet", "emerald_amulet", "sapphire_amulet", "piggy_pants", "piggy_armor", "serpent_skin_legs_armor", "serpent_skin_armor");
        // for(String armor : armorToCraft) {
        //     doCompleteCrafting(armor, 5, mgr);
        // }
        // doCompleteCrafting("ring_of_chance", 10, mgr);

        // runCraftingManagerInLoop(mgr, "steel_ring", (innerMgr) -> innerMgr.getJewelryCrafter().getData().jewelrycraftingLevel <= 25);

        // getListOfCraftableGear();
        // getHighestMonsterDefeatable();

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
        Map<String, PlanStep> assignments = new HashMap<>();
        assignments.put("Bobby", new PlanStep(PlanAction.TASKS, "items", 1, "Manual task complettion"));
        assignments.put("Stuart", new PlanStep(PlanAction.TASKS, "items",1, "Manual task complettion"));
        assignments.put("George", new PlanStep(PlanAction.TASKS, "items", 1, "Manual task complettion"));
        assignments.put("Tim", new PlanStep(PlanAction.TASKS, "items", 1, "Manual task complettion"));
        assignments.put("Joe", new PlanStep(PlanAction.TASKS, "items", 1, "Manual task complettion"));

        List<Thread> threads = new LinkedList<>();
        for (Map.Entry<String, PlanStep> entry : assignments.entrySet()) {
            JsonObject characterData = AtomicActions.getCharacter(entry.getKey());
            Character character = Character.fromJson(characterData);
    
            Thread thread = new Thread(character);
            character.setTask(entry.getValue());
            thread.start();
            
            threads.add(thread);
        }
        threads.get(0).join();
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

}

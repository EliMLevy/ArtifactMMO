package com.elimelvy.artifacts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.model.PlanStep;
import com.google.gson.JsonObject;

public class App {
    public static void main(String[] args) throws Exception {
        // runAllCharactersManually();
        // runCraftingManager();

        CharacterManager mgr = new CharacterManager();
        Bank.getInstance().refreshBankItems();
        mgr.loadCharacters();
        mgr.runCharacters();
        runCraftingManagerInLoop(mgr);

    }

    public static void doCompleteCrafting(String item, int quantity, CharacterManager mgr) throws Exception {
        mgr.setCraftingItem(item, quantity);
        mgr.launchCraftingManager();
        while (!mgr.runCraftingManager()) {
            Thread.sleep(10 * 1000);
        }
        mgr.finishCraftingManager();
    }
    
    public static void runCraftingManagerInLoop(CharacterManager mgr) throws Exception {
        while (true) { 
            doCompleteCrafting("dreadful_amulet",5, mgr);
        }
    }

    public static void runAllCharactersManually() throws Exception {
        Map<String, PlanStep> assignments = new HashMap<>();
        // assignments.put("Bobby", new PlanStep(PlanAction.COLLECT, "dead_wood", 10, "Testing training"));
        assignments.put("Stuart", new PlanStep(PlanAction.ATTACK, "cyclops", 0, "Testing training"));
        // assignments.put("George", new PlanStep(PlanAction.COLLECT, "dead_wood", 10, "Testing training"));
        // assignments.put("Tim", new PlanStep(PlanAction.COLLECT, "dead_wood", 10, "Testing training"));
        // assignments.put("Joe", new PlanStep(PlanAction.COLLECT, "dead_wood", 10, "Testing training"));

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
}

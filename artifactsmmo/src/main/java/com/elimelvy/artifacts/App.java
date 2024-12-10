package com.elimelvy.artifacts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.PlanGenerator.PlanStep;
import com.google.gson.JsonObject;

public class App {
    public static void main(String[] args) throws Exception {
        // runAllCharactersManually();
        runCraftingManager();

    }
    
    public static void runCraftingManager() throws Exception {
        CharacterManager mgr = new CharacterManager();
        Bank.getInstance().refreshBankItems();
        mgr.loadCharacters();
        mgr.runCharacters();
    
        mgr.pickItemToCraft();
        mgr.launchCraftingManager();
        boolean finished = false;
        while(!finished) {
            finished = mgr.runCraftingManager();
            Thread.sleep(60 * 1000); // Check in every minute
        }
    
        mgr.standbyMode();

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

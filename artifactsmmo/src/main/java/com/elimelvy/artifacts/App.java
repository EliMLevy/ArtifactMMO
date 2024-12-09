package com.elimelvy.artifacts;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.PlanGenerator.PlanStep;
import com.google.gson.JsonObject;

public class App 
{
    public static void main( String[] args ) throws Exception
    {

        JsonObject characterJoe = AtomicActions.getCharacter("Joe");
        Character joe = Character.fromJson(characterJoe);
        

        Thread joeThread = new Thread(joe);
        joe.setTask(new PlanStep(PlanAction.COLLECT, "dead_wood", 10, "Testing training"));
        // joe.setTask(new PlanStep(PlanAction.TASKS, "items", 0, "Doing items tasks"));
        joeThread.start();

        // JsonObject characterBobby = AtomicActions.getCharacter("Bobby");
        // Character bobby = Character.fromJson(characterBobby);

        // Thread bobbyThread = new Thread(bobby);
        // bobby.setTask(new PlanStep(PlanAction.TASKS, "items", 0, "Doing items tasks"));
        // bobbyThread.start();

        // JsonObject characterStuart = AtomicActions.getCharacter("Stuart");
        // Character stuart = Character.fromJson(characterStuart);

        // Thread stuartThread = new Thread(stuart);
        // stuart.setTask(new PlanStep(PlanAction.TASKS, "items", 0, "Doing items tasks"));
        // stuartThread.start();

        // JsonObject characterGeorge = AtomicActions.getCharacter("George");
        // Character george = Character.fromJson(characterGeorge);

        // Thread georgeThread = new Thread(george);
        // george.setTask(new PlanStep(PlanAction.TASKS, "items", 0, "Doing items tasks"));
        // georgeThread.start();

        // JsonObject characterTim = AtomicActions.getCharacter("Tim");
        // Character tim = Character.fromJson(characterTim);

        // Thread timThread = new Thread(tim);
        // tim.setTask(new PlanStep(PlanAction.TASKS, "items", 0, "Doing items tasks"));
        // timThread.start();


        joeThread.join();


        

        // CharacterManager mgr = new CharacterManager();
        // Bank.getInstance().refreshBankItems();
        // mgr.loadCharacters();
        // mgr.runCharacters();



        // // mgr.pickItemToCraft();
        // mgr.setCraftingItem("skull_ring", 4);
        // mgr.launchCraftingManager();
        // boolean finished = false;
        // while(!finished) {
        //     finished = mgr.runCraftingManager();
        //     Thread.sleep(5000);
        // }

        // mgr.standbyMode();

    }
}

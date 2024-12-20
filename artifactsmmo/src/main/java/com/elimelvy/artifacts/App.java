package com.elimelvy.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.model.CharacterStatSimulator;
import com.elimelvy.artifacts.model.PlanStep;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.map.MapManager;
import com.google.gson.JsonObject;

public class App {
    public static void main(String[] args) throws Exception {
        CharacterManager mgr = new CharacterManager();
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        int refreshRate = 5;
        scheduled.scheduleAtFixedRate(new EncyclopediaMaker(), 1, refreshRate, TimeUnit.MINUTES);
        
        // Bank.getInstance().refreshBankItems();
        // mgr.loadCharacters();
        // mgr.runCharacters();
        Map<String, PlanStep> interestingEvents = Map.of("bandit_camp", new PlanStep(PlanAction.EVENT, "bandit_lizard", 1, "Bandit event is active!"),
                                                        "snowman", new PlanStep(PlanAction.EVENT, "snowman", 1, "Snowman event is active!"),
                                                        "portal_demon", new PlanStep(PlanAction.EVENT, "demon", 1, "Demon event is active!"));
        EventManager eventMgr = new EventManager(interestingEvents, mgr);
        scheduled.scheduleAtFixedRate(eventMgr, 2, refreshRate, TimeUnit.MINUTES); // offset by 2 minutes so that the encyclopedia is up to date
        new EncyclopediaMaker().run();
        runAllCharactersManually(mgr);

        // getListOfCraftableGear();
        // getHighestMonsterDefeatable();
        // simulateCharacterBattle("Bobby", "lich");

    }

    public static void doCompleteCrafting(String item, int quantity, CharacterManager mgr) throws Exception {
        mgr.setCraftingItem(item, quantity);
        mgr.launchCraftingManager();
        while (!mgr.runCraftingManager()) {
            Thread.sleep(60 * 1000);
        }
        mgr.finishCraftingManager();
    }
    
    public static void runCraftingManagerInLoop(CharacterManager mgr, String item, Predicate<CharacterManager> until) throws Exception {
        while (until.test(mgr)) { 
            doCompleteCrafting(item ,5, mgr);
        }
    }

    public static void runAllCharactersManually(CharacterManager mgr) throws Exception {
        Bank.getInstance().refreshBankItems();
        mgr.loadCharacters();
        mgr.runCharacters();
        // for(int i = 0; i < 2; i++) {
        //     mgr.addToAllQueues(new PlanStep(PlanAction.DEPOSIT, "", 0, "Empty cooked trout"));
        //     mgr.addToAllQueues(new PlanStep(PlanAction.WITHDRAW, "trout", 150, "Everyone cooking trout"));
        //     mgr.addToAllQueues(new PlanStep(PlanAction.CRAFT, "cooked_trout", 150, "Everyone cooking trout"));
        //     mgr.addToAllQueues(new PlanStep(PlanAction.DEPOSIT, "", 0, "Empty cooked trout"));
        // }

        // mgr.forceAllCharactersToDeposit();
        mgr.assignAllToTask(new PlanStep(PlanAction.TASKS, "monsters", 1, "Leveling up characters"));
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
        // simulator.setGear("weapon_slot", "greater_dreadful_staff");

        simulator.optomizeArmorFor(monster, MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
        
        // Armor overrides here
        
        // Potion overrides
        // simulator.setElementPotionBoost("water", 1.1);
        // simulator.setElementPotionResistance("fire", 0.90);
        // simulator.setElementPotionResistance("water", 0.90);
        // simulator.setElementPotionResistance("earth", 0.90);
        // simulator.setElementPotionResistance("air", 0.90);

        System.out.println(simulator.getLoadout());
        System.out.println(simulator.getDamageBreakdownAgainst(MapManager.getInstance().getByMonsterCode(monster).get(0)));
        
        
        List<String> logs = new ArrayList<>();
        simulator.getPlayerWinsAgainstMonster(monster, logs);
        for(String log : logs) {
            System.out.println(log);
        }
    }

}

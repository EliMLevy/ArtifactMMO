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
        CharacterManager mgr = new CharacterManager();
        // ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        // int refreshRate = 10;
        // scheduled.scheduleAtFixedRate(new EncyclopediaMaker(), 1, refreshRate, TimeUnit.MINUTES);
        // Map<String, PlanStep> interestingEvents = Map.of("bandit_camp", new PlanStep(PlanAction.ATTACK, "bandit_lizard", 1, "Bandit event is active!"),
                                                        // "snowman", new PlanStep(PlanAction.ATTACK, "snowman", 1, "Snowman event is active!"),
                                                        // "portal_demon", new PlanStep(PlanAction.ATTACK, "demon", 1, "Demon event is active!"));
        // EventManager eventMgr = new EventManager(interestingEvents, mgr);
        // scheduled.scheduleAtFixedRate(eventMgr, 2, refreshRate, TimeUnit.MINUTES); // offset by 2 minutes so that the encyclopedia is up to date
        // runAllCharactersManually(mgr);
        // runCraftingManager();
        
        Bank.getInstance().refreshBankItems();
        mgr.loadCharacters();
        mgr.runCharacters();
        runCraftingManagerInLoop(mgr, "battlestaff", (innerMgr) -> innerMgr.getWeaponCrafter().getData().weaponcraftingLevel < 30);
        doCompleteCrafting("gold_axe", 5, mgr);
        doCompleteCrafting("gold_pickaxe", 5, mgr);
        doCompleteCrafting("gold_fishing_rod", 5, mgr);
        doCompleteCrafting("elderwood_staff", 5, mgr);
        doCompleteCrafting("golden_gloves", 5, mgr);

        // new EncyclopediaMaker().run();
        // getListOfCraftableGear();
        // getHighestMonsterDefeatable();
        // simulateCharacterBattle("Stuart", "imp");

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

    public static void runAllCharactersManually(CharacterManager mgr) throws Exception {
        Bank.getInstance().refreshBankItems();
        mgr.loadCharacters();
        mgr.runCharacters();
        // for(int i = 0; i < 3; i++) {
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
        simulator.setGear("weapon_slot", "elderwood_staff");

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

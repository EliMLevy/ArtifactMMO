package com.elimelvy.artifacts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.crafting.GearCraftingSorter;
import com.elimelvy.artifacts.model.CharacterStatSimulator;
import com.elimelvy.artifacts.model.InventoryItem;
import com.elimelvy.artifacts.model.PlanStep;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Monster;
import com.google.gson.JsonObject;

public class App {
    public static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        Bank.getInstance().refreshBankItems();
        CharacterManager mgr = new CharacterManager();
        mgr.loadCharacters();
        mgr.runCharacters();
        scheduleEventManager(mgr);
        // encyclopedia is up to date
        // new EncyclopediaMaker().run();

        List<String> gearWithObsidian = List.of(
                "lost_amulet",
                "ruby_ring", // we need 8 more
                "obsidian_legs_armor",
                "obsidian_armor",
                "topaz_ring",
                "obsidian_battleaxe", "sapphire_ring", "emerald_ring");

   

        List<GameItem> items = gearWithObsidian.stream().map(item -> GameItemManager.getInstance().getItem(item))
                .filter(item -> item.craft() != null)
                .sorted(new GearCraftingSorter())
                .toList();

        items.forEach(item -> {
            logger.info("{} {} {}", item.code(), item.level(),
                    GearCraftingSorter.getHighestLevelMonsterIngredient(item.craft().items()));
        });
        // doCompleteCrafting("dreadful_ring", 5, mgr);

        // for (GameItem item : items) {
        // if (item.type().equals("ring")) {
        // doCompleteCrafting(item.code(), 5, mgr);
        // doCompleteCrafting(item.code(), 5, mgr);
        // } else {
        // doCompleteCrafting(item.code(), 5, mgr);
        // }
        // }

        runAllCharactersManually(mgr);

        // makeSpaceInBank(mgr);
        // getListOfCraftableGear(mgr);
        // getHighestMonsterDefeatable();
        // simulateCharacterBattle("Bobby", "lich");

    }

    private static void scheduleEventManager(CharacterManager mgr) {
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        int refreshRate = 5;
        scheduled.scheduleAtFixedRate(new EncyclopediaMaker(), 1, refreshRate, TimeUnit.MINUTES);

        Map<String, PlanStep> interestingEvents = Map.of(
                "bandit_camp", new PlanStep(PlanAction.EVENT, "bandit_lizard", 1, "Bandit event is active!"),
                // "snowman", new PlanStep(PlanAction.EVENT, "snowman", 1, "Snowman event is
                // active!"),
                "portal_demon", new PlanStep(PlanAction.EVENT, "demon", 1, "Demon event is active!"));
        EventManager eventMgr = new EventManager(interestingEvents, mgr);
        scheduled.scheduleAtFixedRate(eventMgr, 2, refreshRate, TimeUnit.MINUTES); // offset by 2 minutes so that the
    }

    public static void doCompleteCrafting(String item, int quantity, CharacterManager mgr) throws Exception {
        logger.info("Doing complete crafting of {} x{}", item, quantity);
        mgr.setCraftingItem(item, quantity);
        mgr.launchCraftingManager();
        while (!mgr.runCraftingManager()) {
            Thread.sleep(60 * 1000);
        }
        mgr.finishCraftingManager();
    }

    public static void runCraftingManagerInLoop(CharacterManager mgr, String item, Predicate<CharacterManager> until)
            throws Exception {
        while (!until.test(mgr)) {
            doCompleteCrafting(item, 5, mgr);
        }
    }

    public static void runAllCharactersManually(CharacterManager mgr) throws Exception {
        List<PlanStep> cookTrout = List.of(
            new PlanStep(PlanAction.DEPOSIT, "", 0, "Empty inventory"),
            new PlanStep(PlanAction.GET_DROP, "trout", 150, "withdraw trout"),
            new PlanStep(PlanAction.CRAFT, "cooked_trout", 150, "Craft cooked trout"),
            new PlanStep(PlanAction.DEPOSIT, "", 0, "Empty inventory")
        );
        List<PlanStep> craftWaterBoost = List.of(
                new PlanStep(PlanAction.DEPOSIT, "", 0, "Empty inventory"),
                new PlanStep(PlanAction.GET_DROP, "blue_slimeball", 10, "Collect slimeball"),
                new PlanStep(PlanAction.GET_DROP, "algae", 10, "withdraw algae"),
                new PlanStep(PlanAction.GET_DROP, "sunflower", 10, "withdraw sunflower"),
                new PlanStep(PlanAction.CRAFT, "water_boost_potion", 10, "Craft potion"),
                new PlanStep(PlanAction.DEPOSIT, "", 0, "Empty inventory")
        );

        // mgr.addToAllQueues(new PlanStep(PlanAction.DEPOSIT, "", 0, "Deposit all before starting"));
        // for (int i = 0; i < 2; i++) {
        //     mgr.getCharacter("Bobby").addTasksToQueue(cookTrout);
        //     mgr.getCharacter("George").addTasksToQueue(cookTrout);
        //     mgr.getCharacter("Tim").addTasksToQueue(cookTrout);
        // }

        // Bobby, George and Tim will fight Lich
        // mgr.getCharacter("Bobby").setTask(new PlanStep(PlanAction.ATTACK, "lich", 1, "Trying to drop crown"));
        mgr.getCharacter("George").setTask(new PlanStep(PlanAction.ATTACK, "lich", 1, "Trying to drop crown"));
        mgr.getCharacter("Tim").setTask(new PlanStep(PlanAction.ATTACK, "lich", 1, "Trying to drop crown"));
        
        loopCharacterWithPlan(mgr.getCharacter("Joe"), craftWaterBoost);
        loopCharacterWithPlan(mgr.getCharacter("Bobby"), craftWaterBoost);
        loopCharacterWithPlan(mgr.getCharacter("Stuart"), cookTrout);
        // for(int i = 0; i < 20; i++) {
        //     // Stuart will craft cooked bass
        //     mgr.getCharacter("Stuart").addTasksToQueue(cookTrout);
        //     // Joe will craft water boost potion
        //     mgr.getCharacter("Joe").addTasksToQueue(craftWaterBoost);

        // }

        // mgr.assignSpecificCharacterToTask(character, task);
        // mgr.forceAllCharactersToDeposit();
        // mgr.assignAllToTask(new PlanStep(PlanAction.TASKS, "monsters", 1, "Leveling up characters"));
        mgr.standbyMode();
    }

    public static void loopCharacterWithPlan(Character c, List<PlanStep> plan) {
        Thread t = new Thread(() -> {
            while(true) {
                logger.info("Adding plan to {}", c.getName());
                // Assign the plan to the character
                c.addTasksToQueue(plan);
                // Wait on the last step
                try {
                    plan.get(plan.size() - 1).waitForCompletion();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Repeat
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void getListOfCraftableGear(CharacterManager mgr) {
        // Go through all monsters and keep a set of optimal gear for each monster
        Bank.getInstance().refreshBankItems();
        JsonObject characterData = AtomicActions.getCharacter("Bobby");
        Character character = Character.fromJson(characterData);
        CharacterStatSimulator simulator = new CharacterStatSimulator(character);

        Set<String> importantGear = new HashSet<>();
        for (Monster m : MapManager.getInstance().getMonstersByLevel(0, 35)) {
            simulator.optimizeWeaponFor(m.getCode(), MapManager.getInstance(), GameItemManager.getInstance(),
                    Bank.getInstance());
            simulator.optomizeArmorFor(m.getCode(), MapManager.getInstance(), GameItemManager.getInstance(),
                    Bank.getInstance());

            for (GameItem item : simulator.equippedGear.values()) {
                if (item.craft() != null) {
                    importantGear.add(item.code());
                }
            }
            // logger.info("For {} we will wear {}", m.getCode(), simulator.getLoadout());
        }
        logger.info("Important gear: {}", importantGear);
        Set<String> gearToCraft = new HashSet<>();
        for (String s : importantGear) {
            if (mgr.getOwnershipQuantity(s) > 0) {
                logger.info("We already own: {}", s);
            } else {
                gearToCraft.add(s);
            }
        }
        logger.info("Gear to craft: {}", gearToCraft);
    }

    public static void getHighestMonsterDefeatable() {
        Bank.getInstance().refreshBankItems();
        List<String> characters = List.of("Bobby", "Stuart", "George", "Tim", "Joe");
        for (String characterName : characters) {
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
        simulator.optimizeWeaponFor(monster, MapManager.getInstance(), GameItemManager.getInstance(),
                Bank.getInstance());
        // Weapon override here

        simulator.optomizeArmorFor(monster, MapManager.getInstance(), GameItemManager.getInstance(),
                Bank.getInstance());

        // simulator.setGear("body_armor_slot", "bandit_armor");
        // simulator.setGear("helmet_slot", "obsidian_helmet");
        // Armor overrides here

        // Potion overrides
        simulator.setElementPotionBoost("water", 1.12);
        // simulator.setElementPotionBoost("earth", 1.1);
        // simulator.setElementPotionResistance("fire", 0.90);
        // simulator.setElementPotionResistance("water", 0.85);
        // simulator.setElementPotionResistance("earth", 0.85);
        // simulator.setElementPotionResistance("air", 0.90);

        System.out.println(simulator.getLoadout());
        logger.info("{}", simulator.getLoadout());
        System.out.println(simulator.getDamageBreakdownAgainst(MapManager.getInstance().getMonster(monster)));

        List<String> logs = new ArrayList<>();
        simulator.getPlayerWinsAgainstMonster(monster, logs);
        for (String log : logs) {
            System.out.println(log);
        }
    }

    public static void makeSpaceInBank(CharacterManager mgr) {
        // Go through each monster and make a set of gear that is useful
        // Go through each piece of gear in the bank and recycle non useful gear
        Set<String> usefulGear = new HashSet<>();
        CharacterStatSimulator simulator = new CharacterStatSimulator(mgr.getWeaponCrafter());
        for (Monster m : MapManager.getInstance().getMonstersByLevel(0, 40)) {
            simulator.optimizeForMonster(m.getCode(), MapManager.getInstance(), GameItemManager.getInstance(),
                    Bank.getInstance());
            for (GameItem gear : simulator.equippedGear.values()) {
                usefulGear.add(gear.code());
            }
            // logger.info("{} uses: {}", m.getContentCode(), simulator.getLoadout());
        }
        logger.info("Useful gear: {}", usefulGear);
        Set<String> itemsToRecycle = new HashSet<>();
        for (InventoryItem i : Bank.getInstance().getBankItems()) {
            GameItem item = GameItemManager.getInstance().getItem(i.getCode());
            if (item != null) {
                if (item.craft() != null) {
                    if (GearManager.allGearTypes.contains(item.type()) && !item.subtype().equals("tool")
                            && item.level() < 20) {
                        if (!usefulGear.contains(item.code())) {
                            logger.info("{} is not useful", item.code());
                            itemsToRecycle.add(item.code());
                        }
                    }
                }
            } else {
                logger.error("Could not find {}", i.getCode());
            }
        }
        logger.info("Items to recycle: {}", itemsToRecycle);
        for (String item : itemsToRecycle) {
            // Get quantity
            // While quantity is > 0 assign to characters to recycle in batches of 5
            int quantity = Bank.getInstance().getBankQuantity(item);
            while (quantity > 0) {
                for (Character c : mgr.getCharacters()) {
                    int quantityToRecycle = Math.min(5, quantity);
                    c.addTaskToQueue(new PlanStep(PlanAction.DEPOSIT, null, 0, "Deposit in preparation of recyling"));
                    c.addTaskToQueue(
                            new PlanStep(PlanAction.WITHDRAW, item, quantityToRecycle, "Withdrawing for recycling"));
                    c.addTaskToQueue(new PlanStep(PlanAction.RECYCLE, item, quantityToRecycle, "Recylling"));
                    quantity -= quantityToRecycle;
                    if (quantity <= 0) {
                        break;
                    }
                }
            }
        }

        mgr.standbyMode();

        // Go through each resource in the bank.
        // If it only has one recipe and its cooking, cook it
    }

}

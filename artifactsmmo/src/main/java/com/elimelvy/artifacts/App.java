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
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        int refreshRate = 5;
        scheduled.scheduleAtFixedRate(new EncyclopediaMaker(), 1, refreshRate, TimeUnit.MINUTES);

        Map<String, PlanStep> interestingEvents = Map.of("bandit_camp",
                new PlanStep(PlanAction.EVENT, "bandit_lizard", 1, "Bandit event is active!"),
                "snowman", new PlanStep(PlanAction.EVENT, "snowman", 1, "Snowman event is active!"),
                "portal_demon", new PlanStep(PlanAction.EVENT, "demon", 1, "Demon event is active!"));
        EventManager eventMgr = new EventManager(interestingEvents, mgr);
        scheduled.scheduleAtFixedRate(eventMgr, 2, refreshRate, TimeUnit.MINUTES); // offset by 2 minutes so that the
                                                                                   // encyclopedia is up to date
        // new EncyclopediaMaker().run();

        List<String> gearToCraft = List.of(
                 "obsidian_helmet", "lost_amulet",
                "ruby_ring"
                // ,"gold_helm", "royal_skeleton_armor", "obsidian_legs_armor",
                // "gold_platebody",  "obsidian_armor", "dreadful_ring", "lizard_boots",
                //  "greater_dreadful_amulet", "topaz_ring", "lich_crown", "serpent_skin_armor",
                // "death_knight_sword", "lizard_skin_armor",  "obsidian_battleaxe"
                );

        List<GameItem> items = gearToCraft.stream().map(item -> GameItemManager.getInstance().getItem(item))
                .filter(item -> item.craft() != null)
                .sorted(new GearCraftingSorter())
                .toList();

        items.forEach(item -> {
            logger.info("{} {} {}", item.code(), item.level(),
                    GearCraftingSorter.getHighestLevelMonsterIngredient(item.craft().items()));
        });

        runAllCharactersManually(mgr);

        for (GameItem item : items) {
            if (item.type().equals("ring")) {
                doCompleteCrafting(item.code(), 10, mgr);
            } else {
                doCompleteCrafting(item.code(), 5, mgr);
            }
        }

        // makeSpaceInBank(mgr);
        // getListOfCraftableGear(mgr);
        // getHighestMonsterDefeatable();
        // simulateCharacterBattle("Bobby", "cultist_acolyte");

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
        // for (int i = 0; i < 3; i++) {
        //     mgr.addToAllQueues(new PlanStep(PlanAction.DEPOSIT, "", 0, "Empty cooked trout"));
        //     mgr.addToAllQueues(new PlanStep(PlanAction.WITHDRAW, "trout", 150, "Everyone cooking trout"));
        //     mgr.addToAllQueues(new PlanStep(PlanAction.CRAFT, "cooked_trout", 150, "Everyone cooking trout"));
        //     mgr.addToAllQueues(new PlanStep(PlanAction.DEPOSIT, "", 0, "Empty cooked trout"));
        // }

        // mgr.forceAllCharactersToDeposit();
        mgr.assignAllToTask(new PlanStep(PlanAction.TASKS, "monsters", 1, "Leveling up characters"));
        mgr.standbyMode();
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
                new FakeBank());
        // Weapon override here
        // simulator.setGear("weapon_slot", "greater_dreadful_staff");

        simulator.optomizeArmorFor(monster, MapManager.getInstance(), GameItemManager.getInstance(),
                new FakeBank());

        // Armor overrides here

        // Potion overrides
        // simulator.setElementPotionBoost("water", 1.1);
        // simulator.setElementPotionResistance("fire", 0.90);
        // simulator.setElementPotionResistance("water", 0.90);
        // simulator.setElementPotionResistance("earth", 0.90);
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

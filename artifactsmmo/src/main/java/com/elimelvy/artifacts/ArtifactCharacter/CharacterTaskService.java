package com.elimelvy.artifacts.ArtifactCharacter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.AtomicActions;
import com.elimelvy.artifacts.Bank;
import com.elimelvy.artifacts.Character;
import com.elimelvy.artifacts.PlanGenerator;
import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.model.PlanStep;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.Resource;
import com.google.gson.JsonObject;

public class CharacterTaskService {

    private final Character character;
    private final Logger logger;

    public CharacterTaskService(Character character) {
        this.character = character;
        this.logger = LoggerFactory.getLogger("CharacterTaskService." + this.character.getName());
    }

    public void tasks(String type, CharacterMovementService movementService, CharacterInventoryService inventoryService,
            CharacterGearService gearService, CharacterCombatService combatService) {
        // Type could be: monsters or items
        // If we have no task, move to task spot and get new task
        getNewTaskIfWeHaveNone(type, movementService);
        // If we are done with our tasks, move to task spot and complete tasks
        completeTaskIfDone(movementService, inventoryService, gearService);
        // If inv full, deposit
        inventoryService.depositAllItemsIfNecessary(movementService);

        // Do task
        if (character.getData().taskType.equals("items")) {
            doItemTask(inventoryService, movementService, gearService);
        } else {
            combatService.attackMonster(character.getData().task, movementService, gearService, inventoryService);
        }
    }

    private void getNewTaskIfWeHaveNone(String type, CharacterMovementService movementService) {
        if (character.getData().task == null || character.getData().task.isEmpty()) {
            this.logger.info("Getting new {} task", type);
            movementService.moveToMap(type);
            JsonObject result = AtomicActions.acceptNewTask(character.getName());
            character.handleActionResult(result);
        }
    }

    private void completeTaskIfDone(CharacterMovementService movementService,
            CharacterInventoryService inventoryService, CharacterGearService gearService) {
        if (character.getData().taskProgress >= character.getData().taskTotal) {
            this.logger.info("Done with task {}", character.getData().task);
            
            String completedTaskType = character.getData().taskType;
            
            movementService.moveToMap(completedTaskType);
            
            JsonObject result = AtomicActions.completeTask(character.getName());
            character.handleActionResult(result);
            
            // If there is a bunch of task coins, withdraw them and exchange them
            int taskCoins = Bank.getInstance().getBankQuantity("tasks_coin")
                    + inventoryService.getInventoryQuantity("tasks_coin", gearService);
            if (taskCoins > 30) {
                exchangeTaskCoins(completedTaskType, inventoryService, movementService, gearService);
            }
        }
    }

    public void exchangeTaskCoins(String taskMaster, CharacterInventoryService inventoryService,
            CharacterMovementService movementService, CharacterGearService gearService) {
        inventoryService.withdrawFromBank("tasks_coin", Bank.getInstance().getBankQuantity("tasks_coin"),
                movementService);
        movementService.moveToMap(taskMaster); // The task type we just completed is the closest task
                                               // master
        while (inventoryService.getInventoryQuantity("tasks_coin", gearService) >= 6) {
            JsonObject result = AtomicActions.exchangeCoinsWithTaskMaster(character.getName());
            character.handleActionResult(result);
        }
        inventoryService.depositAllItems(movementService);
    }

    private void doItemTask(CharacterInventoryService inventoryService, CharacterMovementService movementService,
            CharacterGearService gearService) {

        // Withdraw as much from the bank as I can and trade it
        int withdrawAmount = Math.min(character.getData().inventoryMaxItems,
                Bank.getInstance().getBankQuantity(character.getData().task));
        withdrawAmount = Math.min(withdrawAmount, character.getData().taskTotal - character.getData().taskProgress);
        if (withdrawAmount > 0) {
            this.logger.info("Trading {} {} for a task", withdrawAmount, character.getData().task);
            inventoryService.depositAllItems(movementService);
            inventoryService.withdrawFromBank(character.getData().task, withdrawAmount, movementService);
            movementService.moveToMap("items");
            JsonObject result = AtomicActions.tradeWithTaskMaster(character.getName(), character.getData().task,
                    withdrawAmount);
            character.handleActionResult(result);
            return;
        }

        // If we have enough in my inventory to complete the task, complete it
        if (inventoryService.getInventoryQuantity(character.getData().task,
                gearService) >= character.getData().taskTotal - character.getData().taskProgress) {
            movementService.moveToMap("items");
            JsonObject result = AtomicActions.tradeWithTaskMaster(character.getName(), character.getData().task,
                    character.getData().taskTotal - character.getData().taskProgress);
            character.handleActionResult(result);
            return;
        }

        // Get gameItem
        GameItem target = GameItemManager.getInstance().getItem(character.getData().task);
        if (target.craft() != null && target.craft().items() != null && !target.craft().items().isEmpty()) {
            // If it has a recipe, generate plan to get it
            inventoryService.depositAllItems(movementService);
            List<PlanStep> plan = PlanGenerator.generatePlan(character.getData().task,
                    character.getData().taskTotal - character.getData().taskProgress,
                    (int) (character.getData().inventoryMaxItems * 0.9));
            this.logger.info("Plan to gather {} {}: {}",
                    character.getData().taskTotal - character.getData().taskProgress, target.code(),
                    plan);
            character.addTasksToQueue(plan);
        } else {
            // Otherwise, find where to collect it
            List<Resource> resources = MapManager.getInstance().getResouce(character.getData().task);
            if (resources != null && !resources.isEmpty()) {
                movementService.moveToMap(resources.get(0).getMapCode());
                character.collectResource(character.getData().task);
            } else {
                // Otherwise, log an error, go into idle
                this.logger.error("Unable to collect {}", character.getData().task);
                character.setTask(new PlanStep(PlanAction.IDLE, "", 0, "Failed to complete task so moving into idle"));
            }

        }

    }

}

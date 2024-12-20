package com.elimelvy.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.model.InventoryItem;
import com.elimelvy.artifacts.model.PlanStep;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.item.RecipeIngredient;

public class PlanGenerator {
    private final static Logger logger = LoggerFactory.getLogger(PlanGenerator.class);

    public enum PlanAction {
        ATTACK,
        CRAFT,
        TRAIN,
        COLLECT,
        TASKS,
        DEPOSIT,
        WITHDRAW,
        IDLE,
        NOOP,
        EVENT
    }

    private static class Plan {
        private final String code;
        private final int quantity;
        private final GameItem item;
        private final int maxInventorySpace;
        private int quantityNeeded;
        private List<Plan> dependencies;
        private PlanAction finalAction;
        private int finalActionQuantity;
        private String description;
        private final List<InventoryItem> bank;
        private int quantityFromBankUsed = 0;

        public Plan(String code, int quantity, int maxInventorySpace, List<InventoryItem> bank, boolean useBank) {
            logger.debug("Creating plan for {} x{} (useBank={})", code, quantity, useBank);
            this.code = code;
            this.quantity = quantity;
            this.quantityNeeded = quantity;
            this.maxInventorySpace = maxInventorySpace;
            this.bank = bank;
            this.dependencies = new ArrayList<>();

            this.item = GameItemManager.getInstance().getItem(code);
            if (this.item == null) {
                throw new IllegalArgumentException("Failed to find item " + code);
            }

            if (useBank) {
                int quantityInBank = getBankQuantity(this.code);
                logger.debug("We have x{} of {} in bank", quantityInBank, code);
                if (quantityInBank >= this.quantityNeeded) {
                    this.quantityFromBankUsed = this.quantityNeeded;
                    setBankQuantity(this.code, quantityInBank - this.quantityNeeded);
                    this.finalAction = PlanAction.NOOP;
                    this.finalActionQuantity = this.quantity;
                    this.description = "we had enough " + this.code + " in the bank. Remaining "
                            + (quantityInBank - this.quantityNeeded);
                    this.quantityNeeded = 0;
                } else {
                    this.quantityNeeded -= quantityInBank;
                    this.quantityFromBankUsed = quantityInBank;
                    setBankQuantity(this.code, 0);

                }
            }

            if (this.quantityNeeded > 0) {
                if (this.item.craft() != null) {
                    logger.debug("Creating dependcies for {}", this.item.code());
                    int sumOfIngredients = 0;
                    List<RecipeIngredient> ingredients = this.item.craft().items();
                    for (RecipeIngredient ingredient : ingredients) {
                        Plan newPlan = new Plan(
                                ingredient.code(),
                                ingredient.quantity() * this.quantityNeeded,
                                this.maxInventorySpace,
                                this.bank,
                                true);
                        sumOfIngredients += newPlan.finalActionQuantity;
                        this.dependencies.add(newPlan);
                    }
                    this.finalAction = PlanAction.CRAFT;
                    this.finalActionQuantity = this.quantityNeeded;
                    if (sumOfIngredients > this.maxInventorySpace) {
                        this.splitStep();
                    }
                } else {

                    logger.debug("Doing {} in one batch of {}", this.code, this.quantityNeeded);
                    this.finalAction = PlanAction.COLLECT;
                    this.finalActionQuantity = this.quantityNeeded;

                }
            }
        }

        private int getBankQuantity(String itemCode) {
            return bank.stream()
                    .filter(itemElem -> itemCode.equals(itemElem.getCode()))
                    .map(itemElem -> itemElem.getQuantity())
                    .findFirst()
                    .orElse(0);
        }

        private void setBankQuantity(String itemCode, int quantity) {
            bank.stream()
                    .filter(itemElem -> itemCode.equals(itemElem.getCode()))
                    .findFirst()
                    .ifPresent(itemElem -> itemElem.setQuantity(quantity));
        }

        public void returnIngredientsToBank() {
            for (Plan dependency : this.dependencies) {
                dependency.returnIngredientsToBank();
            }
            this.setBankQuantity(code, this.getBankQuantity(this.code) + this.quantityFromBankUsed);
        }

        private void splitStep() {
            if (this.item.craft() == null) {
                return;
            }

            logger.debug("Splitting {}", this.item.code());

            List<RecipeIngredient> ingredients = this.item.craft().items();

            int totalItemsPerCraft = ingredients.stream()
                    .mapToInt(ingredient -> ingredient.quantity())
                    .sum();

            int maxCraftable = this.maxInventorySpace / totalItemsPerCraft;
            int maxBatchSize = Math.min(this.quantity, maxCraftable);

            // Before we trash our list of dependencies we need to return all the items that
            // were used from the bank.
            this.returnIngredientsToBank();
            this.dependencies = new ArrayList<>();
            int quantityRemaining = this.quantity;
            int batchesNeeded = (int) Math.ceil((double) this.quantity / maxBatchSize);
            for (int i = 0; i < batchesNeeded; i++) {
                if (quantityRemaining < 0) {
                    throw new IllegalStateException(
                            "Logic error for " + this.code + " " + this.quantity + " " + maxBatchSize);
                }
                this.dependencies.add(
                        new Plan(this.code, Math.min(maxBatchSize, quantityRemaining), this.maxInventorySpace,
                                this.bank, true));
                quantityRemaining -= maxBatchSize;
            }
            this.finalAction = PlanAction.NOOP;
            this.finalActionQuantity = this.quantity;
            this.description = "split " + this.code + " x" + this.quantity + " into " + batchesNeeded + " batches";
        }

        public List<PlanStep> getExecutable(List<PlanStep> executable) {
            for (Plan dependency : this.dependencies) {
                dependency.getExecutable(executable);
                executable.add(new PlanStep(PlanAction.DEPOSIT, "", 0, "Deposit all"));
            }
            if (this.finalAction != null && this.finalAction != PlanAction.NOOP) {
                for (Plan dependency : this.dependencies) {
                    executable.add(new PlanStep(PlanAction.WITHDRAW, dependency.code, dependency.quantity,
                            dependency.description));
                }
                executable.add(new PlanStep(this.finalAction, this.code, this.finalActionQuantity, this.description));
            }
            return executable;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("====PLAN FOR: " + this.code + " x" + this.quantity + "====\n");
            result.append("dependencies:\n");
            for (Plan dependency : this.dependencies) {
                result.append(dependency);
            }
            if (this.dependencies.isEmpty()) {
                result.append("No dependencies\n");
            }
            result.append("final action: ").append(this.finalAction).append("\n");
            result.append("==============================\n");
            return result.toString();
        }
    }

    /**
     * The plan generator wont look inside inventories becuase those may be
     * unavailable to the crafter. Make sure that everyone with important
     * ingredients deposits their inventory BEFORE generating a plan.
     * 
     * @param character
     * @param itemCode
     * @param quantity
     * @param maxInventorySpace
     * @return
     */
    public static List<PlanStep> generatePlan(String itemCode, int quantity,
            int maxInventorySpace) {
        List<InventoryItem> bank = Bank.getInstance().getBankItems().stream()
                .map(item -> new InventoryItem(item.getSlot(), item.getCode(), item.getQuantity()))
                .collect(Collectors.toList());
        Plan plan = new Plan(itemCode, quantity, maxInventorySpace, bank, false);
        List<PlanStep> result = plan.getExecutable(new ArrayList<>());

        List<PlanStep> postProcessed = new ArrayList<>();
        PlanAction lastAction = null;
        for (PlanStep action : result) {
            if (action.action == PlanAction.DEPOSIT && lastAction == PlanAction.DEPOSIT) {
                continue;
            }
            lastAction = action.action;
            postProcessed.add(action);
        }

        if (result.isEmpty()) {
            return List.of(new PlanStep(PlanAction.WITHDRAW, itemCode, quantity,
                    "It was all in the bank so we can just withdraw"));
        } else {
            return postProcessed;
        }
    }

    public static void main(String[] args) {
        Bank.getInstance().refreshBankItems();
        List<PlanStep> executable = generatePlan("copper", 18, 120);
        executable.forEach(System.out::println);
    }

}

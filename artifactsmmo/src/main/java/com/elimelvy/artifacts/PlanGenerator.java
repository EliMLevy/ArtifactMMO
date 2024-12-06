package com.elimelvy.artifacts;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.model.InventoryItem;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.item.RecipeIngredient;

public class PlanGenerator {
    private final static Logger logger = LoggerFactory.getLogger(PlanGenerator.class);

    public record PlanStep(String action, String code, int quantity, String description) {};

    private static class Plan {
        private final String code;
        private final int quantity;
        private final GameItem item;
        private final int maxInventorySpace;
        private int quantityNeeded;
        private List<Plan> dependencies;
        private String finalAction;
        private int finalActionQuantity;
        private String finalActionCode;
        private String description;
        private final List<InventoryItem> bank;


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
                    setBankQuantity(this.code, quantityInBank - this.quantityNeeded);
                    this.finalAction = "noop";
                    this.finalActionCode = this.code;
                    this.finalActionQuantity = this.quantity;
                    this.description = "we had enough " + this.code + " in the bank. Remaining "
                                    + (quantityInBank - this.quantityNeeded);
                    this.quantityNeeded = 0;
                } else {
                    this.quantityNeeded -= quantityInBank;
                }
            }

            if (this.quantityNeeded > 0) {
                if (this.item.recipe() != null) {
                    int sumOfIngredients = 0;
                    List<RecipeIngredient> ingredients = this.item.recipe().items();
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
                    this.finalAction = "craft";
                    this.finalActionCode = this.code;
                    this.finalActionQuantity = this.quantityNeeded;
                    if (sumOfIngredients > this.maxInventorySpace) {
                        this.splitStep();
                    }
                } else {
                    logger.error("Did not have enough {} in bank and it is not craftable...", this.code);
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

        private void splitStep() {
            if (this.item.recipe() == null) {
                return;
            }

            List<RecipeIngredient> ingredients = this.item.recipe().items();
            int totalItemsPerCraft = ingredients.stream()
                    .mapToInt(ingredient -> ingredient.quantity())
                    .sum();

            int maxCraftable = this.maxInventorySpace / totalItemsPerCraft;
            int maxBatchSize = Math.min(this.quantity, maxCraftable);

            this.dependencies = new ArrayList<>();
            int quantityRemaining = this.quantity;
            int batchesNeeded = (int) Math.ceil((double) this.quantity / maxBatchSize);
            for (int i = 0; i < batchesNeeded; i++) {
                if (quantityRemaining < 0) {
                    throw new IllegalStateException("Logic error for " + this.code + " " + this.quantity + " " + maxBatchSize);
                }
                this.dependencies.add(
                        new Plan(this.code, Math.min(maxBatchSize, quantityRemaining), this.maxInventorySpace,
                                this.bank, true));
                quantityRemaining -= maxBatchSize;
            }
            this.finalAction = "noop";
            this.finalActionCode = this.code;
            this.finalActionQuantity = this.quantity;
            this.description = "split " + this.code + " x" + this.quantity + " into " + batchesNeeded + " batches";
        }

        public List<PlanStep> getExecutable(List<PlanStep> executable) {
            for (Plan dependency : this.dependencies) {
                dependency.getExecutable(executable);
                executable.add(new PlanStep("deposit", "", 0, "Deposit all"));
            }
            if (!this.finalAction.equals("noop")) {
                for (Plan dependency : this.dependencies) {
                    executable.add(new PlanStep("withdraw", dependency.finalActionCode, dependency.finalActionQuantity, dependency.description));
                }
                executable.add(new PlanStep(this.finalAction, this.finalActionCode, this.finalActionQuantity, this.description));
            }
            return executable;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("====PLAN FOR: " + this.code + "====\n");
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
    public static List<PlanStep> generatePlan(Character character, String itemCode, int quantity,
            int maxInventorySpace) {
        List<InventoryItem> bank = Bank.getInstance().getBankItems().stream().map(item -> new InventoryItem(item.getSlot(), item.getCode(), item.getQuantity())).collect(Collectors.toList());
        Plan plan = new Plan(itemCode, quantity, maxInventorySpace, bank, false);
        List<PlanStep> result = plan.getExecutable(new ArrayList<>());

        List<PlanStep> postProcessed = new ArrayList<>();
        String lastAction = "";
        for (PlanStep action : result) {
            if (action.action().equals("deposit") && "deposit".equals(lastAction)) {
                continue;
            }
            lastAction = action.action();
            postProcessed.add(action);
        }

        if (result.isEmpty()) {
            return List.of(new PlanStep("withdraw", itemCode, quantity, "It was all in the bank so we can just withdraw"));
        } else {
            return postProcessed;
        }
    }
    public static void main(String[] args) {
        Bank.getInstance().refreshBankItems();
        List<PlanStep> executable = generatePlan(null, "fire_staff", 5, 120);
        executable.forEach(System.out::println);
    }

}

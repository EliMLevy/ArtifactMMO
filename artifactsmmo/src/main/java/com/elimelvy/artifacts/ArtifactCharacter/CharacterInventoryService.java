package com.elimelvy.artifacts.ArtifactCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.AtomicActions;
import com.elimelvy.artifacts.Bank;
import com.elimelvy.artifacts.Character;
import com.elimelvy.artifacts.GearManager;
import com.elimelvy.artifacts.model.InventoryItem;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.google.gson.JsonObject;

public class CharacterInventoryService {
    private final Logger logger;
    private final Character character;

    public CharacterInventoryService(Character character) {
        this.character = character;
        this.logger = LoggerFactory.getLogger("CharacterInventoryService." + character.getName());
    }

    /**
     * Get total quantity of an item, including equipped and inventory items
     * 
     * @param code Item code to check
     * @return Total quantity of the item
     */
    public int getInventoryQuantity(String code, CharacterGearService gearService) {
        GameItem item = GameItemManager.getInstance().getItem(code);
        int equippedQuantity = 0;

        // Handle gear items
        if (GearManager.allGearTypes.contains(item.type())) {
            equippedQuantity += gearService.getQuantityEquipped(item);
        }

        return equippedQuantity + getInventoryQuantityWithoutEquipped(code);
    }

    /**
     * Get inventory quantity of an item, excluding equipped items
     * 
     * @param code Item code to check
     * @return Quantity of the item in inventory
     */
    public int getInventoryQuantityWithoutEquipped(String code) {
        return character.getData().inventory.stream()
                .filter(item -> code.equals(item.getCode()))
                .findFirst()
                .map(InventoryItem::getQuantity)
                .orElse(0);
    }

    /**
     * Check if inventory is near full
     * 
     * @param fullThreshold Threshold for considering inventory full (e.g., 0.9)
     * @return true if inventory is near full, false otherwise
     */
    public boolean isInventoryNearFull(double fullThreshold) {
        int currentHolding = character
                .getData().inventory.stream()
                .mapToInt(InventoryItem::getQuantity)
                .sum();
        return currentHolding / (double) character.getData().inventoryMaxItems > fullThreshold;
    }

    /**
     * Deposit all items from inventory to bank
     * 
     * @return List of deposited items
     */
    public void depositAllItems(CharacterMovementService movementService) {
        // Move to bank if necessary
        movementService.moveToClosestBank();
        // For each item in inv, deposit
        for (InventoryItem item : new ArrayList<>(character.getData().inventory)) {
            if (item.getQuantity() > 0) {
                JsonObject result = AtomicActions.depositItem(character.getName(), item.getCode(),
                        item.getQuantity());
                character.handleActionResult(result);
            }
        }
    }

    public void depositAllItemsIfNecessary(CharacterMovementService movementService) {
        if (this.isInventoryNearFull(0.9)) {
            this.depositAllItems(movementService);
        }
    }

    /**
     * Withdraw items from bank
     * 
     * @param code     Item code to withdraw
     * @param quantity Quantity to withdraw
     * @return Actual quantity withdrawn
     */
    public void withdrawFromBank(String code, int quantity, CharacterMovementService movementService) {
        if (Bank.getInstance().getBankQuantity(code) < quantity) {
            this.logger.error("Cant with {} becuase there isnt enough. There is {}",
                    Bank.getInstance().getBankQuantity(code));
            return;
        }

        movementService.moveToClosestBank();

        JsonObject result = AtomicActions.withdrawItem(character.getName(), code, quantity);
        character.handleActionResult(result);
    }

    /**
     * Find and use consumable items for healing
     * 
     * @param maxHp     Character's maximum HP
     * @param currentHp Character's current HP
     * @return Total healing performed
     */
    public void useConsumablesForHealing(CharacterGearService gearService) {
        // Sort consumables by healing value, descending
        List<GameItem> consumables = character.getData().inventory.stream()
                .map(inv -> GameItemManager.getInstance().getItem(inv.getCode()))
                .filter(item -> item != null && item.type().equals("consumable"))
                .sorted((a, b) -> Double.compare(
                        GearManager.getEffectValue(b, "heal"),
                        GearManager.getEffectValue(a, "heal")))
                .collect(Collectors.toList());

        for (GameItem food : consumables) {
            double healAmount = GearManager.getEffectValue(food, "heal");
            int eatAmount = Math.min(
                    (int) Math.floor((character.getData().maxHp - character.getData().hp) / healAmount),
                    this.getInventoryQuantity(food.code(), gearService));
            if (eatAmount > 0) {
                JsonObject result = AtomicActions.useItem(character.getName(), food.code(), eatAmount);
                character.handleActionResult(result);
            }
        }
    }

    public void fillUpOnConsumables(List<String> candidateConsumables, CharacterGearService gearService,
            CharacterMovementService movementService) {
        int targetQuantity = 25;
        for (String food : candidateConsumables) {
            if (getInventoryQuantity(food, gearService) == 0
                    && Bank.getInstance().getBankQuantity(food) >= targetQuantity) {
                this.withdrawFromBank(food, Math.min(Bank.getInstance().getBankQuantity(food), targetQuantity),
                        movementService);
            }
        }
    }

    /**
     * Check if character has enough ingredients for crafting
     * 
     * @param item Item to be crafted
     * @return true if all ingredients are available, false otherwise
     */
    public boolean hasIngredientsForCrafting(GameItem item, CharacterGearService gearService) {
        if (item == null) {
            this.logger.warn("The requested item is null");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
            return false;
        }
        if (item.craft() == null || item.craft().items() == null) {
            this.logger.warn("The requested item is not craftable. {}", item.code());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
            return false;
        }

        return item
                .craft()
                .items()
                .stream()
                .allMatch(ingredient -> this.getInventoryQuantity(ingredient.code(), gearService) >= ingredient
                        .quantity());
    }
}
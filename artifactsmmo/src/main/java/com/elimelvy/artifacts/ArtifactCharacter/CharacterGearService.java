package com.elimelvy.artifacts.ArtifactCharacter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.AtomicActions;
import com.elimelvy.artifacts.Bank;
import com.elimelvy.artifacts.Character;
import com.elimelvy.artifacts.GearManager;
import com.elimelvy.artifacts.model.item.GameItem;
import com.elimelvy.artifacts.model.item.GameItemManager;
import com.elimelvy.artifacts.model.map.MapManager;
import com.google.gson.JsonObject;

public class CharacterGearService {

    private final Character character;
    private final Logger logger;

    public CharacterGearService(Character character) {
        this.character = character;
        this.logger = LoggerFactory.getLogger("CharacterGearService." + this.character.getName());
    }

    public String getGearInSlot(String slot) {
        return switch (slot) {
            case "weapon_slot" -> this.character.getData().weaponSlot;
            case "shield_slot" -> this.character.getData().shieldSlot;
            case "helmet_slot" -> this.character.getData().helmetSlot;
            case "body_armor_slot" -> this.character.getData().bodyArmorSlot;
            case "leg_armor_slot" -> this.character.getData().legArmorSlot;
            case "boots_slot" -> this.character.getData().bootsSlot;
            case "ring1_slot" -> this.character.getData().ring1Slot;
            case "ring2_slot" -> this.character.getData().ring2Slot;
            case "amulet_slot" -> this.character.getData().amuletSlot;
            default -> null;
        };
    }

    /**
     * Equip gear in the specified slot.
     * Unequips and deposits other gear if necessary. Withdraws the specified gear
     * if necessary.
     * 
     * @param slot the name of the slot ex. weapon_slot
     * @param code item code
     */
    public void equipGear(String slot, String code, CharacterInventoryService inventoryService,
            CharacterMovementService movementService, CharacterCombatService combatService) {
        // Check if it is already equipped
        if (getGearInSlot(slot) != null && getGearInSlot(slot).equals(code)) {
            return;
        }

        this.logger.info("Need to equip {} in {}", code, slot);

        // Unequip if necessary
        if (getGearInSlot(slot) != null && !getGearInSlot(slot).isEmpty()) {
            this.logger.info("To equip {} we need to unequip {}", code, this.getGearInSlot(slot));
            combatService.healIfNecessary(inventoryService, this);
            JsonObject result = AtomicActions.unequip(this.character.getName(), slot.replace("_slot", ""));
            character.handleActionResult(result);
        }

        // Withdraw item if necessary
        if (inventoryService.getInventoryQuantityWithoutEquipped(code) == 0) {
            // Withdraw it from the bank
            this.logger.info("{} not found in inventory so I need to withdraw it", code);
            if (Bank.getInstance().getBankQuantity(code) > 0) {
                inventoryService.depositAllItems(movementService);
                inventoryService.withdrawFromBank(code, 1, movementService);
            } else {
                logger.warn("Attempted to equip gear that is not available: {}", code);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    this.logger.error("Interupted!", e);
                }
            }
        }
        // If the withdrawal was successful or if we already had it, equip
        if (inventoryService.getInventoryQuantityWithoutEquipped(code) > 0) {
            this.logger.info("Equipping {} into {}", code, slot);
            JsonObject result = AtomicActions.equip(this.character.getName(), code, slot.replace("_slot", ""));
            character.handleActionResult(result);
        } else {
            this.logger.warn("I was expecting to have {} in my inventory but found none", code);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.logger.error("Interupted!", e);
            }
        }
    }

    public void equipGearForBattle(String monster, CharacterInventoryService inventoryService,
            CharacterMovementService movementService, CharacterCombatService combatService) {
        String selection = GearManager.getBestWeaponAgainstMonster(character,
                monster, MapManager.getInstance(), GameItemManager.getInstance(), Bank.getInstance());
        equipGear("weapon_slot", selection, inventoryService, movementService, combatService);
        for (String gearType : GearManager.allNonWeaponTypes) {
            if (!gearType.equals("ring")) {
                selection = GearManager.getBestAvailableGearAgainstMonster(character,
                        getGearInSlot("weapon_slot"), gearType, monster, MapManager.getInstance(),
                        GameItemManager.getInstance(), Bank.getInstance());
                equipGear(gearType + "_slot", selection, inventoryService, movementService, combatService);
            } else {
                // There are two ring slots
                selection = GearManager.getBestAvailableGearAgainstMonster(character,
                        getGearInSlot("weapon_slot"), gearType + "1", monster, MapManager.getInstance(),
                        GameItemManager.getInstance(), Bank.getInstance());
                equipGear(gearType + "1_slot", selection, inventoryService, movementService, combatService);
                selection = GearManager.getBestAvailableGearAgainstMonster(character,
                        getGearInSlot("weapon_slot"), gearType + "2", monster, MapManager.getInstance(),
                        GameItemManager.getInstance(), Bank.getInstance());
                equipGear(gearType + "2_slot", selection, inventoryService, movementService, combatService);
            }
        }
    }

    public int getQuantityEquipped(GameItem item) {
        int equippedQuantity = 0;
        if (!item.type().equals("ring")) {
            // Check non-ring gear slots
            if (item.code().equals(getGearInSlot(item.type() + "_slot"))) {
                equippedQuantity += 1;
            }
        } else {
            // Check both ring slots
            if (item.code().equals(getGearInSlot("ring1_slot"))) {
                equippedQuantity += 1;
            }
            if (item.code().equals(getGearInSlot("ring2_slot"))) {
                equippedQuantity += 1;
            }
        }
        return equippedQuantity;
    }

}

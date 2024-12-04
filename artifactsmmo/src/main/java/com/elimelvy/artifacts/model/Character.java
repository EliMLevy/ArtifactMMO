package com.elimelvy.artifacts.model;

import java.time.Instant;
import java.util.List;

import com.elimelvy.artifacts.util.InstantTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Character {

    // Basic character information
    private String name;
    private String account;
    private String skin;
    private int level;
    private int xp;
    private int maxXp;
    private int gold;
    private int speed;

    // Skill levels and XP
    @SerializedName("mining_level")
    private int miningLevel;
    @SerializedName("mining_xp")
    private int miningXp;
    @SerializedName("mining_max_xp")
    private int miningMaxXp;

    @SerializedName("woodcutting_level")
    private int woodcuttingLevel;
    @SerializedName("woodcutting_xp")
    private int woodcuttingXp;
    @SerializedName("woodcutting_max_xp")
    private int woodcuttingMaxXp;

    @SerializedName("fishing_level")
    private int fishingLevel;
    @SerializedName("fishing_xp")
    private int fishingXp;
    @SerializedName("fishing_max_xp")
    private int fishingMaxXp;

    @SerializedName("weaponcrafting_level")
    private int weaponcraftingLevel;
    @SerializedName("weaponcrafting_xp")
    private int weaponcraftingXp;
    @SerializedName("weaponcrafting_max_xp")
    private int weaponcraftingMaxXp;

    @SerializedName("gearcrafting_level")
    private int gearcraftingLevel;
    @SerializedName("gearcrafting_xp")
    private int gearcraftingXp;
    @SerializedName("gearcrafting_max_xp")
    private int gearcraftingMaxXp;

    @SerializedName("jewelrycrafting_level")
    private int jewelrycraftingLevel;
    @SerializedName("jewelrycrafting_xp")
    private int jewelrycraftingXp;
    @SerializedName("jewelrycrafting_max_xp")
    private int jewelrycraftingMaxXp;

    @SerializedName("cooking_level")
    private int cookingLevel;
    @SerializedName("cooking_xp")
    private int cookingXp;
    @SerializedName("cooking_max_xp")
    private int cookingMaxXp;

    @SerializedName("alchemy_level")
    private int alchemyLevel;
    @SerializedName("alchemy_xp")
    private int alchemyXp;
    @SerializedName("alchemy_max_xp")
    private int alchemyMaxXp;

    // Combat and defense stats
    private int hp;
    private int maxHp;
    private int haste;
    @SerializedName("critical_strike")
    private int criticalStrike;
    private int stamina;

    // Elemental attacks and damage
    @SerializedName("attack_fire")
    private int attackFire;
    @SerializedName("attack_earth")
    private int attackEarth;
    @SerializedName("attack_water")
    private int attackWater;
    @SerializedName("attack_air")
    private int attackAir;

    @SerializedName("dmg_fire")
    private int dmgFire;
    @SerializedName("dmg_earth")
    private int dmgEarth;
    @SerializedName("dmg_water")
    private int dmgWater;
    @SerializedName("dmg_air")
    private int dmgAir;

    // Elemental resistances
    @SerializedName("res_fire")
    private int resFire;
    @SerializedName("res_earth")
    private int resEarth;
    @SerializedName("res_water")
    private int resWater;
    @SerializedName("res_air")
    private int resAir;

    // Position and cooldown
    private int x;
    private int y;
    private int cooldown;
    @SerializedName("cooldown_expiration")
    private Instant cooldownExpiration;

    // Equipment slots
    @SerializedName("weapon_slot")
    private String weaponSlot;
    @SerializedName("shield_slot")
    private String shieldSlot;
    @SerializedName("helmet_slot")
    private String helmetSlot;
    @SerializedName("body_armor_slot")
    private String bodyArmorSlot;
    @SerializedName("leg_armor_slot")
    private String legArmorSlot;
    @SerializedName("boots_slot")
    private String bootsSlot;
    @SerializedName("ring1_slot")
    private String ring1Slot;
    @SerializedName("ring2_slot")
    private String ring2Slot;
    @SerializedName("amulet_slot")
    private String amuletSlot;
    @SerializedName("artifact1_slot")
    private String artifact1Slot;
    @SerializedName("artifact2_slot")
    private String artifact2Slot;
    @SerializedName("artifact3_slot")
    private String artifact3Slot;
    @SerializedName("utility1_slot")
    private String utility1Slot;
    @SerializedName("utility1_slot_quantity")
    private int utility1SlotQuantity;
    @SerializedName("utility2_slot")
    private String utility2Slot;
    @SerializedName("utility2_slot_quantity")
    private int utility2SlotQuantity;

    // Task information
    private String task;
    @SerializedName("task_type")
    private String taskType;
    @SerializedName("task_progress")
    private int taskProgress;
    @SerializedName("task_total")
    private int taskTotal;

    // Inventory
    @SerializedName("inventory_max_items")
    private int inventoryMaxItems;
    private List<InventoryItem> inventory;
    

    @Override
    public String toString() {
        return "Character{" +
                "name='" + name + '\'' +
                ", level=" + level +
                ", xp=" + xp + "/" + maxXp +
                ", gold=" + gold +
                '}';
    }

    // Method to parse JSON directly
    public static Character fromJson(JsonObject jsonObject) {
        Gson gson = InstantTypeAdapter.createGsonWithInstant();
        return gson.fromJson(jsonObject.get("data"), Character.class);
    }

    public String getName() {
        return name;
    }

    public String getAccount() {
        return account;
    }

    public String getSkin() {
        return skin;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getMaxXp() {
        return maxXp;
    }

    public int getGold() {
        return gold;
    }

    public int getSpeed() {
        return speed;
    }

    public int getMiningLevel() {
        return miningLevel;
    }

    public int getMiningXp() {
        return miningXp;
    }

    public int getMiningMaxXp() {
        return miningMaxXp;
    }

    public int getWoodcuttingLevel() {
        return woodcuttingLevel;
    }

    public int getWoodcuttingXp() {
        return woodcuttingXp;
    }

    public int getWoodcuttingMaxXp() {
        return woodcuttingMaxXp;
    }

    public int getFishingLevel() {
        return fishingLevel;
    }

    public int getFishingXp() {
        return fishingXp;
    }

    public int getFishingMaxXp() {
        return fishingMaxXp;
    }

    public int getWeaponcraftingLevel() {
        return weaponcraftingLevel;
    }

    public int getWeaponcraftingXp() {
        return weaponcraftingXp;
    }

    public int getWeaponcraftingMaxXp() {
        return weaponcraftingMaxXp;
    }

    public int getGearcraftingLevel() {
        return gearcraftingLevel;
    }

    public int getGearcraftingXp() {
        return gearcraftingXp;
    }

    public int getGearcraftingMaxXp() {
        return gearcraftingMaxXp;
    }

    public int getJewelrycraftingLevel() {
        return jewelrycraftingLevel;
    }

    public int getJewelrycraftingXp() {
        return jewelrycraftingXp;
    }

    public int getJewelrycraftingMaxXp() {
        return jewelrycraftingMaxXp;
    }

    public int getCookingLevel() {
        return cookingLevel;
    }

    public int getCookingXp() {
        return cookingXp;
    }

    public int getCookingMaxXp() {
        return cookingMaxXp;
    }

    public int getAlchemyLevel() {
        return alchemyLevel;
    }

    public int getAlchemyXp() {
        return alchemyXp;
    }

    public int getAlchemyMaxXp() {
        return alchemyMaxXp;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getHaste() {
        return haste;
    }

    public int getCriticalStrike() {
        return criticalStrike;
    }

    public int getStamina() {
        return stamina;
    }

    public int getAttackFire() {
        return attackFire;
    }

    public int getAttackEarth() {
        return attackEarth;
    }

    public int getAttackWater() {
        return attackWater;
    }

    public int getAttackAir() {
        return attackAir;
    }

    public int getDmgFire() {
        return dmgFire;
    }

    public int getDmgEarth() {
        return dmgEarth;
    }

    public int getDmgWater() {
        return dmgWater;
    }

    public int getDmgAir() {
        return dmgAir;
    }

    public int getResFire() {
        return resFire;
    }

    public int getResEarth() {
        return resEarth;
    }

    public int getResWater() {
        return resWater;
    }

    public int getResAir() {
        return resAir;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Instant getCooldownExpiration() {
        return cooldownExpiration;
    }

    public String getWeaponSlot() {
        return weaponSlot;
    }

    public String getShieldSlot() {
        return shieldSlot;
    }

    public String getHelmetSlot() {
        return helmetSlot;
    }

    public String getBodyArmorSlot() {
        return bodyArmorSlot;
    }

    public String getLegArmorSlot() {
        return legArmorSlot;
    }

    public String getBootsSlot() {
        return bootsSlot;
    }

    public String getRing1Slot() {
        return ring1Slot;
    }

    public String getRing2Slot() {
        return ring2Slot;
    }

    public String getAmuletSlot() {
        return amuletSlot;
    }

    public String getArtifact1Slot() {
        return artifact1Slot;
    }

    public String getArtifact2Slot() {
        return artifact2Slot;
    }

    public String getArtifact3Slot() {
        return artifact3Slot;
    }

    public String getUtility1Slot() {
        return utility1Slot;
    }

    public int getUtility1SlotQuantity() {
        return utility1SlotQuantity;
    }

    public String getUtility2Slot() {
        return utility2Slot;
    }

    public int getUtility2SlotQuantity() {
        return utility2SlotQuantity;
    }

    public String getTask() {
        return task;
    }

    public String getTaskType() {
        return taskType;
    }

    public int getTaskProgress() {
        return taskProgress;
    }

    public int getTaskTotal() {
        return taskTotal;
    }

    public int getInventoryMaxItems() {
        return inventoryMaxItems;
    }

    public List<InventoryItem> getInventory() {
        return inventory;
    }

    public int getInventoryQuantity(String code) {
        for (InventoryItem i : this.inventory) {
            if (code.equals(i.getCode())) {
                return i.getQuantity();
            }
        }
        return 0;
    }

    public void collectResource(String code) {
        // TODO
    } 
}
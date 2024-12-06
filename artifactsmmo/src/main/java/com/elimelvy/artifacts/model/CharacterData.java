package com.elimelvy.artifacts.model;

import java.time.Instant;
import java.util.List;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class CharacterData {

    
    // Basic character information
    public String name;
    public String account;
    public String skin;
    public int level;
    public int xp;
    public int maxXp;
    public int gold;
    public int speed;

    // Skill levels and XP
    @SerializedName("mining_level")
    public int miningLevel;
    @SerializedName("mining_xp")
    public int miningXp;
    @SerializedName("mining_max_xp")
    public int miningMaxXp;

    @SerializedName("woodcutting_level")
    public int woodcuttingLevel;
    @SerializedName("woodcutting_xp")
    public int woodcuttingXp;
    @SerializedName("woodcutting_max_xp")
    public int woodcuttingMaxXp;

    @SerializedName("fishing_level")
    public int fishingLevel;
    @SerializedName("fishing_xp")
    public int fishingXp;
    @SerializedName("fishing_max_xp")
    public int fishingMaxXp;

    @SerializedName("weaponcrafting_level")
    public int weaponcraftingLevel;
    @SerializedName("weaponcrafting_xp")
    public int weaponcraftingXp;
    @SerializedName("weaponcrafting_max_xp")
    public int weaponcraftingMaxXp;

    @SerializedName("gearcrafting_level")
    public int gearcraftingLevel;
    @SerializedName("gearcrafting_xp")
    public int gearcraftingXp;
    @SerializedName("gearcrafting_max_xp")
    public int gearcraftingMaxXp;

    @SerializedName("jewelrycrafting_level")
    public int jewelrycraftingLevel;
    @SerializedName("jewelrycrafting_xp")
    public int jewelrycraftingXp;
    @SerializedName("jewelrycrafting_max_xp")
    public int jewelrycraftingMaxXp;

    @SerializedName("cooking_level")
    public int cookingLevel;
    @SerializedName("cooking_xp")
    public int cookingXp;
    @SerializedName("cooking_max_xp")
    public int cookingMaxXp;

    @SerializedName("alchemy_level")
    public int alchemyLevel;
    @SerializedName("alchemy_xp")
    public int alchemyXp;
    @SerializedName("alchemy_max_xp")
    public int alchemyMaxXp;

    // Combat and defense stats
    public int hp;
    @SerializedName("max_hp")
    public int maxHp;
    public int haste;
    @SerializedName("critical_strike")
    public int criticalStrike;
    public int stamina;

    // Elemental attacks and damage
    @SerializedName("attack_fire")
    public int attackFire;
    @SerializedName("attack_earth")
    public int attackEarth;
    @SerializedName("attack_water")
    public int attackWater;
    @SerializedName("attack_air")
    public int attackAir;

    @SerializedName("dmg_fire")
    public int dmgFire;
    @SerializedName("dmg_earth")
    public int dmgEarth;
    @SerializedName("dmg_water")
    public int dmgWater;
    @SerializedName("dmg_air")
    public int dmgAir;

    // Elemental resistances
    @SerializedName("res_fire")
    public int resFire;
    @SerializedName("res_earth")
    public int resEarth;
    @SerializedName("res_water")
    public int resWater;
    @SerializedName("res_air")
    public int resAir;

    // Position and cooldown
    public int x;
    public int y;
    public int cooldown;
    @SerializedName("cooldown_expiration")
    public Instant cooldownExpiration;

    // Equipment slots
    @SerializedName("weapon_slot")
    public String weaponSlot;
    @SerializedName("shield_slot")
    public String shieldSlot;
    @SerializedName("helmet_slot")
    public String helmetSlot;
    @SerializedName("body_armor_slot")
    public String bodyArmorSlot;
    @SerializedName("leg_armor_slot")
    public String legArmorSlot;
    @SerializedName("boots_slot")
    public String bootsSlot;
    @SerializedName("ring1_slot")
    public String ring1Slot;
    @SerializedName("ring2_slot")
    public String ring2Slot;
    @SerializedName("amulet_slot")
    public String amuletSlot;
    @SerializedName("artifact1_slot")
    public String artifact1Slot;
    @SerializedName("artifact2_slot")
    public String artifact2Slot;
    @SerializedName("artifact3_slot")
    public String artifact3Slot;
    @SerializedName("utility1_slot")
    public String utility1Slot;
    @SerializedName("utility1_slot_quantity")
    public int utility1SlotQuantity;
    @SerializedName("utility2_slot")
    public String utility2Slot;
    @SerializedName("utility2_slot_quantity")
    public int utility2SlotQuantity;

    // Task information
    public String task;
    @SerializedName("task_type")
    public String taskType;
    @SerializedName("task_progress")
    public int taskProgress;
    @SerializedName("task_total")
    public int taskTotal;

    // Inventory
    @SerializedName("inventory_max_items")
    public int inventoryMaxItems;
    public List<InventoryItem> inventory;
    

    
}

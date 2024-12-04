package com.elimelvy.artifacts.model;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

import com.elimelvy.artifacts.util.InstantTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Character {
    // Logger
    public static final Logger logger = Logger.getLogger(Character.class.getName());

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
}
package com.elimelvy.artifacts.model.map;


public class Monster {
    // Monster properties based on the CSV columns
    private final int level;
    private final String resourceCode;
    private final int x;
    private final int y;
    private final int dropChance;
    private final String mapCode;
    private final int attackFire;
    private final int attackEarth;
    private final int attackWater;
    private final int attackAir;
    private final int resFire;
    private final int resEarth;
    private final int resWater;
    private final int resAir;

    // Constructor
    public Monster(int level, String resourceCode, int x, int y, int dropChance,
            String mapCode, int attackFire, int attackEarth,
            int attackWater, int attackAir, int resFire,
            int resEarth, int resWater, int resAir) {
        this.level = level;
        this.resourceCode = resourceCode;
        this.x = x;
        this.y = y;
        this.dropChance = dropChance;
        this.mapCode = mapCode;
        this.attackFire = attackFire;
        this.attackEarth = attackEarth;
        this.attackWater = attackWater;
        this.attackAir = attackAir;
        this.resFire = resFire;
        this.resEarth = resEarth;
        this.resWater = resWater;
        this.resAir = resAir;
    }

    // Static method to read CSV and create Monster list
    
    // Getters (you can add setters if needed)
    public int getLevel() {
        return level;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDropChance() {
        return dropChance;
    }

    public String getMapCode() {
        return mapCode;
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

    // Optional: toString method for easy printing
    @Override
    public String toString() {
        return "Monster{" +
                "level=" + level +
                ", resourceCode='" + resourceCode + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", dropChance=" + dropChance +
                ", mapCode='" + mapCode + '\'' +
                '}';
    }

}
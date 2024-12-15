package com.elimelvy.artifacts.model.map;

public class Monster extends MapTile {
    // Monster properties based on the CSV columns
    private final int level;
    private final String resourceCode;
    private final int x;
    private final int y;
    private final double dropChance;
    private final String mapCode;
    private final int attackFire;
    private final int attackEarth;
    private final int attackWater;
    private final int attackAir;
    private final int resFire;
    private final int resEarth;
    private final int resWater;
    private final int resAir;
    private final int hp;

    // Constructor
    public Monster(int level, String resourceCode, int x, int y, double dropChance,
            String mapCode, int hp, int attackFire, int attackEarth,
            int attackWater, int attackAir, int resFire,
            int resEarth, int resWater, int resAir) {
        super(x, y, "monster", mapCode);
        this.level = level;
        this.resourceCode = resourceCode;
        this.x = x;
        this.y = y;
        this.dropChance = dropChance;
        this.mapCode = mapCode;
        this.hp = hp;
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

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public double getDropChance() {
        return dropChance;
    }

    public String getMapCode() {
        return mapCode;
    }

    public int getHp() {
        return hp;
    }

    public double getAttackFire() {
        return attackFire;
    }

    public double getAttackEarth() {
        return attackEarth;
    }

    public double getAttackWater() {
        return attackWater;
    }

    public double getAttackAir() {
        return attackAir;
    }

    public double getResFire() {
        return resFire;
    }

    public double getResEarth() {
        return resEarth;
    }

    public double getResWater() {
        return resWater;
    }

    public double getResAir() {
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
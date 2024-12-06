package com.elimelvy.artifacts.model.map;

public class Resource extends MapTile {
    // Resource properties based on the CSV columns
    private final String resourceCode;
    private final int x;
    private final int y;
    private final int dropChance;
    private final String mapCode;
    private final int level;
    private final String skill;

    // Constructor
    public Resource(String resourceCode, int x, int y, int dropChance,
            String mapCode, int level, String skill) {
        super(x, y, "resource", mapCode);
        this.resourceCode = resourceCode;
        this.x = x;
        this.y = y;
        this.dropChance = dropChance;
        this.mapCode = mapCode;
        this.level = level;
        this.skill = skill;
    }

    

    // Getters (you can add setters if needed)
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

    public int getDropChance() {
        return dropChance;
    }

    public String getMapCode() {
        return mapCode;
    }

    public int getLevel() {
        return level;
    }

    public String getSkill() {
        return skill;
    }

    // Optional: toString method for easy printing
    @Override
    public String toString() {
        return "Resource{" +
                "resourceCode='" + resourceCode + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", dropChance=" + dropChance +
                ", mapCode='" + mapCode + '\'' +
                ", level=" + level +
                ", skill='" + skill + '\'' +
                '}';
    }
}
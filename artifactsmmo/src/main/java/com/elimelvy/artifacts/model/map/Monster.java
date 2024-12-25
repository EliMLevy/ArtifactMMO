package com.elimelvy.artifacts.model.map;

public class Monster {
    // Monster properties based on the CSV columns
    private final int level;
    private final String name;
    private final String code;
    private final int hp;
    private final int attackFire;
    private final int attackEarth;
    private final int attackWater;
    private final int attackAir;
    private final int resFire;
    private final int resEarth;
    private final int resWater;
    private final int resAir;

    // Constructor
    public Monster(int level, String code,
            String name, int hp, int attackFire, int attackEarth,
            int attackWater, int attackAir, int resFire,
            int resEarth, int resWater, int resAir) {
        this.level = level;
        this.name = name;
        this.code = code;
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

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
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

    

    @Override
    public String toString() {
        return "Monster [level=" + level + ", name=" + name + ", code=" + code + ", hp=" + hp + ", attackFire="
                + attackFire + ", attackEarth=" + attackEarth + ", attackWater=" + attackWater + ", attackAir="
                + attackAir + ", resFire=" + resFire + ", resEarth=" + resEarth + ", resWater=" + resWater + ", resAir="
                + resAir + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + level;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + hp;
        result = prime * result + attackFire;
        result = prime * result + attackEarth;
        result = prime * result + attackWater;
        result = prime * result + attackAir;
        result = prime * result + resFire;
        result = prime * result + resEarth;
        result = prime * result + resWater;
        result = prime * result + resAir;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Monster other = (Monster) obj;
        if (level != other.level)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        if (hp != other.hp)
            return false;
        if (attackFire != other.attackFire)
            return false;
        if (attackEarth != other.attackEarth)
            return false;
        if (attackWater != other.attackWater)
            return false;
        if (attackAir != other.attackAir)
            return false;
        if (resFire != other.resFire)
            return false;
        if (resEarth != other.resEarth)
            return false;
        if (resWater != other.resWater)
            return false;
        if (resAir != other.resAir)
            return false;
        return true;
    }

    

}
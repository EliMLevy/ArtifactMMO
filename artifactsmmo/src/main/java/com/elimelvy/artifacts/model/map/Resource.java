package com.elimelvy.artifacts.model.map;

public class Resource {
    // Resource properties based on the CSV columns
    private final String code;
    private final String name;
    private final int level;
    private final String skill;

    // Constructor
    public Resource(String code, String name, int level, String skill) {
        this.code = code;
        this.name = name;
        this.level = level;
        this.skill = skill;
    }

    

    // Getters (you can add setters if needed)
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
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
                "code='" + code + '\'' +
                ", level=" + level +
                ", skill='" + skill + '\'' +
                '}';
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + level;
        result = prime * result + ((skill == null) ? 0 : skill.hashCode());
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
        Resource other = (Resource) obj;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (level != other.level)
            return false;
        if (skill == null) {
            if (other.skill != null)
                return false;
        } else if (!skill.equals(other.skill))
            return false;
        return true;
    }

    
}
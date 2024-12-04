package com.elimelvy.artifacts.model.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapManager {

    private final List<Monster> monsters;
    private final List<Resource> resources;
    private static MapManager instance;

    private MapManager() {
        this.monsters = readMonstersFromCSV("./src/main/resources/monsters.csv");
        this.resources = readResourcesFromCSV("./src/main/resources/resources.csv");
    }

    public static MapManager getInstance() {
        if (instance == null) {
            instance = new MapManager();
        }
        return instance;
    }

    // Static method to read CSV and create Resource list
    public static List<Resource> readResourcesFromCSV(String filePath) {
        List<Resource> resources = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the header line
            String line;
            br.readLine();

            // Read data lines
            while ((line = br.readLine()) != null) {
                // Split the line by comma
                String[] values = line.split(",");

                // Parse each value
                Resource resource = new Resource(
                        values[0], // resource_code
                        Integer.parseInt(values[1]), // x
                        Integer.parseInt(values[2]), // y
                        Integer.parseInt(values[3]), // drop_chance
                        values[4], // map_code
                        Integer.parseInt(values[5]), // level
                        values[6] // skill
                );

                resources.add(resource);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return resources;
    }

    public static List<Monster> readMonstersFromCSV(String filePath) {
        List<Monster> monsters = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the header line
            String line;
            br.readLine();

            // Read data lines
            while ((line = br.readLine()) != null) {
                // Split the line by comma
                String[] values = line.split(",");

                // Parse each value
                Monster monster = new Monster(
                        Integer.parseInt(values[0]), // level
                        values[1], // resource_code
                        Integer.parseInt(values[2]), // x
                        Integer.parseInt(values[3]), // y
                        Integer.parseInt(values[4]), // drop_chance
                        values[5], // map_code
                        Integer.parseInt(values[6]), // attack_fire
                        Integer.parseInt(values[7]), // attack_earth
                        Integer.parseInt(values[8]), // attack_water
                        Integer.parseInt(values[9]), // attack_air
                        Integer.parseInt(values[10]), // res_fire
                        Integer.parseInt(values[11]), // res_earth
                        Integer.parseInt(values[12]), // res_water
                        Integer.parseInt(values[13]) // res_air
                );

                monsters.add(monster);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

        return monsters;
    }

    public List<Resource> getResouce(String resourceCode) {
        return this.resources.stream().filter(elem -> elem.getResourceCode().equals(resourceCode)).toList();
    }

    public List<Monster> getMonster(String resourceCode) {
        return this.monsters.stream().filter(elem -> elem.getResourceCode().equals(resourceCode)).toList();
    }
}

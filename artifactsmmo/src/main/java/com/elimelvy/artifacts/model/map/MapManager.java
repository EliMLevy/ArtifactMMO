package com.elimelvy.artifacts.model.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapManager {

    private final Map<String, List<MapTile>> index;
    private final List<Monster> monsters;
    private final List<Resource> resources;
    private static MapManager instance;

    private MapManager() {
        this.monsters = new ArrayList<>();
        this.resources = new ArrayList<>();
        this.index = new HashMap<>();
        readMonstersFromCSV("./src/main/resources/monsters.csv");
        readResourcesFromCSV("./src/main/resources/resources.csv");
        readAllMapsFromCSV("./src/main/resources/all_maps.csv");
    }

    public static MapManager getInstance() {
        if (instance == null) {
            instance = new MapManager();
        }
        return instance;
    }

    private void readResourcesFromCSV(String filePath) {

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
    }

    private void readMonstersFromCSV(String filePath) {

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

    }


    private void readAllMapsFromCSV(String filePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the header line
            String line;
            br.readLine();

            // Read data lines
            while ((line = br.readLine()) != null) {
                // Split the line by comma
                String[] values = line.split(",");

                // Parse each value
                MapTile mapTile = new MapTile(
                        Integer.parseInt(values[0]), // x
                        Integer.parseInt(values[1]), // y
                        values[2], // content_type
                        values[3] // content_code
                );

                this.index.putIfAbsent(mapTile.getContentCode(), new ArrayList<>());
                this.index.get(mapTile.getContentCode()).add(mapTile);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }

    }

    public List<MapTile> getMap(String mapCode) {
        return this.index.get(mapCode);
    }

    public boolean isMonsterDrop(String resourceCode) {
        return !this.getMonster(resourceCode).isEmpty();
    }

    public List<Resource> getResouce(String resourceCode) {
        return this.resources.stream().filter(elem -> elem.getResourceCode().equals(resourceCode)).toList();
    }

    public List<Monster> getMonster(String resourceCode) {
        return this.monsters.stream().filter(elem -> elem.getResourceCode().equals(resourceCode)).toList();
    }

    public List<Monster> getByMonsterCode(String monsterCode) {
        return this.monsters.stream().filter(elem -> elem.getContentCode().equals(monsterCode)).toList();
    }
}

package com.elimelvy.artifacts.model.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MapManager {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<MapTile>> index;
    private final ConcurrentHashMap<String, Monster> monsters;
    private final ConcurrentHashMap<String, Resource> resources;
    private final ConcurrentHashMap<String, List<Drop>> drops;
    private static volatile MapManager instance; // volatile for double-checked locking
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private MapManager() {
        this.monsters = new ConcurrentHashMap<>();
        this.resources = new ConcurrentHashMap<>();
        this.drops = new ConcurrentHashMap<>();
        this.index = new ConcurrentHashMap<>();
        reloadFromDisk();
    }

    // Thread-safe singleton with double-checked locking
    public static MapManager getInstance() {
        if (instance == null) {
            synchronized (MapManager.class) {
                if (instance == null) {
                    instance = new MapManager();
                }
            }
        }
        return instance;
    }

    // Synchronized reloadFromDisk method
    public final void reloadFromDisk() {
        lock.writeLock().lock(); // Acquire write lock
        try {
            readMonstersFromCSV("./src/main/resources/monsters.csv");
            readResourcesFromCSV("./src/main/resources/resources.csv");
            readAllMapsFromCSV("./src/main/resources/all_maps.csv");
            readDropsFromCSV("./src/main/resources/drops.csv");
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    private void readResourcesFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            Map<String, Resource> tempResources = new HashMap<>();
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Resource resource = new Resource(values[1], values[2], Integer.parseInt(values[0]), values[3]);
                tempResources.put(resource.getCode(), resource);
            }
            resources.clear();
            resources.putAll(tempResources);
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private void readMonstersFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            Map<String, Monster> tempMonsters = new HashMap<>();
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Monster monster = new Monster(
                        Integer.parseInt(values[0]), values[1], values[2], Integer.parseInt(values[3]),
                        Integer.parseInt(values[4]), Integer.parseInt(values[5]), Integer.parseInt(values[6]),
                        Integer.parseInt(values[7]), Integer.parseInt(values[8]), Integer.parseInt(values[9]),
                        Integer.parseInt(values[10]), Integer.parseInt(values[11]));
                tempMonsters.put(monster.getCode(), monster);
            }
            monsters.clear();
            monsters.putAll(tempMonsters);
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private void readDropsFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            Map<String, List<Drop>> tempDrops = new HashMap<>();
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Drop drop = new Drop(
                        values[0], values[1], Integer.parseInt(values[2]),
                        Integer.parseInt(values[3]), Integer.parseInt(values[4]));
                tempDrops.putIfAbsent(drop.getDropCode(), new ArrayList<>());
                tempDrops.get(drop.getDropCode()).add(drop);
            }
            drops.clear();
            drops.putAll(tempDrops);
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private void readAllMapsFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            ConcurrentHashMap<String, CopyOnWriteArrayList<MapTile>> tempIndex = new ConcurrentHashMap<>();
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                MapTile mapTile = new MapTile(
                        Integer.parseInt(values[0]), Integer.parseInt(values[1]),
                        values[2], values[3]);
                tempIndex.computeIfAbsent(mapTile.getContentCode(), k -> new CopyOnWriteArrayList<>()).add(mapTile);
            }
            index.clear();
            index.putAll(tempIndex);
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    // Read operations with read lock for consistency
    public List<MapTile> getMap(String mapCode) {
        return index.getOrDefault(mapCode, new CopyOnWriteArrayList<>());
    }

    public boolean isMonsterDrop(String resourceCode) {
        return getMonster(drops.get(resourceCode).get(0).getContentCode()) == null;
    }

    public Resource getResouce(String resourceCode) {
        return resources.get(resourceCode);
    }

    public Monster getMonster(String monsterCode) {
        return monsters.get(monsterCode);
    }

    /**
     * Check the drop table for a list of places where this resource is dropped.
     * Filter out spots that arent avaiable. Choose the spot with the best rate.
     * The return value is a list because a given map may have several locations.
     * 
     * @param resourceCode The code of the resource or mosnter drop
     * @return the map tiles where this resource could be collected
     */
    public List<MapTile> getMapByResource(String resourceCode) {
        if(drops.containsKey(resourceCode)) {
            List<Drop> dropCandidates = this.drops.get(resourceCode);
            List<MapTile> result = null;
            int bestRate = Integer.MAX_VALUE;
            for(Drop d : dropCandidates) {
                if(this.index.containsKey(d.getContentCode())) {
                    if(d.getRate() < bestRate) {
                        result = this.index.get(d.getContentCode());
                    }
                }
            }
            return result;
            
        } else {
            return null;
        }
    }

    public Monster getMonsterByDrop(String resourceCode) {
        if (drops.containsKey(resourceCode)) {
            List<Drop> dropCandidates = this.drops.get(resourceCode);
            Monster result = null;
            int bestRate = Integer.MAX_VALUE;
            for (Drop d : dropCandidates) {
                if (this.monsters.containsKey(d.getContentCode())) {
                    if (d.getRate() < bestRate) {
                        result = this.monsters.get(d.getContentCode());
                    }
                }
            }
            return result;

        } else {
            return null;
        }
    }

    public Resource getResourceByDrop(String resourceCode) {
        if (drops.containsKey(resourceCode)) {
            List<Drop> dropCandidates = this.drops.get(resourceCode);
            Resource result = null;
            int bestRate = Integer.MAX_VALUE;
            for (Drop d : dropCandidates) {
                if (this.resources.containsKey(d.getContentCode())) {
                    if (d.getRate() < bestRate) {
                        result = this.resources.get(d.getContentCode());
                    }
                }
            }
            return result;

        } else {
            return null;
        }
    }

    public List<Resource> getMapsBySkill(String skill) {
        lock.readLock().lock();
        try {
            return resources.values().stream()
                    .filter(elem -> elem.getSkill().equals(skill))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Monster> getMonstersByLevel(int min, int max) {
        lock.readLock().lock();
        try {
            return monsters.values().stream()
                    .filter(elem -> elem.getLevel() >= min && elem.getLevel() <= max)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }
}

package com.elimelvy.artifacts.model.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MapManager {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<MapTile>> index;
    private final CopyOnWriteArrayList<Monster> monsters;
    private final CopyOnWriteArrayList<Resource> resources;
    private static volatile MapManager instance; // volatile for double-checked locking
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private MapManager() {
        this.monsters = new CopyOnWriteArrayList<>();
        this.resources = new CopyOnWriteArrayList<>();
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
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    private void readResourcesFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            List<Resource> tempResources = new ArrayList<>();
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Resource resource = new Resource(
                        values[0], Integer.parseInt(values[1]), Integer.parseInt(values[2]),
                        Double.parseDouble(values[3]), values[4], Integer.parseInt(values[5]), values[6]);
                tempResources.add(resource);
            }
            resources.clear();
            resources.addAll(tempResources);
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private void readMonstersFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            List<Monster> tempMonsters = new ArrayList<>();
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Monster monster = new Monster(
                        Integer.parseInt(values[0]), values[1], Integer.parseInt(values[2]),
                        Integer.parseInt(values[3]),
                        Double.parseDouble(values[4]), values[5], Integer.parseInt(values[6]),
                        Integer.parseInt(values[7]), Integer.parseInt(values[8]), Integer.parseInt(values[9]),
                        Integer.parseInt(values[10]), Integer.parseInt(values[11]), Integer.parseInt(values[12]),
                        Integer.parseInt(values[13]), Integer.parseInt(values[14]));
                tempMonsters.add(monster);
            }
            monsters.clear();
            monsters.addAll(tempMonsters);
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
        lock.readLock().lock();
        try {
            return index.getOrDefault(mapCode, new CopyOnWriteArrayList<>());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isMonsterDrop(String resourceCode) {
        return !getMonster(resourceCode).isEmpty();
    }

    public List<Resource> getResouce(String resourceCode) {
        lock.readLock().lock();
        try {
            return resources.stream()
                    .filter(elem -> elem.getResourceCode().equals(resourceCode))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Monster> getMonster(String resourceCode) {
        lock.readLock().lock();
        try {
            return monsters.stream()
                    .filter(elem -> elem.getResourceCode().equals(resourceCode))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Monster> getByMonsterCode(String monsterCode) {
        lock.readLock().lock();
        try {
            return monsters.stream()
                    .filter(elem -> elem.getContentCode().equals(monsterCode))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Resource> getMapsBySkill(String skill) {
        lock.readLock().lock();
        try {
            return resources.stream()
                    .filter(elem -> elem.getSkill().equals(skill))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Monster> getMonstersByLevel(int min, int max) {
        lock.readLock().lock();
        try {
            return monsters.stream()
                    .filter(elem -> elem.getLevel() >= min && elem.getLevel() <= max)
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }
}

package com.elimelvy.artifacts.model.item;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameItemManager {
    private final Map<String, GameItem> items;
    private static GameItemManager instance;

    private final Logger logger = LoggerFactory.getLogger(GameItemManager.class);

    private GameItemManager() {
        this.items = loadItems();
    }

    public static synchronized GameItemManager getInstance() {
        if (instance == null) {
            instance = new GameItemManager();
        }
        return instance;
    }

    private Map<String, GameItem> loadItems() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("all_items.json");
            if (inputStream == null) {
                throw new RuntimeException("Could not find all_items.json");
            }

            Reader reader = new InputStreamReader(inputStream);
            Gson gson = new Gson();
            TypeToken<Map<String, GameItem>> typeToken = new TypeToken<Map<String, GameItem>>() {
            };
            Map<String, GameItem> loadedItems = gson.fromJson(reader, typeToken.getType());
            return Collections.unmodifiableMap(new HashMap<>(loadedItems));
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to load items", e);
        }
    }

    /**
     * Get a GameItem by its name
     * 
     * @param name The name of the item to retrieve
     * @return The GameItem if found, null otherwise
     */
    public GameItem getItem(String name) {
        return items.get(name);
    }

    /**
     * Get all items that match the given condition
     * 
     * @param condition The condition to filter items by
     * @return A list of GameItems that match the condition
     */
    public List<GameItem> getItems(Predicate<GameItem> condition) {
        List<GameItem> result = items.values().stream()
                .filter(condition)
                .collect(Collectors.toList());
        return result;
    }

    /**
     * Get all items
     * 
     * @return An unmodifiable collection of all GameItems
     */
    public Collection<GameItem> getAllItems() {
        return Collections.unmodifiableCollection(items.values());
    }
}

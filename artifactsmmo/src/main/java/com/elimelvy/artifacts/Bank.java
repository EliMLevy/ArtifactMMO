package com.elimelvy.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.model.InventoryItem;
import com.elimelvy.artifacts.util.HTTPRequester;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Bank {
    // Singleton instance
    private static Bank instance;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(Bank.class);
    private static final Gson gson = new Gson();

    // Synchronization lock
    private final Lock lock = new ReentrantLock();

    // Caching variables
    private volatile List<InventoryItem> bankItems;
    private volatile AtomicLong lastUpdated = new AtomicLong();

    // Private constructor to prevent instantiation
    private Bank() {
    }

    // Singleton getter
    public static synchronized Bank getInstance() {
        if (instance == null) {
            instance = new Bank();
        }
        return instance;
    }

    /**
     * Retrieves bank items, using cache if possible
     * 
     * @return List of bank items
     */
    public List<InventoryItem> getBankItems() {
        return this.bankItems;
    }

    public List<InventoryItem> refreshBankItems() {
        lock.lock();
        try {
            // Check if cache needs refresh
            logger.info("Refreshing bank items cache.");

            // Fetch bank items
            int page = 1;
            List<InventoryItem> allResults = new ArrayList<>();
            boolean done = false;

            while (!done) {
                // Use the utility method to send request
                String url = "https://api.artifactsmmo.com/my/bank/items?page=" + page;
                JsonObject result = HTTPRequester.sendRequestToUrl(url, "", "GET", null);

                if (result != null && result.has("data")) {
                    JsonArray dataArray = result.getAsJsonArray("data");
                    // Convert JsonArray to List<JsonObject>
                    for (int i = 0; i < dataArray.size(); i++) {
                        allResults.add(gson.fromJson(dataArray.get(i), InventoryItem.class));
                    }

                    logger.debug("Page results: {}", dataArray.size());

                    // Check if more pages exist
                    if (result.has("pages") && result.get("pages").getAsInt() > page) {
                        page++;
                        // Small delay between pages
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            logger.warn("Page fetch interrupted");
                        }
                    } else {
                        done = true;
                    }
                } else {
                    logger.warn("Failed to retrieve bank items");
                    break;
                }
            }
            // Update cache
            this.bankItems = allResults;
            return allResults;
        } finally {
            lock.unlock();
        }
    }

    public synchronized void updateBankContents(JsonElement data, long dataTimestamp) {
        lock.lock();
        try {
            if(this.lastUpdated.get() < dataTimestamp) {
                if (data != null && data.isJsonArray()) {
                    logger.debug("Updating bank contents!");
                    List<InventoryItem> allResults = new ArrayList<>();
                    JsonArray dataArray = data.getAsJsonArray();
                    // Convert JsonArray to List<JsonObject>
                    for (int i = 0; i < dataArray.size(); i++) {
                        allResults.add(gson.fromJson(dataArray.get(i), InventoryItem.class));
                    }
                    this.bankItems = allResults;
                } else {
                    logger.warn("Tried passing a non-json array to updateBankContents: {}", "");
                }
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the quantity of a specific item in the bank
     * 
     * @param itemCode Item code to search for
     * @return Quantity of the item (0 if not found)
     */
    public int getBankQuantity(String itemCode) {
        lock.lock();
        try {
            if(this.bankItems == null) {
                this.refreshBankItems();
            }
        } finally {
            lock.unlock();
        }

        for (InventoryItem item : this.bankItems) {
            if (item.getCode() != null && item.getCode().equals(itemCode)) {
                int quantity = item.getQuantity();
                // logger.debug("Item {} quantity: {}", itemCode, quantity );
                return quantity;
            }
        }

        // logger.debug("Item {} not found in bank", itemCode);
        return 0;
    }
}
package com.elimelvy.artifacts;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.elimelvy.artifacts.util.HTTPRequester;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Bank {
    // Singleton instance
    private static Bank instance;

    // Logger
    private static final Logger logger = Logger.getLogger(Bank.class.getName());

    // Synchronization lock
    private final Lock lock = new ReentrantLock();

    // Caching variables
    private List<JsonObject> cachedBankItems;
    private Instant lastFetchedBankItems;
    private static final int BANK_ITEM_FETCH_REFRESH = 30; // seconds

    // Private constructor to prevent instantiation
    private Bank() {
        cachedBankItems = null;
        lastFetchedBankItems = null;
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
    public List<JsonObject> getBankItems() {
        lock.lock();
        try {
            // Check if cache needs refresh
            if (cachedBankItems == null ||
                    lastFetchedBankItems == null ||
                    Duration.between(lastFetchedBankItems, Instant.now()).getSeconds() > BANK_ITEM_FETCH_REFRESH) {

                logger.log(Level.INFO, "Refreshing bank items cache. Last fetched: {0}", lastFetchedBankItems);

                // Fetch bank items
                int page = 1;
                List<JsonObject> allResults = new ArrayList<>();
                boolean done = false;

                while (!done) {
                    // Use the utility method to send request
                    String url = "https://api.artifactsmmo.com/my/bank/items?page=" + page;
                    JsonObject result = HTTPRequester.sendRequestToUrl(url, "", "GET", null);

                    if (result != null && result.has("data")) {
                        JsonArray dataArray = result.getAsJsonArray("data");

                        // Convert JsonArray to List<JsonObject>
                        for (int i = 0; i < dataArray.size(); i++) {
                            allResults.add(dataArray.get(i).getAsJsonObject());
                        }

                        logger.log(Level.FINE, "Page results: {0}", dataArray.size());

                        // Check if more pages exist
                        if (result.has("pages") && result.get("pages").getAsInt() > page) {
                            page++;
                        } else {
                            done = true;
                        }

                        // Small delay between pages
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            logger.warning("Page fetch interrupted");
                        }
                    } else {
                        logger.warning("Failed to retrieve bank items");
                        break;
                    }
                }

                // Update cache
                cachedBankItems = allResults;
                lastFetchedBankItems = Instant.now();
                return allResults;
            }

            // Return cached items
            logger.info("Returning cached bank items");
            return cachedBankItems;
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
        List<JsonObject> bankItems = getBankItems();

        for (JsonObject item : bankItems) {
            if (item.has("code") && item.get("code").getAsString().equals(itemCode)) {
                int quantity = item.has("quantity") ? item.get("quantity").getAsInt() : 0;
                logger.log(Level.FINE, "Item {0} quantity: {1}", new Object[]{itemCode, quantity});
                return quantity;
            }
        }

        logger.log(Level.FINE, "Item {0} not found in bank", itemCode);
        return 0;
    }
}
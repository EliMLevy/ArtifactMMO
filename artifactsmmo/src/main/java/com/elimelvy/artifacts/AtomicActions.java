package com.elimelvy.artifacts;

import com.elimelvy.artifacts.util.HTTPRequester;
import com.elimelvy.artifacts.util.StructuredLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AtomicActions {
    /**
     * Move character to specified coordinates
     * 
     * @param character Character name
     * @param x         X coordinate
     * @param y         Y coordinate
     * @return API response
     */
    public static JsonObject move(String character, int x, int y) {
        JsonObject raw = new JsonObject();
        raw.addProperty("x", x);
        raw.addProperty("y", y);
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/move", "POST", raw);
        logEvent(raw.toString(), result, "MOVE", character, "destination");
        return result;
    }

    /**
     * Initiate a fight action
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject attack(String character, String monsterCode) {
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/fight", "POST", null);
        reformatAttackResponseBody(result);
        logEvent(monsterCode, result, "ATTACK", character, "fight");
        return result;
    }

    private static JsonObject reformatAttackResponseBody(JsonObject result) {
        if(result != null) {
            if(result.has("data") && result.get("data").isJsonObject()) {
                JsonObject data = result.getAsJsonObject("data");
                if(data.has("fight") && data.get("fight").isJsonObject()) {
                    JsonObject fight = data.getAsJsonObject("fight");
                    if(fight.has("monster_blocked_hits")) {
                        fight.remove("monster_blocked_hits");
                    }
                    if(fight.has("player_blocked_hits")) {
                        fight.remove("player_blocked_hits");
                    }
                    if(fight.has("logs") && fight.get("logs").isJsonArray()) {
                        JsonArray logs = fight.getAsJsonArray("logs");
                        // Keep the first and last log message
                        JsonArray abridged = new JsonArray(2);
                        abridged.add(logs.get(0));
                        abridged.add(logs.get(logs.size() - 1));
                        fight.add("logs", abridged);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Rest action for the character
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject rest(String character) {
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/rest", "POST", null);
        logEvent("", result, "REST", character, "hp_restored");

        return result;
    }

    /**
     * Gathering action for the character
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject collect(String character, String resourceCode) {
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/gathering", "POST", null);
        logEvent(resourceCode, result, "COLLECT", character, "details");
        return result;
    }

    /**
     * Craft an item
     * 
     * @param character Character name
     * @param itemCode  Item code to craft
     * @param quantity  Quantity to craft
     * @return API response
     */
    public static JsonObject craft(String character, String itemCode, int quantity) {
        JsonObject raw = new JsonObject();
        raw.addProperty("code", itemCode);
        raw.addProperty("quantity", quantity);
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/crafting", "POST", raw);
        logEvent(raw.toString(), result, "CRAFT", character, "details");
        return result;
    }

    /**
     * Recycle an item
     * 
     * @param character Character name
     * @param code      Item code to recycle
     * @param quantity  Quantity to recycle
     * @return API response
     */
    public static JsonObject recycle(String character, String code, int quantity) {
        JsonObject raw = new JsonObject();
        raw.addProperty("code", code);
        raw.addProperty("quantity", quantity);
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/recycling", "POST", raw);
        logEvent(raw.toString(), result, "RECYCLE", character, "details");
        return result;
    }

    /**
     * Unequip an item from a specific slot
     * 
     * @param character Character name
     * @param slot      Slot to unequip from
     * @return API response
     */
    public static JsonObject unequip(String character, String slot, int quantity) {
        JsonObject raw = new JsonObject();
        raw.addProperty("slot", slot);
        raw.addProperty("quantity", quantity);
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/unequip", "POST", raw);
        logEvent(raw.toString(), result, "UNEQUIP", character, null);
        return result;
    }

    /**
     * Equip an item to a specific slot
     * 
     * @param character Character name
     * @param code      Item code to equip
     * @param slot      Slot to equip to
     * @param quantity  Quantity to equip (default 1)
     * @return API response
     */
    public static JsonObject equip(String character, String code, String slot, int quantity) {
        JsonObject raw = new JsonObject();
        raw.addProperty("code", code);
        raw.addProperty("slot", slot);
        raw.addProperty("quantity", quantity);
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/equip", "POST", raw);
        logEvent(raw.toString(), result, "EQUIP", character, null);
        return result;
    }

    /**
     * Overloaded equip method with default quantity of 1
     */
    public static JsonObject equip(String character, String code, String slot) {
        return equip(character, code, slot, 1);
    }

    /**
     * Get character information
     * 
     * @param name Character name
     * @return API response
     */
    public static JsonObject getCharacter(String name) {
        return HTTPRequester.sendRequestToUrl("https://api.artifactsmmo.com/characters/" + name, "", "GET", null);
    }

    public static JsonObject getAllCharacters() {
        return HTTPRequester.sendRequestToUrl("https://api.artifactsmmo.com/my/characters", "", "GET", null);
    }

    /**
     * Deposit an item to bank
     * 
     * @param character Character name
     * @param code      Item code to deposit
     * @param quantity  Quantity to deposit
     * @return API response
     */
    public static JsonObject depositItem(String character, String code, int quantity) {
        JsonObject body = new JsonObject();
        body.addProperty("code", code);
        body.addProperty("quantity", quantity);
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/bank/deposit", "POST", body);
        logEvent(body.toString(), result, "DEPOSIT", character, null);
        return result;
    }

    /**
     * Withdraw an item from bank
     * update out local copy
     * 
     * 
     * @param character Character name
     * @param code      Item code to withdraw
     * @param quantity  Quantity to withdraw
     * @return API response
     */
    public static JsonObject withdrawItem(String character, String code, int quantity) {
        JsonObject body = new JsonObject();
        body.addProperty("code", code);
        body.addProperty("quantity", quantity);
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/bank/withdraw", "POST", body);
        logEvent(body.toString(), result, "WITHDRAW", character, null);
        return result;
    }

    /**
     * Use an item
     * 
     * @param character Character name
     * @param code      Item code to use
     * @param quantity  Quantity to use
     * @return API response
     */
    public static JsonObject useItem(String character, String code, int quantity) {
        JsonObject body = new JsonObject();
        body.addProperty("code", code);
        body.addProperty("quantity", quantity);
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/use", "POST", body);
        logEvent(body.toString(), result, "USE_ITEM", character, null);
        return result;
    }

    /**
     * Accept a new task
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject acceptNewTask(String character) {
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/task/new", "POST", null);
        logEvent("", result, "NEW_TASK", character, "task");
        return result;
    }

    /**
     * Complete current task
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject completeTask(String character) {
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/task/complete", "POST", null);
        logEvent("", result, "END_TASK", character, "rewards");
        return result;
    }

    /**
     * Trade with task master
     * 
     * @param character Character name
     * @param code      Item code to trade
     * @param quantity  Quantity to trade
     * @return API response
     */
    public static JsonObject tradeWithTaskMaster(String character, String code, int quantity) {
        JsonObject body = new JsonObject();
        body.addProperty("code", code);
        body.addProperty("quantity", quantity);
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/task/trade", "POST", body);
        logEvent(body.toString(), result, "TRADE_TASK_ITEMS", character, "trade");
        return result;
    }

    public static JsonObject exchangeCoinsWithTaskMaster(String character) {
        JsonObject result = HTTPRequester.sendCharacterRequest(character, "/action/task/exchange", "POST", null);
        logEvent("", result, "EXCHANGE_TASK_COINS", character, "rewards");
        return result;
    }

    private static void logEvent(String requestBody, JsonObject result, String eventName, String character,
            String responseBodyKey) {
        if (result != null) {
            if (result.has("error")) {
                StructuredLogger.getInstance().logEvent(eventName, character, requestBody, result.toString(), 0);
            } else {
                JsonObject data = result.getAsJsonObject("data");
                StructuredLogger.getInstance().logEvent(eventName, character,
                        requestBody, responseBodyKey != null ? data.get(responseBodyKey).toString() : "",
                        data.getAsJsonObject("cooldown").get("total_seconds").getAsLong());
            }
        }
    }
}

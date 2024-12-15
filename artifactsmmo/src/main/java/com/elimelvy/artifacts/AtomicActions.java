package com.elimelvy.artifacts;


import com.elimelvy.artifacts.util.HTTPRequester;
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
        return HTTPRequester.sendCharacterRequest(character, "/action/move", "POST", raw);
    }

    /**
     * Initiate a fight action
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject attack(String character) {
        return HTTPRequester.sendCharacterRequest(character, "/action/fight", "POST", null);
    }

    /**
     * Rest action for the character
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject rest(String character) {
        return HTTPRequester.sendCharacterRequest(character, "/action/rest", "POST", null);
    }

    /**
     * Gathering action for the character
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject collect(String character) {
        return HTTPRequester.sendCharacterRequest(character, "/action/gathering", "POST", null);
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
        return HTTPRequester.sendCharacterRequest(character, "/action/crafting", "POST", raw);
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
        return HTTPRequester.sendCharacterRequest(character, "/action/recycling", "POST", raw);
    }

    /**
     * Unequip an item from a specific slot
     * 
     * @param character Character name
     * @param slot      Slot to unequip from
     * @return API response
     */
    public static JsonObject unequip(String character, String slot) {
        JsonObject raw = new JsonObject();
        raw.addProperty("slot", slot);
        return HTTPRequester.sendCharacterRequest(character, "/action/unequip", "POST", raw);
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
        return HTTPRequester.sendCharacterRequest(character, "/action/equip", "POST", raw);
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
        return HTTPRequester.sendCharacterRequest(character, "/action/bank/deposit", "POST", body);
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
        return HTTPRequester.sendCharacterRequest(character, "/action/bank/withdraw", "POST", body);
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
        return HTTPRequester.sendCharacterRequest(character, "/action/use", "POST", body);
    }

    /**
     * Accept a new task
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject acceptNewTask(String character) {
        return HTTPRequester.sendCharacterRequest(character, "/action/task/new", "POST", null);
    }

    /**
     * Complete current task
     * 
     * @param character Character name
     * @return API response
     */
    public static JsonObject completeTask(String character) {
        return HTTPRequester.sendCharacterRequest(character, "/action/task/complete", "POST", null);
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
        return HTTPRequester.sendCharacterRequest(character, "/action/task/trade", "POST", body);
    }

    public static JsonObject exchangeCoinsWithTaskMaster(String character) {
        return HTTPRequester.sendCharacterRequest(character, "/action/task/exchange", "POST", null);
    }
}

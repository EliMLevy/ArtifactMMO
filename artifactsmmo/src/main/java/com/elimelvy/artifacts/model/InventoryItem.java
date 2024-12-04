package com.elimelvy.artifacts.model;

public class InventoryItem {
    private int slot;
    private String code;
    private int quantity;

    public int getSlot() {
        return slot;
    }

    public String getCode() {
        return code;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "slot=" + slot +
                ", code='" + code + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
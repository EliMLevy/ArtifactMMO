package com.elimelvy.artifacts.model;

public class InventoryItem {
    private int slot;
    private String code;
    private int quantity;

    public InventoryItem(int slot, String code, int quantity) {
        this.slot = slot;
        this.code = code;
        this.quantity = quantity;
    }

    public int getSlot() {
        return slot;
    }

    public String getCode() {
        return code;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
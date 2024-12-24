package com.elimelvy.artifacts.model.map;

public class Drop {

    private final String contentCode;
    private final String dropCode;
    private final int minQuantity;
    private final int maxQuantity;
    private final int rate;

    public Drop(String contentCode,
            String dropCode,
            int minQuantity,
            int maxQuantity,
            int rate) {
        this.contentCode = contentCode;
        this.dropCode = dropCode;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.rate = rate;
    }

    public String getContentCode() {
        return contentCode;
    }

    public String getDropCode() {
        return dropCode;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public int getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return "Drop [contentCode=" + contentCode + ", dropCode=" + dropCode + ", minQuantity=" + minQuantity
                + ", maxQuantity=" + maxQuantity + ", rate=" + rate + "]";
    }


    

}

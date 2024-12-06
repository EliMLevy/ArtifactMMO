package com.elimelvy.artifacts.model.map;

public class MapTile {

    private final int x;
    private final String contentType;
    private final String contentCode;
    private final int y;

    public MapTile(
            int x,
            int y,
            String contentType,
            String contentCode) {
        this.x = x;
        this.y = y;
        this.contentType = contentType;
        this.contentCode = contentCode;
    }

    public int getX() {
        return x;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentCode() {
        return contentCode;
    }

    public int getY() {
        return y;
    }


    

}

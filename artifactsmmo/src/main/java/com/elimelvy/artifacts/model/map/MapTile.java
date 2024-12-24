package com.elimelvy.artifacts.model.map;

public class MapTile {

    private final int x;
    private final int y;
    private final String contentType;
    private final String contentCode;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
        result = prime * result + ((contentCode == null) ? 0 : contentCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MapTile other = (MapTile) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (contentType == null) {
            if (other.contentType != null)
                return false;
        } else if (!contentType.equals(other.contentType))
            return false;
        if (contentCode == null) {
            if (other.contentCode != null)
                return false;
        } else if (!contentCode.equals(other.contentCode))
            return false;
        return true;
    }


    
    

}

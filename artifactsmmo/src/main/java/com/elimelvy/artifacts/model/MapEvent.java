package com.elimelvy.artifacts.model;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

public class MapEvent {
    public class EventLocation {
        public int x, y;
    }

    public String name;
    public String code;
    public EventLocation map;
    public int duration;
    public Instant expiration;
    @SerializedName("created_at")
    public Instant createdAt;
}

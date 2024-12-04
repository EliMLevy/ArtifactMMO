package com.elimelvy.artifacts.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

public class InstantTypeAdapter extends TypeAdapter<Instant> {
    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            // Write Instant as an ISO-8601 string
            out.value(value.toString());
        }
    }

    @Override
    public Instant read(JsonReader in) throws IOException {
        switch (in.peek()) {
            case NULL -> {
                in.nextNull();
                return null;
            }
            case STRING -> {
                // Parse the ISO-8601 string back to Instant
                return Instant.parse(in.nextString());
            }
            default -> throw new IllegalArgumentException("Unexpected type for Instant: " + in.peek());
        }
    }

    // Utility method to create a Gson instance with Instant support
    public static Gson createGsonWithInstant() {
        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .create();
    }
}
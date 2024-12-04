package com.elimelvy.artifacts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

public class AtomicActionsTest {

    @Test
    public void testGetCharacters() {
        JsonObject response = AtomicActions.getAllCharacters();
        assertTrue(response.has("data"));
        assertTrue(response.get("data").isJsonArray());
        assertEquals(5, response.get("data").getAsJsonArray().size());
    }
}

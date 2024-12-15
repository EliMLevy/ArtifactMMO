package com.elimelvy.artifacts;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.model.PlanStep;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/*
 * Every hour lets reload the current events
 * We pass in the CharacterManager so that when there is an 
 * exclusive event that we want to take advantage of we an alert the character manager and it will assign characters to it
 * 
 */
public class EventManager implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(EventManager.class);
    private final Map<String, PlanStep> interestingEvents;
    private final CharacterManager mgr;
    private static final Gson gson = new Gson();

    public EventManager(Map<String, PlanStep> interestingEvents, CharacterManager mgr) {
        this.interestingEvents = interestingEvents;
        this.mgr = mgr;
    }

    @Override
    public void run() {
        // Load all the events
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL("https://api.artifactsmmo.com/events/active?size=100")
                    .openConnection();
            connection.setRequestProperty("Accept", "application/json");
            InputStream responseStream = connection.getInputStream();
            String response = new String(responseStream.readAllBytes());
            JsonObject result = gson.fromJson(response, JsonObject.class);

            // Check if any are interesting
            if (result.has("data") && result.get("data").isJsonArray()) {
                for (JsonElement event : result.getAsJsonArray("data")) {
                    if (event.isJsonObject()) {
                        JsonObject eventObj = event.getAsJsonObject();
                        if (eventObj.has("code") && eventObj.get("code").isJsonPrimitive()) {
                            if (interestingEvents.containsKey(eventObj.get("code").getAsString())) {
                                // Alert the character manager if they are
                                logger.info("Event is interesting! Assigning characters to it! {}", eventObj.get("code").getAsString());
                                mgr.assignAllToTask(interestingEvents.get(eventObj.get("code").getAsString()));
                                // Assign characters to the first interesting event we see. 
                                // TODO in the future we can delegate some to each interesting event
                                break;
                            }
                        } else {
                            logger.warn("Event did not have a code or it was not primitive. {}", eventObj);
                        }
                    } else {
                        logger.warn("Event was not a json object. {}", event);
                    }
                }
            } else {
                logger.warn("result did not have data or is not array. {}", result);
            }
        } catch (MalformedURLException ex) {
            logger.error("Failed to retrieve events! {}. {}", ex.getMessage(), ex.getStackTrace());
        } catch (IOException e) {
            logger.error("Failed to retrieve events! {}. {}", e.getMessage(), e.getStackTrace());
        }
    }

}

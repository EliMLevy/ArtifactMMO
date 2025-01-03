package com.elimelvy.artifacts;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elimelvy.artifacts.PlanGenerator.PlanAction;
import com.elimelvy.artifacts.model.PlanStep;
import com.elimelvy.artifacts.util.InstantTypeAdapter;
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
    private static final Gson gson = InstantTypeAdapter.createGsonWithInstant();

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
                    // TODO in the future we can delegate some to each interesting event
                    if (handleEvent(event)) {
                        break;
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

    public boolean handleEvent(JsonElement event) {
        this.logger.info("Event manager running!");
        if (event.isJsonObject()) {
            JsonObject eventObj = event.getAsJsonObject();
            if (eventObj.has("code") && eventObj.get("code").isJsonPrimitive()) {
                logger.info("Event: {}", eventObj.get("code").getAsString());
                if (interestingEvents.containsKey(eventObj.get("code").getAsString())) {
                    // Alert the character manager if they are
                    Instant expiration = gson.fromJson(eventObj.get("expiration"), Instant.class);
                    Duration d = Duration.between(expiration, Instant.now()).abs();
                    logger.info("Event is interesting! Assigning characters to it for {} seconds! {}",
                            d.toSeconds(), eventObj.get("code").getAsString());
                    if (mgr != null) {
                        Map<String, PlanStep> assignedTasks = mgr.getAllAssignedTasks();
                        for (Map.Entry<String, PlanStep> assignment : assignedTasks.entrySet()) {
                            String name = assignment.getKey();
                            if(assignment.getValue().action == PlanAction.EVENT) { // Only assign characters who arent working on an event right now
                                logger.info("Character {} is busy with event {}", name, assignment.getValue().code);
                            } else {
                                mgr.getCharacter(name).pausePendingTasks();
                                mgr.assignSpecificCharacterToTask(name, interestingEvents.get(eventObj.get("code").getAsString()));
                                mgr.scheduleAssignToTask(name, assignedTasks.get(name), d.toSeconds(), TimeUnit.SECONDS);
                            }
                        }
                    }
                    // Assign characters to the first interesting event we see.
                    return true;
                }
            } else {
                logger.warn("Event did not have a code or it was not primitive. {}", eventObj);
            }
        } else {
            logger.warn("Event was not a json object. {}", event);
        }
        return false;
    }

    public static void main(String[] args) {
        String exampleEvent = """
                              {\r
                                    "name": "name",\r
                                    "code": "code",\r
                                    "map": {\r
                                      "name": "mapname",\r
                                      "skin": "mapskin",\r
                                      "x": 0,\r
                                      "y": 1,\r
                                      "content": {\r
                                        "type": "contenttype",\r
                                        "code": "contentcode"\r
                                      }\r
                                    },\r
                                    "previous_skin": "prevskin",\r
                                    "duration": 100,\r
                                    "expiration": "2024-12-15T17:44:22Z",\r
                                    "created_at": "2024-12-15T14:15:22Z"\r
                                  }""";
        EventManager mgr = new EventManager(
                Map.of("code", new PlanStep(PlanAction.ATTACK, "exampleEvent", 0, "exampleEvent description")), null);
        // mgr.run();
        mgr.handleEvent(gson.fromJson(exampleEvent, JsonObject.class));
    }

}

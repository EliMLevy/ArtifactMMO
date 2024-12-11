package com.elimelvy.artifacts.ArtifactCharacter;

import com.elimelvy.artifacts.AtomicActions;
import com.elimelvy.artifacts.model.map.MapManager;
import com.elimelvy.artifacts.model.map.MapTile;
import com.elimelvy.artifacts.Character;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CharacterMovementService {
    private final Character character;
    private final Logger logger;

    public CharacterMovementService(Character character) {
        this.character = character;
        this.logger = LoggerFactory.getLogger("CharacterMovementService." + character.getName());

    }

    /**
     * Finds the closest map tile from a given list based on the character's current
     * position.
     * 
     * @param <T>  A type that extends MapTile
     * @param maps List of map tiles to search through
     * @return The closest map tile to the character
     */
    public <T extends MapTile> T getClosestMap(List<T> maps) {
        T closestMap = maps.get(0);
        int closestDist = Integer.MAX_VALUE;
        for (T m : maps) {
            int dist = Math.abs(m.getX() - character.getData().x) + Math.abs(m.getY() - character.getData().y);
            if (dist < closestDist) {
                closestMap = m;
                closestDist = dist;
            }
        }
        return closestMap;
    }

    /**
     * Moves the character to a specific map location based on the map code.
     * 
     * @param mapCode The code of the map to move to
     */
    public void moveToMap(String mapCode) {
        List<MapTile> targets = MapManager.getInstance().getMap(mapCode);
        if (!targets.isEmpty()) {
            MapTile target = getClosestMap(targets);
            if (character.getData().x != target.getX() || character.getData().y != target.getY()) {
                JsonObject response = AtomicActions.move(character.getName(), target.getX(), target.getY());
                character.handleActionResult(response);
            }
        } else {
            logger.warn("Tried to move to invalid map: {}", mapCode);
        }
    }

    /**
     * Moves the character to the closest bank location.
     */
    public void moveToClosestBank() {
        List<MapTile> banks = MapManager.getInstance().getMap("bank");
        MapTile closestBank = getClosestMap(banks);
        if (character.getData().x != closestBank.getX() || character.getData().y != closestBank.getY()) {
            JsonObject result = AtomicActions.move(character.getName(), closestBank.getX(), closestBank.getY());
            character.handleActionResult(result);
        }
    }
}
package com.elimelvy.artifacts;

import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncyclopediaMaker implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(EncyclopediaMaker.class);

    public static void fetchAllPagesAndSave(String url, String output) throws Exception {
        int page = 1;
        int pages = 2;
        List<JSONObject> allResults = new ArrayList<>();
        while (page <= pages) {
            System.out.println(url + "&page=" + page);
            HttpURLConnection connection = (HttpURLConnection) new URL(url + "&page=" + page).openConnection();
            connection.setRequestProperty("Accept", "application/json");

            InputStream responseStream = connection.getInputStream();
            String response = new String(responseStream.readAllBytes());

            JSONObject data = new JSONObject(response);
            page = data.getInt("page") + 1;
            pages = data.getInt("pages");

            JSONArray dataPayload = data.getJSONArray("data");
            for (int i = 0; i < dataPayload.length(); i++) {
                allResults.add(dataPayload.getJSONObject(i));
            }

            Thread.sleep(1000);
        }

        try (FileWriter file = new FileWriter(output)) {
            file.write(allResults.toString());
        }
    }

    public static void mapCombination() throws Exception {
        Map<String, List<JSONObject>> allMaps = new HashMap<>();

        String unfilteredMapsContent = Files.readString(Paths.get("maps/all_maps.json"));
        JSONArray unfilteredMaps = new JSONArray(unfilteredMapsContent);

        for (int i = 0; i < unfilteredMaps.length(); i++) {
            JSONObject map = unfilteredMaps.getJSONObject(i);
            if (map.has("content") && !map.isNull("content")) {
                JSONObject content = map.getJSONObject("content");
                String code = content.getString("code");

                map = new JSONObject()
                        .put("x", map.getInt("x"))
                        .put("y", map.getInt("y"))
                        .put("content", content);

                allMaps.computeIfAbsent(code, k -> new ArrayList<>()).add(map);
            }
        }

        try (FileWriter file = new FileWriter("maps/all_interesting_maps.json")) {
            file.write(new JSONObject(allMaps).toString());
        }
    }

    public static void combineMapsAndResources() throws Exception {
        String resourcesContent = Files.readString(Paths.get("resources/all_resources.json"));
        JSONArray resources = new JSONArray(resourcesContent);

        String mapsContent = Files.readString(Paths.get("maps/all_interesting_maps.json"));
        JSONObject maps = new JSONObject(mapsContent);

        CSVFormat.Builder.create();
        try (CSVPrinter printer = new CSVPrinter(new FileWriter("src/main/resources/resources.csv"),
                CSVFormat.Builder.create().setHeader("resource_code", "x", "y", "drop_chance", "map_code",
                        "level", "skill").build())) {

            for (int i = 0; i < resources.length(); i++) {
                JSONObject resource = resources.getJSONObject(i);
                String resourceCode = resource.getString("code");

                if (maps.has(resourceCode)) {
                    JSONArray locations = maps.getJSONArray(resourceCode);

                    JSONArray drops = resource.getJSONArray("drops");
                    for (int j = 0; j < drops.length(); j++) {
                        JSONObject drop = drops.getJSONObject(j);
                        for (int k = 0; k < locations.length(); k++) {
                            JSONObject location = locations.getJSONObject(k);
                            printer.printRecord(drop.getString("code"), location.getInt("x"), location.getInt("y"),
                                    drop.getDouble("rate"), location.getJSONObject("content").getString("code"),
                                    resource.getInt("level"), resource.getString("skill"));
                        }
                    }
                }
            }
        }
    }

    public static void combineMapsAndMonsters() throws Exception {
        String monstersContent = Files.readString(Paths.get("monsters/all_monsters.json"));
        JSONArray monsters = new JSONArray(monstersContent);

        String mapsContent = Files.readString(Paths.get("maps/all_interesting_maps.json"));
        JSONObject maps = new JSONObject(mapsContent);

        CSVFormat.Builder.create();
        try (CSVPrinter printer = new CSVPrinter(new FileWriter("src/main/resources/monsters.csv"),
                CSVFormat.Builder.create()
                        .setHeader("level", "resource_code", "x", "y", "drop_chance", "map_code", "hp", "attack_fire",
                                "attack_earth", "attack_water", "attack_air", "res_fire", "res_earth", "res_water",
                                "res_air")
                        .build())) {

            for (int i = 0; i < monsters.length(); i++) {
                JSONObject monster = monsters.getJSONObject(i);
                String monsterCode = monster.getString("code");

                if (maps.has(monsterCode)) {
                    JSONArray locations = maps.getJSONArray(monsterCode);

                    JSONArray drops = monster.getJSONArray("drops");
                    for (int j = 0; j < drops.length(); j++) {
                        JSONObject drop = drops.getJSONObject(j);
                        for (int k = 0; k < locations.length(); k++) {
                            JSONObject location = locations.getJSONObject(k);
                            printer.printRecord(monster.get("level"), drop.getString("code"), location.getInt("x"),
                                    location.getInt("y"), drop.getDouble("rate"),
                                    location.getJSONObject("content").getString("code"), monster.get("hp"),
                                    monster.get("attack_fire"), monster.get("attack_earth"),
                                    monster.get("attack_water"), monster.get("attack_air"), monster.get("res_fire"),
                                    monster.get("res_earth"), monster.get("res_water"), monster.get("res_air"));
                        }
                    }
                }
            }
        }
    }

    public static void convertMapsFromJsonToCsv() throws Exception {
        String mapsContent = Files.readString(Paths.get("maps/all_interesting_maps.json"));
        JSONObject data = new JSONObject(mapsContent);

        try (CSVPrinter printer = new CSVPrinter(new FileWriter("src/main/resources/all_maps.csv"), CSVFormat.Builder.create().setHeader(
                "x", "y", "content_type", "content_code").build())) {

            for (String contentCode : data.keySet()) {
                JSONArray items = data.getJSONArray(contentCode);
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    printer.printRecord(item.getInt("x"), item.getInt("y"),
                            item.getJSONObject("content").getString("type"), contentCode);
                }
            }
        }
    }

    public static void convertItemsToDict() throws Exception {

        String itemsContent = Files.readString(Paths.get("items/all_items.json"));
        JSONArray items = new JSONArray(itemsContent);
        Map<String, JSONObject> allItems = new HashMap<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            allItems.put(item.getString("code"), item);
        }

        try (FileWriter file = new FileWriter("src/main/resources/all_items.json")) {
            file.write(new JSONObject(allItems).toString());
        }
    }

    public static void main(String[] args) throws Exception {
        new EncyclopediaMaker().run();
    }

    @Override
    public void run() {
        try {
            fetchAllPagesAndSave("https://api.artifactsmmo.com/maps?size=100", "maps/all_maps.json");
            fetchAllPagesAndSave("https://api.artifactsmmo.com/items?size=100", "items/all_items.json");
            fetchAllPagesAndSave("https://api.artifactsmmo.com/monsters?size=100", "monsters/all_monsters.json");
            fetchAllPagesAndSave("https://api.artifactsmmo.com/resources?size=100", "resources/all_resources.json");
    
            mapCombination();
            convertMapsFromJsonToCsv();
            combineMapsAndResources();
            combineMapsAndMonsters();
            convertItemsToDict();
        } catch (Exception e) {
            logger.error("Failed to create encyclopedia, {}. {}", e.getMessage(), e.getStackTrace());
        }
    }
}

package com.elimelvy.artifacts.util;



import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class HTTPRequester {
    private static final String BASE_URL = "https://api.artifactsmmo.com/my/";
    private static final String API_TOKEN;
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient;
    private static final Logger logger = LoggerFactory.getLogger(HTTPRequester.class);

    // Static initializer to load environment variables and set up HTTP client
    static {
        API_TOKEN = System.getenv("ARTIFACTS_API_KEY");
        
        httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    /**
     * Sends a request for a specific character to the API
     * 
     * @param character The character identifier
     * @param path The API endpoint path
     * @param method The HTTP method to use
     * @param body The request body
     * @return The API response as a JsonObject
     */
    public static JsonObject sendCharacterRequest(String character, String path, String method, Object body) {
        String url = BASE_URL + character;
        return sendRequestToUrl(url, path, method, body);
    }

    /**
     * Sends a request to a specific URL with given parameters
     * 
     * @param url The base URL
     * @param path The API endpoint path
     * @param method The HTTP method to use
     * @param body The request body
     * @return The API response as a JsonObject
     */
    public static JsonObject sendRequestToUrl(String url, String path, String method, Object body) {
        try {
            // Prepare the full URL
            String fullUrl = url + path;
            // Create request builder
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_TOKEN);

            // Set method and body
            if (body != null) {
                String jsonBody = gson.toJson(body);
                requestBuilder = requestBuilder
                    .method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } else {
                requestBuilder = requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            // Send request
            HttpResponse<String> response = httpClient.send(
                requestBuilder.build(), 
                HttpResponse.BodyHandlers.ofString()
            );

            // Parse response
            JsonObject result = gson.fromJson(response.body(), JsonObject.class);

            // Check for errors
            if (result.has("error")) {
                logger.error("Failed: " + url + path + " - " + result);
            }
            if (response.statusCode() != 200) {
                logger.error("Unexpected status: " + response.statusCode() + " - " + result);
            }

            return result;

        } catch (JsonSyntaxException | IOException | InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handles cooldown from API response
     * 
     * @param result The API response
     */
    public static void handleResultCooldown(JsonObject result) {
        if (result == null) return;

        if (result.has("data") && result.getAsJsonObject("data").has("cooldown")) {
            JsonObject cooldown = result.getAsJsonObject("data").getAsJsonObject("cooldown");
            int remainingSeconds = cooldown.get("remaining_seconds").getAsInt();

            try {
                TimeUnit.SECONDS.sleep(remainingSeconds + 1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Cooldown sleep interrupted");
            }
        }
    }
}
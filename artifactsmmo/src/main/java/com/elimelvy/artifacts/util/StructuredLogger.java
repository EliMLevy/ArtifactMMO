package com.elimelvy.artifacts.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StructuredLogger {

    // Singleton instance
    private static StructuredLogger instance;

    // File path where logs will be stored
    private final String logFilePath = "events.log";

    // Formatter for timestamp
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Private constructor to enforce singleton pattern
    private StructuredLogger() {
    }

    // Public method to get the singleton instance
    public static StructuredLogger getInstance() {
        if (instance == null) {
            synchronized (StructuredLogger.class) {
                if (instance == null) {
                    instance = new StructuredLogger();
                }
            }
        }
        return instance;
    }

    /**
     * Appends a structured log message to the file.
     *
     * @param eventType    The type of the event.
     * @param requestBody  The request body content.
     * @param responseBody The response body content.
     * @param cooldown     The cooldown value.
     * @param timestamp    The event timestamp.
     */
    public void logEvent(String eventType, String character, String requestBody, String responseBody, long cooldown) {
        // Format the log message as a JSON-like structure
        String logMessage = String.format(
                "%s|%s|%s|%s|%s|%d",
                LocalDateTime.now().format(formatter), eventType, character, requestBody, responseBody, cooldown);

        // Append the log message to the file
        synchronized (this) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
                writer.write(logMessage);
                writer.newLine(); // Add a new line after each log entry
            } catch (IOException e) {
                System.err.println("Error writing log: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {

        // StructuredLogger.getInstance().logEvent("CRAFT", "{'quantity': 10, 'code':'iron_helmet'}", "Success", 100, LocalDateTime.now());
        
    }
}

package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    // Tatum API endpoint URL for getting ETH/USD price
    private static final String API_URL =
            "https://api.tatum.io/v4/data/rate/symbol?symbol=ETH&basePair=USD";

    // Format for displaying time
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {

        // Retrieve API key from environment variable
        String apiKey = System.getenv("TATUM_API_KEY");

        // Checking that the API key exists
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ERROR: Missing TATUM_API_KEY environment variable.");
            System.out.println("Set it in PowerShell:");
            System.out.println("$env:TATUM_API_KEY=\"your_api_key\"");
            return;
        }

        // Interval between measurements in seconds
        int intervalSeconds = 30;

        // Program header
        System.out.println("========================================");
        System.out.println("   ETH/USD Price Tracker (Tatum API)   ");
        System.out.println("========================================");
        System.out.println();

        // First price measurement
        double price1 = fetchPrice(apiKey);
        String time1 = now();

        System.out.printf("First measurement  (%s)%n", time1);
        System.out.printf("ETH/USD price:     %.4f%n", price1);
        System.out.println();

        // Pause between measurements
        System.out.printf("Waiting %d seconds before next measurement...%n", intervalSeconds);
        System.out.println();

        Thread.sleep(intervalSeconds * 1000);

        // Second price measurement
        double price2 = fetchPrice(apiKey);
        String time2 = now();

        System.out.printf("Second measurement (%s)%n", time2);
        System.out.printf("ETH/USD price:     %.4f%n", price2);
        System.out.println();

        // Price change calculation
        double diff = price2 - price1;
        double pct = (diff / price1) * 100.0;

        // Summary of results
        System.out.println("========================================");
        System.out.println("               SUMMARY                  ");
        System.out.println("========================================");

        System.out.printf("Price change:      %.4f USD%n", diff);
        System.out.printf("Percentage change: %.4f%%%n", pct);
        System.out.println("========================================");
    }

    /**
     * The method calls the Tatum API and returns the current ETH/USD price.
     */
    private static double fetchPrice(String apiKey) throws Exception {

        // Creating an HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Creating an HTTP GET request with the API key in the header
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("x-api-key", apiKey)
                .GET()
                .build();

        // Sending a request and getting a response
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Checking HTTP status
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error: " + response.statusCode()
                    + " Body: " + response.body());
        }

        // Parsing JSON response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        // Getting the price value from the JSON field "value"
        JsonNode valueNode = root.get("value");
        if (valueNode == null) {
            throw new RuntimeException("Unexpected response: " + response.body());
        }

        // Returning price as a number
        return valueNode.asDouble();
    }

    /**
     * The method returns the current time as text.
     */
    private static String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }
}



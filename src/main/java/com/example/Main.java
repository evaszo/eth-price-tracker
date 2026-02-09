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

    // URL endpointu Tatum API pro získání ceny ETH/USD
    private static final String API_URL =
            "https://api.tatum.io/v4/data/rate/symbol?symbol=ETH&basePair=USD";

    // Formát pro zobrazení času ve výstupu
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {

        // Načtení API klíče z environment variable (bezpečně, není v kódu)
        String apiKey = System.getenv("TATUM_API_KEY");

        // Kontrola, že API klíč existuje
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("ERROR: Missing TATUM_API_KEY environment variable.");
            System.out.println("Set it in PowerShell:");
            System.out.println("$env:TATUM_API_KEY=\"your_api_key\"");
            return;
        }

        // Interval mezi měřeními v sekundách
        int intervalSeconds = 30;

        // Hlavička programu
        System.out.println("========================================");
        System.out.println("   ETH/USD Price Tracker (Tatum API)   ");
        System.out.println("========================================");
        System.out.println();

        // První měření ceny
        double price1 = fetchPrice(apiKey);
        String time1 = now();

        System.out.printf("First measurement  (%s)%n", time1);
        System.out.printf("ETH/USD price:     %.4f%n", price1);
        System.out.println();

        // Pauza mezi měřeními
        System.out.printf("Waiting %d seconds before next measurement...%n", intervalSeconds);
        System.out.println();

        Thread.sleep(intervalSeconds * 1000);

        // Druhé měření ceny
        double price2 = fetchPrice(apiKey);
        String time2 = now();

        System.out.printf("Second measurement (%s)%n", time2);
        System.out.printf("ETH/USD price:     %.4f%n", price2);
        System.out.println();

        // Výpočet změny ceny
        double diff = price2 - price1;
        double pct = (diff / price1) * 100.0;

        // Shrnutí výsledků
        System.out.println("========================================");
        System.out.println("               SUMMARY                  ");
        System.out.println("========================================");

        System.out.printf("Price change:      %.4f USD%n", diff);
        System.out.printf("Percentage change: %.4f%%%n", pct);
        System.out.println("========================================");
    }

    /**
     * Metoda zavolá Tatum API a vrátí aktuální cenu ETH/USD.
     */
    private static double fetchPrice(String apiKey) throws Exception {

        // Vytvoření HTTP klienta
        HttpClient client = HttpClient.newHttpClient();

        // Vytvoření HTTP GET requestu s API klíčem v headeru
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("x-api-key", apiKey)
                .GET()
                .build();

        // Odeslání requestu a získání odpovědi
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Kontrola HTTP statusu
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error: " + response.statusCode()
                    + " Body: " + response.body());
        }

        // Parsování JSON odpovědi
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        // Získání hodnoty ceny z JSON pole "value"
        JsonNode valueNode = root.get("value");
        if (valueNode == null) {
            throw new RuntimeException("Unexpected response: " + response.body());
        }

        // Vrácení ceny jako čísla
        return valueNode.asDouble();
    }

    /**
     * Pomocná metoda vrátí aktuální čas jako text.
     */
    private static String now() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }
}



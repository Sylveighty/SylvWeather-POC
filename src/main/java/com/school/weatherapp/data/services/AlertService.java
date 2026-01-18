package com.school.weatherapp.data.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.data.models.Alert;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AlertService - Service for fetching weather alerts
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 3)
 */
public class AlertService {
    
    private final HttpClient httpClient;
    
    public AlertService() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    /**
     * Fetch weather alerts for a city (asynchronous)
     * 
     * @param cityName Name of the city
     * @return CompletableFuture containing list of Alert objects
     */
    public CompletableFuture<List<Alert>> getAlertsAsync(String cityName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAlerts(cityName);
            } catch (Exception e) {
                System.err.println("Error fetching alerts: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * Fetch weather alerts for a city (synchronous)
     * 
     * @param cityName Name of the city
     * @return List of Alert objects
     * @throws Exception if API call fails
     */
    public List<Alert> getAlerts(String cityName) throws Exception {
        // Get coordinates first
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        
        // Step 1: Geocode city to get lat/lon
        String geoUrl = String.format("%s/geo/1.0/direct?q=%s&limit=1&appid=%s",
            AppConfig.WEATHER_API_BASE_URL.replace("/data/2.5", ""),
            encodedCity,
            AppConfig.WEATHER_API_KEY
        );
        
        HttpRequest geoRequest = HttpRequest.newBuilder()
            .uri(URI.create(geoUrl))
            .GET()
            .build();
        
        HttpResponse<String> geoResponse = httpClient.send(geoRequest, 
            HttpResponse.BodyHandlers.ofString());
        
        if (geoResponse.statusCode() != 200) {
            return new ArrayList<>();
        }
        
        JsonArray geoArray = JsonParser.parseString(geoResponse.body()).getAsJsonArray();
        if (geoArray.size() == 0) {
            return new ArrayList<>();
        }
        
        JsonObject geoData = geoArray.get(0).getAsJsonObject();
        double lat = geoData.get("lat").getAsDouble();
        double lon = geoData.get("lon").getAsDouble();
        
        // Step 2: Fetch alerts using coordinates
        String alertUrl = String.format("%s/data/2.5/alerts?lat=%f&lon=%f&appid=%s",
            AppConfig.WEATHER_API_BASE_URL.replace("/data/2.5", ""),
            lat, lon,
            AppConfig.WEATHER_API_KEY
        );
        
        HttpRequest alertRequest = HttpRequest.newBuilder()
            .uri(URI.create(alertUrl))
            .GET()
            .build();
        
        HttpResponse<String> alertResponse = httpClient.send(alertRequest, 
            HttpResponse.BodyHandlers.ofString());
        
        if (alertResponse.statusCode() != 200) {
            return new ArrayList<>();
        }
        
        return parseAlertsResponse(alertResponse.body());
    }
    
    /**
     * Parse JSON response into Alert objects
     */
    private List<Alert> parseAlertsResponse(String jsonResponse) {
        List<Alert> alerts = new ArrayList<>();
        
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            
            if (!json.has("alerts")) {
                return alerts;
            }
            
            JsonArray alertsArray = json.getAsJsonArray("alerts");
            
            for (int i = 0; i < alertsArray.size(); i++) {
                JsonObject alertJson = alertsArray.get(i).getAsJsonObject();
                Alert alert = new Alert();
                
                alert.setId(alertJson.has("event") ? alertJson.get("event").getAsString() : "unknown");
                alert.setTitle(alertJson.has("event") ? alertJson.get("event").getAsString() : "Weather Alert");
                alert.setDescription(alertJson.has("description") ? alertJson.get("description").getAsString() : "");
                alert.setTimestamp(alertJson.has("start") ? alertJson.get("start").getAsLong() : System.currentTimeMillis() / 1000);
                
                // Determine severity based on alert type
                String event = alert.getTitle().toLowerCase();
                if (event.contains("warning") || event.contains("severe")) {
                    alert.setSeverity("high");
                } else if (event.contains("watch")) {
                    alert.setSeverity("medium");
                } else {
                    alert.setSeverity("low");
                }
                
                alerts.add(alert);
            }
        } catch (Exception e) {
            System.err.println("Error parsing alerts: " + e.getMessage());
        }
        
        return alerts;
    }
}

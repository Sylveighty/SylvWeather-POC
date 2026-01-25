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
 * AlertService - Fetches weather alerts for a city.
 *
 * POC behavior:
 * - Attempts to retrieve alerts using a geocoding step (city -> lat/lon).
 * - Then attempts an alerts-capable endpoint.
 * - If the API path is unavailable or fails, returns simulated alerts so the UI can demonstrate the feature.
 *
 * This keeps the app demo-friendly even when alert data is not available for a location or plan.
 */
public class AlertService {

    private static final String OPENWEATHER_BASE_URL = "https://api.openweathermap.org";
    private final HttpClient httpClient;

    public AlertService() {
        this.httpClient = HttpClient.newHttpClient();
    }

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

    public List<Alert> getAlerts(String cityName) {
        try {
            List<Alert> fromApi = fetchFromApi(cityName);
            if (!fromApi.isEmpty()) {
                return fromApi;
            }
        } catch (Exception e) {
            System.out.println("Alert API unavailable, using simulated alerts.");
        }
        return getSimulatedAlerts();
    }

    private List<Alert> fetchFromApi(String cityName) throws Exception {
        double[] coords = getCityCoordinates(cityName);
        if (coords == null) {
            return new ArrayList<>();
        }

        // Attempt an alerts-capable endpoint.
        // Note: OpenWeather "alerts" are commonly provided via One Call API responses.
        String url = buildOneCallUrl(coords[0], coords[1]);

        HttpResponse<String> response = sendHttpRequest(url);
        validateApiResponse(response);

        return parseAlertsResponse(response.body());
    }

    /**
     * Uses the OpenWeather Geocoding API:
     * GET /geo/1.0/direct?q={city}&limit=1&appid={key}
     */
    private double[] getCityCoordinates(String cityName) throws Exception {
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String geoUrl = String.format(
            "%s/geo/1.0/direct?q=%s&limit=1&appid=%s",
            OPENWEATHER_BASE_URL,
            encodedCity,
            AppConfig.WEATHER_API_KEY
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(geoUrl))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) return null;

        JsonArray geoArray = JsonParser.parseString(response.body()).getAsJsonArray();
        if (geoArray.size() == 0) return null;

        JsonObject geoData = geoArray.get(0).getAsJsonObject();
        double lat = geoData.get("lat").getAsDouble();
        double lon = geoData.get("lon").getAsDouble();

        return new double[]{lat, lon};
    }

    /**
     * One Call (alerts-capable) request.
     * We exclude other blocks to keep response smaller (POC).
     *
     * Note: Alerts availability may depend on API plan/region.
     */
    private String buildOneCallUrl(double lat, double lon) {
        return String.format(
            "%s/data/3.0/onecall?lat=%f&lon=%f&exclude=current,minutely,hourly,daily&appid=%s",
            OPENWEATHER_BASE_URL,
            lat, lon,
            AppConfig.WEATHER_API_KEY
        );
    }

    private HttpResponse<String> sendHttpRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void validateApiResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() != 200) {
            throw new Exception("API returned status code: " + response.statusCode());
        }
    }

    /**
     * Expected JSON: { "alerts": [ ... ] }
     * If "alerts" is missing, returns an empty list (caller may fall back to simulated alerts).
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
                alerts.add(createAlertFromJson(alertJson));
            }
        } catch (Exception e) {
            System.err.println("Error parsing alerts: " + e.getMessage());
        }

        return alerts;
    }

    private Alert createAlertFromJson(JsonObject alertJson) {
        Alert alert = new Alert();

        String event = alertJson.has("event") ? alertJson.get("event").getAsString() : "Weather Alert";
        String description = alertJson.has("description") ? alertJson.get("description").getAsString() : "";

        alert.setId(event);
        alert.setTitle(event);
        alert.setDescription(description);
        long start = alertJson.has("start") ? alertJson.get("start").getAsLong() : (System.currentTimeMillis() / 1000);
        long end = alertJson.has("end") ? alertJson.get("end").getAsLong() : 0L;
        alert.setTimestamp(start);
        alert.setEffectiveStart(start);
        alert.setEffectiveEnd(end);

        // Simple severity heuristic (POC).
        String lower = event.toLowerCase();
        if (lower.contains("warning") || lower.contains("severe")) {
            alert.setSeverity("high");
        } else if (lower.contains("watch")) {
            alert.setSeverity("medium");
        } else {
            alert.setSeverity("low");
        }

        return alert;
    }

    /**
     * Simulated alerts for demo/testing when live alerts are unavailable.
     */
    private List<Alert> getSimulatedAlerts() {
        List<Alert> alerts = new ArrayList<>();

        Alert alert1 = new Alert();
        alert1.setId("alert-001");
        alert1.setTitle("Moderate Wind Advisory");
        alert1.setDescription("Winds 25-35 mph expected through tonight. Secure outdoor objects.");
        alert1.setSeverity("medium");
        long now = System.currentTimeMillis() / 1000;
        alert1.setTimestamp(now);
        alert1.setEffectiveStart(now);
        alert1.setEffectiveEnd(now + (6 * 60 * 60));
        alerts.add(alert1);

        return alerts;
    }
}

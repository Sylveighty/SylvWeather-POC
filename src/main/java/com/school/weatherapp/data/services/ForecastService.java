package com.school.weatherapp.data.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.data.models.Forecast;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ForecastService - Service for fetching forecast data from OpenWeatherMap API
 * 
 * This service handles:
 * - 5-day/3-hour forecast (used for both daily and hourly views)
 * - Parsing forecast data into Forecast objects
 * - Grouping hourly data into daily summaries
 * - Async requests for non-blocking UI
 * 
 * API Documentation: https://openweathermap.org/forecast5
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 2)
 */
public class ForecastService {
    
    private final HttpClient httpClient;
    
    /**
     * Constructor - initializes HTTP client
     */
    public ForecastService() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    /**
     * Fetch 5-day forecast for a city (asynchronous)
     * 
     * @param cityName Name of the city
     * @return CompletableFuture containing list of Forecast objects
     */
    public CompletableFuture<List<Forecast>> getForecastAsync(String cityName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getForecast(cityName);
            } catch (Exception e) {
                System.err.println("Error fetching forecast: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * Fetch 5-day forecast for a city (synchronous)
     * Returns 3-hour interval forecasts
     * 
     * @param cityName Name of the city
     * @return List of Forecast objects
     * @throws Exception if API call fails
     */
    public List<Forecast> getForecast(String cityName) throws Exception {
        // Build API URL
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String units = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "imperial" : "metric";
        
        String url = String.format("%s/forecast?q=%s&units=%s&appid=%s",
            AppConfig.WEATHER_API_BASE_URL,
            encodedCity,
            units,
            AppConfig.WEATHER_API_KEY
        );
        
        // Create HTTP request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        
        // Send request and get response
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        // Check for successful response
        if (response.statusCode() != 200) {
            throw new Exception("API returned status code: " + response.statusCode());
        }
        
        // Parse JSON response
        return parseForecastResponse(response.body());
    }
    
    /**
     * Parse JSON response from OpenWeatherMap API into list of Forecast objects
     * 
     * @param jsonResponse JSON string from API
     * @return List of Forecast objects
     */
    private List<Forecast> parseForecastResponse(String jsonResponse) {
        List<Forecast> forecasts = new ArrayList<>();
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray list = json.getAsJsonArray("list");
        
        for (int i = 0; i < list.size(); i++) {
            JsonObject item = list.get(i).getAsJsonObject();
            Forecast forecast = new Forecast();
            
            // Parse timestamp
            long timestamp = item.get("dt").getAsLong();
            forecast.setTimestamp(timestamp);
            
            // Parse time labels
            Instant instant = Instant.ofEpochSecond(timestamp);
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE")
                .withZone(ZoneId.systemDefault());
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
                .withZone(ZoneId.systemDefault());
            
            forecast.setDayOfWeek(dayFormatter.format(instant));
            forecast.setTimeLabel(timeFormatter.format(instant));
            
            // Parse main weather data
            JsonObject main = item.getAsJsonObject("main");
            forecast.setTemperature(main.get("temp").getAsDouble());
            forecast.setFeelsLike(main.get("feels_like").getAsDouble());
            forecast.setTempMin(main.get("temp_min").getAsDouble());
            forecast.setTempMax(main.get("temp_max").getAsDouble());
            forecast.setHumidity(main.get("humidity").getAsInt());
            
            // Parse weather condition
            JsonObject weather = item.getAsJsonArray("weather").get(0).getAsJsonObject();
            forecast.setCondition(weather.get("main").getAsString());
            forecast.setDescription(weather.get("description").getAsString());
            forecast.setIconCode(weather.get("id").getAsString());
            
            // Parse wind
            JsonObject wind = item.getAsJsonObject("wind");
            forecast.setWindSpeed(wind.get("speed").getAsDouble());
            
            // Parse precipitation probability (if available)
            if (item.has("pop")) {
                int precipPercent = (int) (item.get("pop").getAsDouble() * 100);
                forecast.setPrecipitation(precipPercent);
            }
            
            forecasts.add(forecast);
        }
        
        return forecasts;
    }
    
    /**
     * Get hourly forecast for today (next 24 hours)
     * Filters forecast data to show next 8 entries (24 hours in 3-hour intervals)
     * 
     * @param cityName Name of the city
     * @return List of hourly Forecast objects
     */
    public CompletableFuture<List<Forecast>> getHourlyForecastAsync(String cityName) {
        return getForecastAsync(cityName).thenApply(forecasts -> {
            // Return first 8 entries (24 hours)
            return forecasts.subList(0, Math.min(8, forecasts.size()));
        });
    }
    
    /**
     * Get 7-day daily forecast
     * Groups 3-hour forecasts by day and calculates daily min/max
     * 
     * @param cityName Name of the city
     * @return List of daily Forecast objects
     */
    public CompletableFuture<List<Forecast>> getDailyForecastAsync(String cityName) {
        return getForecastAsync(cityName).thenApply(this::groupForecastsByDay);
    }
    
    /**
     * Group 3-hour forecasts into daily forecasts
     * Calculates min/max temperatures and most common condition per day
     * 
     * @param forecasts List of 3-hour forecasts
     * @return List of daily forecasts
     */
    private List<Forecast> groupForecastsByDay(List<Forecast> forecasts) {
        Map<String, List<Forecast>> dayGroups = new HashMap<>();
        
        // Group by day
        for (Forecast forecast : forecasts) {
            String day = forecast.getDayOfWeek();
            dayGroups.computeIfAbsent(day, k -> new ArrayList<>()).add(forecast);
        }
        
        // Create daily summaries
        List<Forecast> dailyForecasts = new ArrayList<>();
        
        for (Map.Entry<String, List<Forecast>> entry : dayGroups.entrySet()) {
            List<Forecast> dayForecasts = entry.getValue();
            if (dayForecasts.isEmpty()) continue;
            
            Forecast dailyForecast = new Forecast();
            
            // Use first forecast for base data
            Forecast first = dayForecasts.get(0);
            dailyForecast.setDayOfWeek(first.getDayOfWeek());
            dailyForecast.setTimeLabel(first.getDayOfWeek());
            dailyForecast.setTimestamp(first.getTimestamp());
            
            // Calculate min/max temps
            double minTemp = dayForecasts.stream()
                .mapToDouble(Forecast::getTempMin)
                .min()
                .orElse(first.getTempMin());
            
            double maxTemp = dayForecasts.stream()
                .mapToDouble(Forecast::getTempMax)
                .max()
                .orElse(first.getTempMax());
            
            dailyForecast.setTempMin(minTemp);
            dailyForecast.setTempMax(maxTemp);
            dailyForecast.setTemperature((minTemp + maxTemp) / 2);
            
            // Use midday forecast for condition (around noon)
            Forecast midday = dayForecasts.get(dayForecasts.size() / 2);
            dailyForecast.setCondition(midday.getCondition());
            dailyForecast.setDescription(midday.getDescription());
            dailyForecast.setIconCode(midday.getIconCode());
            
            // Average other values
            double avgHumidity = dayForecasts.stream()
                .mapToInt(Forecast::getHumidity)
                .average()
                .orElse(first.getHumidity());
            dailyForecast.setHumidity((int) avgHumidity);
            
            double avgWind = dayForecasts.stream()
                .mapToDouble(Forecast::getWindSpeed)
                .average()
                .orElse(first.getWindSpeed());
            dailyForecast.setWindSpeed(avgWind);
            
            dailyForecasts.add(dailyForecast);
            
            // Limit to 7 days
            if (dailyForecasts.size() >= 7) break;
        }
        
        return dailyForecasts;
    }
}
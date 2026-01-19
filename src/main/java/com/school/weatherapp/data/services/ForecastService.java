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
    
    // ==================== Fields ====================
    private final HttpClient httpClient;
    
    // ==================== Constructors ====================
    
    /**
     * Constructor - initializes HTTP client
     */
    public ForecastService() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    // ==================== Public Methods ====================
    
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
        String apiUrl = buildForecastApiUrl(cityName);
        HttpResponse<String> response = sendHttpRequest(apiUrl);
        validateApiResponse(response);
        
        String units = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "imperial" : "metric";
        return parseForecastResponse(response.body(), units);
    }
    
    /**
     * Get hourly forecast for today (next 24 hours)
     * Filters forecast data to show next 8 entries (24 hours in 3-hour intervals)
     * 
     * @param cityName Name of the city
     * @return CompletableFuture containing list of hourly Forecast objects
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
     * @return CompletableFuture containing list of daily Forecast objects
     */
    public CompletableFuture<List<Forecast>> getDailyForecastAsync(String cityName) {
        return getForecastAsync(cityName).thenApply(this::groupForecastsByDay);
    }
    
    // ==================== Private Methods ====================
    
    /**
     * Build the forecast API URL for the given city
     * 
     * @param cityName Name of the city
     * @return Complete API URL string
     */
    private String buildForecastApiUrl(String cityName) {
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String units = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "imperial" : "metric";
        
        return String.format("%s/forecast?q=%s&units=%s&appid=%s",
            AppConfig.WEATHER_API_BASE_URL,
            encodedCity,
            units,
            AppConfig.WEATHER_API_KEY
        );
    }
    
    /**
     * Send HTTP request to the API
     * 
     * @param url API URL to send request to
     * @return HTTP response
     * @throws Exception if request fails
     */
    private HttpResponse<String> sendHttpRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Validate that the API response was successful
     * 
     * @param response HTTP response from API
     * @throws Exception if response indicates an error
     */
    private void validateApiResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() != 200) {
            throw new Exception("API returned status code: " + response.statusCode());
        }
    }
    
    /**
     * Parse JSON response from OpenWeatherMap API into list of Forecast objects
     * 
     * @param jsonResponse JSON string from API
     * @param units The temperature units used in the API request
     * @return List of Forecast objects
     */
    private List<Forecast> parseForecastResponse(String jsonResponse, String units) {
        List<Forecast> forecasts = new ArrayList<>();
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray list = json.getAsJsonArray("list");
        
        for (int i = 0; i < list.size(); i++) {
            JsonObject item = list.get(i).getAsJsonObject();
            Forecast forecast = createForecastFromJson(item, units);
            forecasts.add(forecast);
        }
        
        return forecasts;
    }
    
    /**
     * Create a Forecast object from a JSON item
     * 
     * @param item JSON object representing a forecast entry
     * @param units Temperature units used in the API request
     * @return Forecast object populated with data
     */
    private Forecast createForecastFromJson(JsonObject item, String units) {
        Forecast forecast = new Forecast();
        
        // Parse timestamp and time labels
        parseTimestampData(item, forecast);
        
        // Parse main weather data
        parseMainForecastData(item, forecast, units);
        
        // Parse weather condition
        parseForecastCondition(item, forecast);
        
        // Parse wind data
        parseForecastWind(item, forecast);
        
        // Parse precipitation probability
        parseForecastPrecipitation(item, forecast);
        
        return forecast;
    }
    
    /**
     * Parse timestamp and time label data from JSON
     */
    private void parseTimestampData(JsonObject item, Forecast forecast) {
        long timestamp = item.get("dt").getAsLong();
        forecast.setTimestamp(timestamp);
        
        Instant instant = Instant.ofEpochSecond(timestamp);
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE")
            .withZone(ZoneId.systemDefault());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
            .withZone(ZoneId.systemDefault());
        
        forecast.setDayOfWeek(dayFormatter.format(instant));
        forecast.setTimeLabel(timeFormatter.format(instant));
    }
    
    /**
     * Parse main weather data from JSON
     */
    private void parseMainForecastData(JsonObject item, Forecast forecast, String units) {
        JsonObject main = item.getAsJsonObject("main");
        forecast.setTemperature(main.get("temp").getAsDouble());
        forecast.setFeelsLike(main.get("feels_like").getAsDouble());
        forecast.setTempMin(main.get("temp_min").getAsDouble());
        forecast.setTempMax(main.get("temp_max").getAsDouble());
        forecast.setHumidity(main.get("humidity").getAsInt());
        forecast.setTemperatureUnit(units);
    }
    
    /**
     * Parse weather condition from JSON
     */
    private void parseForecastCondition(JsonObject item, Forecast forecast) {
        JsonObject weather = item.getAsJsonArray("weather").get(0).getAsJsonObject();
        forecast.setCondition(weather.get("main").getAsString());
        forecast.setDescription(weather.get("description").getAsString());
        forecast.setIconCode(weather.get("id").getAsString());
    }
    
    /**
     * Parse wind data from JSON
     */
    private void parseForecastWind(JsonObject item, Forecast forecast) {
        JsonObject wind = item.getAsJsonObject("wind");
        forecast.setWindSpeed(wind.get("speed").getAsDouble());
    }
    
    /**
     * Parse precipitation probability from JSON
     */
    private void parseForecastPrecipitation(JsonObject item, Forecast forecast) {
        if (item.has("pop")) {
            int precipPercent = (int) (item.get("pop").getAsDouble() * 100);
            forecast.setPrecipitation(precipPercent);
        }
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
        
        // Group forecasts by day
        for (Forecast forecast : forecasts) {
            String day = forecast.getDayOfWeek();
            dayGroups.computeIfAbsent(day, k -> new ArrayList<>()).add(forecast);
        }
        
        // Create daily summaries
        List<Forecast> dailyForecasts = new ArrayList<>();
        
        for (Map.Entry<String, List<Forecast>> entry : dayGroups.entrySet()) {
            List<Forecast> dayForecasts = entry.getValue();
            if (dayForecasts.isEmpty()) continue;
            
            Forecast dailyForecast = createDailyForecast(dayForecasts);
            dailyForecasts.add(dailyForecast);
            
            // Limit to 7 days
            if (dailyForecasts.size() >= 7) break;
        }
        
        return dailyForecasts;
    }
    
    /**
     * Create a daily forecast from a list of hourly forecasts for that day
     * 
     * @param dayForecasts List of hourly forecasts for a specific day
     * @return Daily forecast with aggregated data
     */
    private Forecast createDailyForecast(List<Forecast> dayForecasts) {
        Forecast dailyForecast = new Forecast();
        
        // Use first forecast for base data
        Forecast first = dayForecasts.get(0);
        dailyForecast.setDayOfWeek(first.getDayOfWeek());
        dailyForecast.setTimeLabel(first.getDayOfWeek());
        dailyForecast.setTimestamp(first.getTimestamp());
        dailyForecast.setTemperatureUnit(first.getTemperatureUnit());
        
        // Calculate min/max temperatures
        calculateTemperatureRange(dayForecasts, dailyForecast);
        
        // Use midday forecast for condition (around noon)
        Forecast midday = dayForecasts.get(dayForecasts.size() / 2);
        dailyForecast.setCondition(midday.getCondition());
        dailyForecast.setDescription(midday.getDescription());
        dailyForecast.setIconCode(midday.getIconCode());
        
        // Calculate average values
        calculateAverageValues(dayForecasts, dailyForecast);
        
        return dailyForecast;
    }
    
    /**
     * Calculate min/max temperature range for a day
     */
    private void calculateTemperatureRange(List<Forecast> dayForecasts, Forecast dailyForecast) {
        double minTemp = dayForecasts.stream()
            .mapToDouble(Forecast::getTempMin)
            .min()
            .orElse(dayForecasts.get(0).getTempMin());
        
        double maxTemp = dayForecasts.stream()
            .mapToDouble(Forecast::getTempMax)
            .max()
            .orElse(dayForecasts.get(0).getTempMax());
        
        dailyForecast.setTempMin(minTemp);
        dailyForecast.setTempMax(maxTemp);
        dailyForecast.setTemperature((minTemp + maxTemp) / 2);
    }
    
    /**
     * Calculate average values for humidity and wind speed
     */
    private void calculateAverageValues(List<Forecast> dayForecasts, Forecast dailyForecast) {
        double avgHumidity = dayForecasts.stream()
            .mapToInt(Forecast::getHumidity)
            .average()
            .orElse(dayForecasts.get(0).getHumidity());
        dailyForecast.setHumidity((int) avgHumidity);
        
        double avgWind = dayForecasts.stream()
            .mapToDouble(Forecast::getWindSpeed)
            .average()
            .orElse(dayForecasts.get(0).getWindSpeed());
        dailyForecast.setWindSpeed(avgWind);
    }
}

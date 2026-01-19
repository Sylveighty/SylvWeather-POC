package com.school.weatherapp.data.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.data.models.Weather;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * WeatherService - Service for fetching weather data from OpenWeatherMap API
 * 
 * This service handles all API communication:
 * - Fetches current weather by city name
 * - Parses JSON responses into Weather objects
 * - Handles errors gracefully
 * - Uses async requests for non-blocking UI
 * 
 * API Documentation: https://openweathermap.org/current
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 1)
 */
public class WeatherService {
    
    private final HttpClient httpClient;
    
    /**
     * Constructor - initializes HTTP client
     */
    public WeatherService() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    /**
     * Fetch current weather for a city (asynchronous)
     * 
     * @param cityName Name of the city to fetch weather for
     * @return CompletableFuture containing Weather object or null if error
     */
    public CompletableFuture<Weather> getCurrentWeatherAsync(String cityName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getCurrentWeather(cityName);
            } catch (Exception e) {
                System.err.println("Error fetching weather: " + e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * Fetch current weather for a city (synchronous)
     * 
     * @param cityName Name of the city to fetch weather for
     * @return Weather object or null if error
     * @throws Exception if API call fails
     */
    public Weather getCurrentWeather(String cityName) throws Exception {
        // Build API URL
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String units = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "imperial" : "metric";
        
        String url = String.format("%s/weather?q=%s&units=%s&appid=%s",
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
        return parseWeatherResponse(response.body(), units);
    }
    
    /**
     * Parse JSON response from OpenWeatherMap API into Weather object
     * 
     * @param jsonResponse JSON string from API
     * @param units The temperature units used in the API request
     * @return Weather object populated with data
     */
    private Weather parseWeatherResponse(String jsonResponse, String units) {
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        Weather weather = new Weather();
        
        // Parse location
        weather.setCityName(json.get("name").getAsString());
        JsonObject sys = json.getAsJsonObject("sys");
        weather.setCountry(sys.get("country").getAsString());
        
        // Parse main weather data
        JsonObject main = json.getAsJsonObject("main");
        weather.setTemperature(main.get("temp").getAsDouble());
        weather.setFeelsLike(main.get("feels_like").getAsDouble());
        weather.setTempMin(main.get("temp_min").getAsDouble());
        weather.setTempMax(main.get("temp_max").getAsDouble());
        weather.setHumidity(main.get("humidity").getAsInt());
        weather.setPressure(main.get("pressure").getAsInt());
        
        // Set the temperature unit based on the API request
        weather.setTemperatureUnit(units);
        
        // Parse weather condition
        JsonObject weatherObj = json.getAsJsonArray("weather").get(0).getAsJsonObject();
        weather.setCondition(weatherObj.get("main").getAsString());
        weather.setDescription(weatherObj.get("description").getAsString());
        weather.setIconCode(weatherObj.get("id").getAsString());
        
        // Parse wind
        JsonObject wind = json.getAsJsonObject("wind");
        weather.setWindSpeed(wind.get("speed").getAsDouble());
        if (wind.has("deg")) {
            weather.setWindDirection(wind.get("deg").getAsInt());
        }
        
        // Parse clouds
        if (json.has("clouds")) {
            JsonObject clouds = json.getAsJsonObject("clouds");
            weather.setCloudiness(clouds.get("all").getAsInt());
        }
        
        // Parse timestamp
        weather.setTimestamp(json.get("dt").getAsLong());
        
        return weather;
    }
    
    /**
     * Check if the API key is configured
     * 
     * @return true if API key is set, false otherwise
     */
    public boolean isApiKeyConfigured() {
        return !AppConfig.WEATHER_API_KEY.equals("YOUR_API_KEY_HERE");
    }
}

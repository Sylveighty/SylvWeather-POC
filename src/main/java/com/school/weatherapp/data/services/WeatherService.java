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
    
    // ==================== Fields ====================
    private final HttpClient httpClient;
    
    // ==================== Constructors ====================
    
    /**
     * Constructor - initializes HTTP client
     */
    public WeatherService() {
        this.httpClient = HttpClient.newHttpClient();
    }
    
    // ==================== Public Methods ====================
    
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
        String apiUrl = buildWeatherApiUrl(cityName);
        HttpResponse<String> response = sendHttpRequest(apiUrl);
        validateApiResponse(response);
        
        String units = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "imperial" : "metric";
        return parseWeatherResponse(response.body(), units);
    }
    
    /**
     * Check if the API key is configured
     * 
     * @return true if API key is set, false otherwise
     */
    public boolean isApiKeyConfigured() {
        return !AppConfig.WEATHER_API_KEY.equals("YOUR_API_KEY_HERE");
    }
    
    // ==================== Private Methods ====================
    
    /**
     * Build the weather API URL for the given city
     * 
     * @param cityName Name of the city
     * @return Complete API URL string
     */
    private String buildWeatherApiUrl(String cityName) {
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String units = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "imperial" : "metric";
        
        return String.format("%s/weather?q=%s&units=%s&appid=%s",
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
     * Parse JSON response from OpenWeatherMap API into Weather object
     * 
     * @param jsonResponse JSON string from API
     * @param units The temperature units used in the API request
     * @return Weather object populated with data
     */
    private Weather parseWeatherResponse(String jsonResponse, String units) {
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        Weather weather = new Weather();
        
        // Parse location data
        parseLocationData(json, weather);
        
        // Parse main weather data
        parseMainWeatherData(json, weather, units);
        
        // Parse weather condition
        parseWeatherCondition(json, weather);
        
        // Parse wind data
        parseWindData(json, weather);
        
        // Parse additional data
        parseAdditionalData(json, weather);
        
        return weather;
    }
    
    /**
     * Parse location information from JSON
     */
    private void parseLocationData(JsonObject json, Weather weather) {
        weather.setCityName(json.get("name").getAsString());
        JsonObject sys = json.getAsJsonObject("sys");
        weather.setCountry(sys.get("country").getAsString());
    }
    
    /**
     * Parse main weather data from JSON
     */
    private void parseMainWeatherData(JsonObject json, Weather weather, String units) {
        JsonObject main = json.getAsJsonObject("main");
        weather.setTemperature(main.get("temp").getAsDouble());
        weather.setFeelsLike(main.get("feels_like").getAsDouble());
        weather.setTempMin(main.get("temp_min").getAsDouble());
        weather.setTempMax(main.get("temp_max").getAsDouble());
        weather.setHumidity(main.get("humidity").getAsInt());
        weather.setPressure(main.get("pressure").getAsInt());
        weather.setTemperatureUnit(units);
    }
    
    /**
     * Parse weather condition from JSON
     */
    private void parseWeatherCondition(JsonObject json, Weather weather) {
        JsonObject weatherObj = json.getAsJsonArray("weather").get(0).getAsJsonObject();
        weather.setCondition(weatherObj.get("main").getAsString());
        weather.setDescription(weatherObj.get("description").getAsString());
        weather.setIconCode(weatherObj.get("id").getAsString());
    }
    
    /**
     * Parse wind data from JSON
     */
    private void parseWindData(JsonObject json, Weather weather) {
        JsonObject wind = json.getAsJsonObject("wind");
        weather.setWindSpeed(wind.get("speed").getAsDouble());
        
        if (wind.has("deg")) {
            weather.setWindDirection(wind.get("deg").getAsInt());
        }
    }
    
    /**
     * Parse additional weather data from JSON
     */
    private void parseAdditionalData(JsonObject json, Weather weather) {
        // Parse clouds
        if (json.has("clouds")) {
            JsonObject clouds = json.getAsJsonObject("clouds");
            weather.setCloudiness(clouds.get("all").getAsInt());
        }
        
        // Parse timestamp
        weather.setTimestamp(json.get("dt").getAsLong());
    }
}

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
 * WeatherService - Fetches current weather data from OpenWeather.
 *
 * Design notes (POC):
 * - Uses the Java 11+ HttpClient
 * - Returns null on failure for simple UI handling
 * - Parsing is intentionally direct (no heavy abstraction)
 */
public class WeatherService {

    private final HttpClient httpClient;

    public WeatherService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Fetch current weather asynchronously.
     * Contract: completes with a Weather object, or null if an error occurs.
     */
    public CompletableFuture<Weather> getCurrentWeatherAsync(String cityName) {
        String url = buildWeatherApiUrl(cityName);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                try {
                    validateApiResponse(response);
                    String units = resolveUnits();
                    return parseWeatherResponse(response.body(), units);
                } catch (Exception ex) {
                    System.err.println("Error fetching weather: " + ex.getMessage());
                    return null;
                }
            })
            .exceptionally(ex -> {
                System.err.println("Error fetching weather: " + ex.getMessage());
                return null;
            });
    }

    /**
     * Synchronous variant (kept for completeness / testing).
     */
    public Weather getCurrentWeather(String cityName) throws Exception {
        String url = buildWeatherApiUrl(cityName);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateApiResponse(response);

        String units = resolveUnits();
        return parseWeatherResponse(response.body(), units);
    }

    public boolean isApiKeyConfigured() {
        return AppConfig.WEATHER_API_KEY != null
            && !AppConfig.WEATHER_API_KEY.isBlank()
            && !AppConfig.WEATHER_API_KEY.equals("YOUR_API_KEY_HERE");
    }

    private String resolveUnits() {
        return "imperial".equalsIgnoreCase(AppConfig.TEMPERATURE_UNIT) ? "imperial" : "metric";
    }

    private String buildWeatherApiUrl(String cityName) {
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String units = resolveUnits();

        return String.format("%s/weather?q=%s&units=%s&appid=%s",
            AppConfig.WEATHER_API_BASE_URL,
            encodedCity,
            units,
            AppConfig.WEATHER_API_KEY
        );
    }

    private void validateApiResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() != 200) {
            throw new Exception("API returned status code: " + response.statusCode());
        }
    }

    private Weather parseWeatherResponse(String jsonResponse, String units) {
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        Weather weather = new Weather();

        parseLocationData(json, weather);
        parseMainWeatherData(json, weather, units);
        parseWeatherCondition(json, weather);
        parseWindData(json, weather);
        parseAdditionalData(json, weather);

        return weather;
    }

    private void parseLocationData(JsonObject json, Weather weather) {
        weather.setCityName(json.get("name").getAsString());
        JsonObject sys = json.getAsJsonObject("sys");
        weather.setCountry(sys.get("country").getAsString());
        if (sys.has("sunrise")) {
            weather.setSunriseTimestamp(sys.get("sunrise").getAsLong());
        }
        if (sys.has("sunset")) {
            weather.setSunsetTimestamp(sys.get("sunset").getAsLong());
        }
    }

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

    private void parseWeatherCondition(JsonObject json, Weather weather) {
        JsonObject weatherObj = json.getAsJsonArray("weather").get(0).getAsJsonObject();
        weather.setCondition(weatherObj.get("main").getAsString());
        weather.setDescription(weatherObj.get("description").getAsString());
        // Note: current code stores "id" as iconCode; kept for UI compatibility.
        weather.setIconCode(weatherObj.get("id").getAsString());
    }

    private void parseWindData(JsonObject json, Weather weather) {
        JsonObject wind = json.getAsJsonObject("wind");
        weather.setWindSpeed(wind.get("speed").getAsDouble());
        if (wind.has("deg")) {
            weather.setWindDirection(wind.get("deg").getAsInt());
        }
    }

    private void parseAdditionalData(JsonObject json, Weather weather) {
        if (json.has("clouds")) {
            JsonObject clouds = json.getAsJsonObject("clouds");
            weather.setCloudiness(clouds.get("all").getAsInt());
        }
        if (json.has("visibility")) {
            weather.setVisibility(json.get("visibility").getAsInt());
        }
        if (json.has("uvi")) {
            weather.setUvIndex(json.get("uvi").getAsInt());
        }
        weather.setTimestamp(json.get("dt").getAsLong());
    }
}

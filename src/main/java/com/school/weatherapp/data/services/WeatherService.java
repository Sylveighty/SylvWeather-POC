package com.school.weatherapp.data.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.data.cache.CacheService;
import com.school.weatherapp.data.models.Weather;
import com.school.weatherapp.features.UserPreferencesService;

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
    private final CacheService cacheService;
    private final UserPreferencesService preferencesService;

    public WeatherService() {
        this.httpClient = HttpClient.newHttpClient();
        this.cacheService = new CacheService();
        this.preferencesService = new UserPreferencesService();
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
            .thenCompose(response -> {
                try {
                    validateApiResponse(response);
                    String units = resolveUnits();
                    Weather weather = parseWeatherResponse(response.body(), units);
                    return enrichWeatherWithUvIndexAsync(weather, units)
                        .thenApply(enriched -> {
                            if (enriched != null) {
                                enriched.setCached(false);
                                cacheService.saveWeather(cityName, enriched);
                            }
                            return enriched;
                        });
                } catch (Exception ex) {
                    System.err.println("Error fetching weather: " + ex.getMessage());
                    return CompletableFuture.completedFuture(loadCachedWeather());
                }
            })
            .exceptionally(ex -> {
                System.err.println("Error fetching weather: " + ex.getMessage());
                return loadCachedWeather();
            });
    }

    /**
     * Synchronous variant (kept for completeness / testing).
     */
    public Weather getCurrentWeather(String cityName) throws Exception {
        try {
            String url = buildWeatherApiUrl(cityName);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            validateApiResponse(response);

            String units = resolveUnits();
            Weather weather = parseWeatherResponse(response.body(), units);
            Weather enriched = enrichWeatherWithUvIndex(weather, units);
            if (enriched != null) {
                enriched.setCached(false);
                cacheService.saveWeather(cityName, enriched);
            }
            return enriched;
        } catch (Exception ex) {
            Weather cached = loadCachedWeather();
            if (cached != null) {
                return cached;
            }
            throw ex;
        }
    }

    public boolean isApiKeyConfigured() {
        return AppConfig.WEATHER_API_KEY != null
            && !AppConfig.WEATHER_API_KEY.isBlank()
            && !AppConfig.WEATHER_API_KEY.equals("YOUR_API_KEY_HERE");
    }

    private String resolveUnits() {
        return "imperial".equalsIgnoreCase(preferencesService.getTemperatureUnit()) ? "imperial" : "metric";
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
        if (json.has("coord")) {
            JsonObject coord = json.getAsJsonObject("coord");
            if (coord.has("lat")) {
                weather.setLatitude(coord.get("lat").getAsDouble());
            }
            if (coord.has("lon")) {
                weather.setLongitude(coord.get("lon").getAsDouble());
            }
        }
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

    private CompletableFuture<Weather> enrichWeatherWithUvIndexAsync(Weather weather, String units) {
        if (weather == null || Double.isNaN(weather.getLatitude()) || Double.isNaN(weather.getLongitude())) {
            return CompletableFuture.completedFuture(weather);
        }

        return fetchUvIndexAsync(weather.getLatitude(), weather.getLongitude(), units)
            .thenApply(uvIndex -> {
                if (uvIndex >= 0) {
                    weather.setUvIndex(uvIndex);
                }
                return weather;
            })
            .exceptionally(ex -> {
                System.err.println("Error fetching UV index: " + ex.getMessage());
                return weather;
            });
    }

    private Weather enrichWeatherWithUvIndex(Weather weather, String units) {
        if (weather == null || Double.isNaN(weather.getLatitude()) || Double.isNaN(weather.getLongitude())) {
            return weather;
        }

        try {
            int uvIndex = fetchUvIndex(weather.getLatitude(), weather.getLongitude(), units);
            if (uvIndex >= 0) {
                weather.setUvIndex(uvIndex);
            }
        } catch (Exception ex) {
            System.err.println("Error fetching UV index: " + ex.getMessage());
        }

        return weather;
    }

    private CompletableFuture<Integer> fetchUvIndexAsync(double lat, double lon, String units) {
        String url = buildUvIndexApiUrl(lat, lon, units);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                try {
                    validateApiResponse(response);
                    return parseUvIndex(response.body());
                } catch (Exception ex) {
                    System.err.println("Error fetching UV index: " + ex.getMessage());
                    return -1;
                }
            });
    }

    private int fetchUvIndex(double lat, double lon, String units) throws Exception {
        String url = buildUvIndexApiUrl(lat, lon, units);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateApiResponse(response);
        return parseUvIndex(response.body());
    }

    private String buildUvIndexApiUrl(double lat, double lon, String units) {
        return String.format("%s/onecall?lat=%s&lon=%s&units=%s&exclude=minutely,hourly,daily,alerts&appid=%s",
            AppConfig.WEATHER_API_BASE_URL,
            lat,
            lon,
            units,
            AppConfig.WEATHER_API_KEY
        );
    }

    private int parseUvIndex(String jsonResponse) {
        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        if (json.has("current")) {
            JsonObject current = json.getAsJsonObject("current");
            if (current.has("uvi")) {
                return (int) Math.round(current.get("uvi").getAsDouble());
            }
        }
        return -1;
    }

    private Weather loadCachedWeather() {
        return cacheService.loadWeather();
    }
}

package com.school.weatherapp.data.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.data.models.Forecast;
import com.school.weatherapp.data.models.Weather;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * CacheService - Simple file-backed cache for offline fallback.
 */
public class CacheService {

    private final Gson gson;

    public CacheService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void saveWeather(String cityName, Weather weather) {
        if (weather == null) {
            return;
        }
        WeatherCacheEntry entry = new WeatherCacheEntry(cityName, Instant.now().getEpochSecond(), weather);
        writeCache(Path.of(AppConfig.WEATHER_CACHE_FILE_PATH), entry);
    }

    public Weather loadWeather() {
        WeatherCacheEntry entry = readCache(Path.of(AppConfig.WEATHER_CACHE_FILE_PATH), WeatherCacheEntry.class);
        if (entry == null || entry.weather == null) {
            return null;
        }
        entry.weather.setCached(true);
        return entry.weather;
    }

    public void saveForecast(String cityName, List<Forecast> forecasts) {
        if (forecasts == null || forecasts.isEmpty()) {
            return;
        }
        ForecastCacheEntry entry = new ForecastCacheEntry(cityName, Instant.now().getEpochSecond(), forecasts);
        writeCache(Path.of(AppConfig.FORECAST_CACHE_FILE_PATH), entry);
    }

    public List<Forecast> loadForecast() {
        ForecastCacheEntry entry = readCache(Path.of(AppConfig.FORECAST_CACHE_FILE_PATH), ForecastCacheEntry.class);
        if (entry == null || entry.forecasts == null) {
            return new ArrayList<>();
        }
        entry.forecasts.forEach(forecast -> forecast.setCached(true));
        return entry.forecasts;
    }

    public void saveDailyForecast(String cityName, List<Forecast> forecasts) {
        if (forecasts == null || forecasts.isEmpty()) {
            return;
        }
        ForecastCacheEntry entry = new ForecastCacheEntry(cityName, Instant.now().getEpochSecond(), forecasts);
        writeCache(Path.of(AppConfig.DAILY_FORECAST_CACHE_FILE_PATH), entry);
    }

    public List<Forecast> loadDailyForecast() {
        ForecastCacheEntry entry = readCache(Path.of(AppConfig.DAILY_FORECAST_CACHE_FILE_PATH), ForecastCacheEntry.class);
        if (entry == null || entry.forecasts == null) {
            return new ArrayList<>();
        }
        entry.forecasts.forEach(forecast -> forecast.setCached(true));
        return entry.forecasts;
    }

    private void writeCache(Path path, Object payload) {
        try {
            Files.writeString(path, gson.toJson(payload));
        } catch (IOException ex) {
            System.err.println("Error saving cache: " + ex.getMessage());
        }
    }

    private <T> T readCache(Path path, Class<T> type) {
        if (!Files.exists(path)) {
            return null;
        }
        try {
            String json = Files.readString(path);
            if (json == null || json.isBlank()) {
                return null;
            }
            return gson.fromJson(json, type);
        } catch (IOException ex) {
            System.err.println("Error reading cache: " + ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static class WeatherCacheEntry {
        private final String cityName;
        private final long cachedAt;
        private final Weather weather;

        private WeatherCacheEntry(String cityName, long cachedAt, Weather weather) {
            this.cityName = cityName;
            this.cachedAt = cachedAt;
            this.weather = weather;
        }
    }

    @SuppressWarnings("unused")
    private static class ForecastCacheEntry {
        private final String cityName;
        private final long cachedAt;
        private final List<Forecast> forecasts;

        private ForecastCacheEntry(String cityName, long cachedAt, List<Forecast> forecasts) {
            this.cityName = cityName;
            this.cachedAt = cachedAt;
            this.forecasts = forecasts;
        }
    }
}

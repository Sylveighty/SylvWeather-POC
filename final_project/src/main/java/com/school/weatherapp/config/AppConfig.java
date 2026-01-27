package com.school.weatherapp.config;

/**
 * AppConfig - Centralized configuration for SylvWeather-POC.
 *
 * POC note:
 * - Configuration is intentionally simple.
 * - Secrets (API keys) must not be committed to version control.
 */
public final class AppConfig {

    // ==================== API Configuration ====================

    /** OpenWeather API base URL (v2.5 endpoints used for current + forecast). */
    public static final String WEATHER_API_BASE_URL = "https://api.openweathermap.org/data/2.5";

    /**
     * OpenWeather API key.
     * Read from environment variable OPENWEATHER_API_KEY.
     *
     * Do NOT commit real keys.
     */
    public static final String WEATHER_API_KEY =
        System.getenv("OPENWEATHER_API_KEY") != null
            ? System.getenv("OPENWEATHER_API_KEY").trim()
            : "";

    /** Default city displayed on app launch. */
    public static final String DEFAULT_CITY = "New York";

    // ==================== UI Configuration ====================

    /** Temperature unit preference ("metric" or "imperial"). */
    public static final String TEMPERATURE_UNIT = "imperial";

    /** Path to favorites file for storing favorite cities. */
    public static final String FAVORITES_FILE_PATH = "favorites.txt";

    /** Path to cached weather data for offline fallback. */
    public static final String WEATHER_CACHE_FILE_PATH = "cache-weather.json";

    /** Path to cached hourly forecast data for offline fallback. */
    public static final String FORECAST_CACHE_FILE_PATH = "cache-forecast.json";

    /** Path to cached daily forecast data for offline fallback. */
    public static final String DAILY_FORECAST_CACHE_FILE_PATH = "cache-daily-forecast.json";

    /** Maximum number of recent searches to retain. */
    public static final int RECENT_SEARCH_LIMIT = 6;

    private AppConfig() {
        throw new UnsupportedOperationException("AppConfig is a utility class and cannot be instantiated");
    }
}

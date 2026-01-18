package com.school.weatherapp.config;

/**
 * AppConfig - Centralized configuration for the Weather Application
 * 
 * This class holds all configuration constants including:
 * - API endpoints and keys
 * - Refresh intervals
 * - UI settings
 * - Feature toggles
 * 
 * All values are static final for easy access throughout the application.
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 0)
 */
public class AppConfig {
    
    // ============ API Configuration ============
    
    /**
     * OpenWeatherMap API base URL
     * Documentation: https://openweathermap.org/api
     */
    public static final String WEATHER_API_BASE_URL = "https://api.openweathermap.org/data/2.5";
    
    /**
     * API Key for OpenWeatherMap
     * DONE: Replace with your actual API key from https://openweathermap.org/api
     */
    public static final String WEATHER_API_KEY = "f55978d8ae2181360e45c253d1e13d60";
    
    /**
     * Default city to display on app launch
     */
    public static final String DEFAULT_CITY = "New York";
    
    
    // ============ Refresh Intervals ============
    
    /**
     * How often to refresh current weather data (in seconds)
     * Default: 10 minutes
     */
    public static final int WEATHER_REFRESH_INTERVAL = 600;
    
    /**
     * How often to check for weather alerts (in seconds)
     * Default: 15 minutes
     */
    public static final int ALERTS_REFRESH_INTERVAL = 900;
    
    
    // ============ UI Configuration ============
    
    /**
     * Default theme on startup (light or dark)
     */
    public static final String DEFAULT_THEME = "light";
    
    /**
     * Enable smooth animations for data updates
     */
    public static final boolean ENABLE_ANIMATIONS = true;
    
    /**
     * Temperature unit preference (metric or imperial)
     */
    public static final String TEMPERATURE_UNIT = "imperial"; // Fahrenheit
    
    
    // ============ Feature Toggles ============
    
    /**
     * Enable real-time weather alerts feature
     */
    public static final boolean ENABLE_ALERTS = true;
    
    /**
     * Enable GPS location detection
     */
    public static final boolean ENABLE_GPS = false; // Disabled for POC
    
    /**
     * Use simulated data instead of live API calls
     * Useful for development/testing without API key
     */
    public static final boolean USE_SIMULATED_DATA = false;
    
    
    // ============ Private Constructor ============
    
    /**
     * Private constructor to prevent instantiation
     * This is a utility class with only static members
     */
    private AppConfig() {
        throw new UnsupportedOperationException("AppConfig is a utility class and cannot be instantiated");
    }
}
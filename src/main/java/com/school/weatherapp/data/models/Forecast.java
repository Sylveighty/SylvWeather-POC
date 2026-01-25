package com.school.weatherapp.data.models;

/**
 * Forecast - Data model representing a single forecast entry
 * 
 * Can represent either:
 * - A daily forecast (for 7-day view)
 * - An hourly forecast (for hourly view)
 * 
 * Contains temperature, weather condition, and timing information.
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 2)
 */
public class Forecast {
    
    // ==================== Timing Information ====================
    private long timestamp;           // Unix timestamp.
    private String dayOfWeek;         // e.g., "Monday", "Tuesday".
    private String timeLabel;         // e.g., "3:00 PM" for hourly, "Mon" for daily.
    
    // ==================== Temperature Data ====================
    private double temperature;       // Main temperature.
    private double tempMin;           // Minimum temperature (daily).
    private double tempMax;           // Maximum temperature (daily).
    private double feelsLike;         // Feels like temperature.
    
    /** Track the unit system this forecast data was fetched with ("imperial" or "metric") */
    private String temperatureUnit;
    
    // ==================== Weather Conditions ====================
    private String condition;         // e.g., "Clear", "Rain".
    private String description;       // e.g., "clear sky".
    private String iconCode;          // Weather icon code.
    
    // ==================== Additional Data ====================
    private int humidity;             // Percentage.
    private double windSpeed;         // mph or m/s.
    private int precipitation;        // Percentage chance (0-100).
    private double rainAmount;        // mm.
    private double snowAmount;        // mm.

    // ==================== Cache State ====================
    private boolean cached;
    
    // ==================== Constructors ====================
    
    /**
     * Default constructor
     */
    public Forecast() {
    }
    
    // ==================== Getters and Setters ====================
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public String getTimeLabel() {
        return timeLabel;
    }
    
    public void setTimeLabel(String timeLabel) {
        this.timeLabel = timeLabel;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public String getTemperatureUnit() {
        return temperatureUnit;
    }
    
    public void setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }
    
    public double getTempMin() {
        return tempMin;
    }
    
    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }
    
    public double getTempMax() {
        return tempMax;
    }
    
    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }
    
    public double getFeelsLike() {
        return feelsLike;
    }
    
    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIconCode() {
        return iconCode;
    }
    
    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }
    
    public int getHumidity() {
        return humidity;
    }
    
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }
    
    public double getWindSpeed() {
        return windSpeed;
    }
    
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
    
    public int getPrecipitation() {
        return precipitation;
    }
    
    public void setPrecipitation(int precipitation) {
        this.precipitation = precipitation;
    }

    public double getRainAmount() {
        return rainAmount;
    }

    public void setRainAmount(double rainAmount) {
        this.rainAmount = rainAmount;
    }

    public double getSnowAmount() {
        return snowAmount;
    }

    public void setSnowAmount(double snowAmount) {
        this.snowAmount = snowAmount;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }
    
    // ==================== Utility Methods ====================
    
    @Override
    public String toString() {
        return String.format("%s: %.1f°F - %s", 
            timeLabel, temperature, condition);
    }
}

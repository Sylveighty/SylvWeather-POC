package com.school.weatherapp.data.models;

/**
 * Weather - Data model representing current weather conditions
 * 
 * This class holds all weather information returned from the API:
 * - Temperature (current, feels like, min, max)
 * - Atmospheric conditions (humidity, pressure)
 * - Wind information
 * - Weather description and icon
 * - Location details
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 1)
 */
public class Weather {
    
    // ==================== Location Information ====================
    private String cityName;
    private String country;
    private double latitude;
    private double longitude;
    
    // ==================== Temperature Data ====================
    private double temperature;
    private double feelsLike;
    private double tempMin;
    private double tempMax;
    
    /** Track the unit system this weather data was fetched with ("imperial" or "metric") */
    private String temperatureUnit;
    
    // ==================== Weather Conditions ====================
    private String condition;        // e.g., "Clear", "Clouds", "Rain".
    private String description;      // e.g., "clear sky", "light rain".
    private String iconCode;         // OpenWeatherMap icon code.
    
    // ==================== Atmospheric Data ====================
    private int humidity;            // Percentage.
    private int pressure;            // hPa.
    private int cloudiness;          // Percentage.
    
    // ==================== Wind Data ====================
    private double windSpeed;        // mph or m/s.
    private int windDirection;       // Degrees.
    
    // ==================== Additional Data ====================
    private int uvIndex = -1;
    private int visibility = -1;     // meters.
    private long sunriseTimestamp;   // Unix timestamp.
    private long sunsetTimestamp;    // Unix timestamp.
    private double precipitation;    // mm.
    private long timestamp;          // Unix timestamp.
    
    // ==================== Constructors ====================
    
    /**
     * Default constructor - initializes timestamp to current time
     */
    public Weather() {
        this.timestamp = System.currentTimeMillis() / 1000;
        this.latitude = Double.NaN;
        this.longitude = Double.NaN;
    }
    
    // ==================== Getters and Setters ====================
    
    public String getCityName() {
        return cityName;
    }
    
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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
    
    public double getFeelsLike() {
        return feelsLike;
    }
    
    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
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
    
    public int getPressure() {
        return pressure;
    }
    
    public void setPressure(int pressure) {
        this.pressure = pressure;
    }
    
    public int getCloudiness() {
        return cloudiness;
    }
    
    public void setCloudiness(int cloudiness) {
        this.cloudiness = cloudiness;
    }
    
    public double getWindSpeed() {
        return windSpeed;
    }
    
    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }
    
    public int getWindDirection() {
        return windDirection;
    }
    
    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }
    
    public int getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(int uvIndex) {
        this.uvIndex = uvIndex;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public long getSunriseTimestamp() {
        return sunriseTimestamp;
    }

    public void setSunriseTimestamp(long sunriseTimestamp) {
        this.sunriseTimestamp = sunriseTimestamp;
    }

    public long getSunsetTimestamp() {
        return sunsetTimestamp;
    }

    public void setSunsetTimestamp(long sunsetTimestamp) {
        this.sunsetTimestamp = sunsetTimestamp;
    }
    
    public double getPrecipitation() {
        return precipitation;
    }
    
    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Get full location string (City, Country)
     * 
     * @return Formatted location string
     */
    public String getFullLocation() {
        return cityName + ", " + country;
    }
    
    /**
     * Get wind direction as compass direction (N, NE, E, etc.)
     * 
     * @return Compass direction string
     */
    public String getWindDirectionCompass() {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.round(((windDirection % 360) / 45.0)) % 8;
        return directions[index];
    }
    
    @Override
    public String toString() {
        return String.format("%s, %s: %.1f°F - %s", 
            cityName, country, temperature, description);
    }
}

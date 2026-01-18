package com.school.weatherapp.ui.panels;

import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.data.models.Forecast;
import com.school.weatherapp.data.services.ForecastService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

/**
 * HourlyForecastPanel - UI panel displaying hourly weather forecast
 * 
 * Shows a horizontal row of hourly forecast cards with:
 * - Time
 * - Weather icon
 * - Temperature
 * - Precipitation chance
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 2)
 */
public class HourlyForecastPanel extends VBox {
    
    private final ForecastService forecastService;
    private HBox forecastCardsContainer;
    private ProgressIndicator loadingIndicator;
    private Label titleLabel;
    
    /**
     * Constructor - builds the UI panel
     */
    public HourlyForecastPanel() {
        this.forecastService = new ForecastService();
        
        // Panel styling
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        
        // Build UI
        buildTitle();
        buildForecastContainer();
    }
    
    /**
     * Build title section
     */
    private void buildTitle() {
        titleLabel = new Label("Hourly Forecast");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #333;");
        
        this.getChildren().add(titleLabel);
    }
    
    /**
     * Build container for forecast cards
     */
    private void buildForecastContainer() {
        // Container for cards
        forecastCardsContainer = new HBox(12);
        forecastCardsContainer.setAlignment(Pos.CENTER_LEFT);
        forecastCardsContainer.setStyle("-fx-background-color: white; " +
                                        "-fx-background-radius: 8; " +
                                        "-fx-padding: 20;");
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(120);
        
        forecastCardsContainer.getChildren().add(loadingBox);
        
        this.getChildren().add(forecastCardsContainer);
    }
    
    /**
     * Load hourly forecast for a city
     * 
     * @param cityName Name of the city
     */
    public void loadHourlyForecast(String cityName) {
        // Show loading
        Platform.runLater(() -> {
            forecastCardsContainer.getChildren().clear();
            VBox loadingBox = new VBox(loadingIndicator);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPrefHeight(120);
            forecastCardsContainer.getChildren().add(loadingBox);
        });
        
        // Fetch forecast data
        forecastService.getHourlyForecastAsync(cityName)
            .thenAccept(forecasts -> {
                Platform.runLater(() -> {
                    forecastCardsContainer.getChildren().clear();
                    
                    if (forecasts != null && !forecasts.isEmpty()) {
                        displayForecasts(forecasts);
                    } else {
                        showError();
                    }
                });
            });
    }
    
    /**
     * Display forecast cards
     * 
     * @param forecasts List of hourly forecasts
     */
    private void displayForecasts(List<Forecast> forecasts) {
        for (Forecast forecast : forecasts) {
            VBox card = createForecastCard(forecast);
            forecastCardsContainer.getChildren().add(card);
        }
    }
    
    /**
     * Create a single forecast card
     * 
     * @param forecast Forecast data
     * @return VBox containing the card UI
     */
    private VBox createForecastCard(Forecast forecast) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setStyle("-fx-background-color: #fafafa; " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 8; " +
                     "-fx-border-width: 1;");
        card.setPrefWidth(90);
        
        // Time
        Label timeLabel = new Label(forecast.getTimeLabel());
        timeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        timeLabel.setStyle("-fx-text-fill: #333;");
        
        // Weather icon
        Text icon = new Text(getWeatherEmoji(forecast.getCondition()));
        icon.setStyle("-fx-font-size: 32px;");
        
        // Temperature
        String tempUnit = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "°F" : "°C";
        Label tempLabel = new Label(String.format("%.0f%s", forecast.getTemperature(), tempUnit));
        tempLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        tempLabel.setStyle("-fx-text-fill: #333;");
        
        // Precipitation chance
        if (forecast.getPrecipitation() > 0) {
            Label precipLabel = new Label(forecast.getPrecipitation() + "%");
            precipLabel.setFont(Font.font("System", 11));
            precipLabel.setStyle("-fx-text-fill: #1976d2;");
            card.getChildren().addAll(timeLabel, icon, tempLabel, precipLabel);
        } else {
            card.getChildren().addAll(timeLabel, icon, tempLabel);
        }
        
        return card;
    }
    
    /**
     * Show error message
     */
    private void showError() {
        Label errorLabel = new Label("Could not load forecast data");
        errorLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
        forecastCardsContainer.getChildren().add(errorLabel);
    }
    
    /**
     * Get emoji representation of weather condition
     */
    private String getWeatherEmoji(String condition) {
        switch (condition.toLowerCase()) {
            case "clear": return "☀️";
            case "clouds": return "☁️";
            case "rain": return "🌧️";
            case "drizzle": return "🌦️";
            case "thunderstorm": return "⛈️";
            case "snow": return "❄️";
            case "mist":
            case "fog": return "🌫️";
            default: return "🌤️";
        }
    }
}
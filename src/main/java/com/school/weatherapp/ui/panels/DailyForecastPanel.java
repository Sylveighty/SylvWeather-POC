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
 * DailyForecastPanel - UI panel displaying 7-day weather forecast
 * 
 * Shows a horizontal row of daily forecast cards with:
 * - Day of week
 * - Weather icon
 * - High/low temperatures
 * - Weather condition
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 2)
 */
public class DailyForecastPanel extends VBox {
    
    private final ForecastService forecastService;
    private HBox forecastCardsContainer;
    private ProgressIndicator loadingIndicator;
    private Label titleLabel;
    
    /**
     * Constructor - builds the UI panel
     */
    public DailyForecastPanel() {
        this.forecastService = new ForecastService();
        
        // Panel styling
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.applyLightTheme();
        
        // Build UI
        buildTitle();
        buildForecastContainer();
    }
    
    public void applyLightTheme() {
        this.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
    }
    
    public void applyDarkTheme() {
        this.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10;");
    }
    
    /**
     * Build title section
     */
    private void buildTitle() {
        titleLabel = new Label("7-Day Forecast");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #333;");
        
        this.getChildren().add(titleLabel);
    }
    
    /**
     * Build container for forecast cards
     */
    private void buildForecastContainer() {
        // Container for cards
        forecastCardsContainer = new HBox(15);
        forecastCardsContainer.setAlignment(Pos.CENTER);
        forecastCardsContainer.setStyle("-fx-background-color: white; " +
                                        "-fx-background-radius: 8; " +
                                        "-fx-padding: 20;");
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(150);
        
        forecastCardsContainer.getChildren().add(loadingBox);
        
        this.getChildren().add(forecastCardsContainer);
    }
    
    /**
     * Load daily forecast for a city
     * 
     * @param cityName Name of the city
     */
    public void loadDailyForecast(String cityName) {
        // Show loading
        Platform.runLater(() -> {
            forecastCardsContainer.getChildren().clear();
            VBox loadingBox = new VBox(loadingIndicator);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPrefHeight(150);
            forecastCardsContainer.getChildren().add(loadingBox);
        });
        
        // Fetch forecast data
        forecastService.getDailyForecastAsync(cityName)
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
     * @param forecasts List of daily forecasts
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
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setStyle("-fx-background-color: #fafafa; " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 8; " +
                     "-fx-border-width: 1;");
        card.setPrefWidth(110);
        
        // Day of week
        Label dayLabel = new Label(forecast.getTimeLabel());
        dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        dayLabel.setStyle("-fx-text-fill: #333;");
        
        // Weather icon
        Text icon = new Text(getWeatherEmoji(forecast.getCondition()));
        icon.setStyle("-fx-font-size: 36px; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif;");
        
        // High/Low temps
        String tempUnit = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "°F" : "°C";
        Label highLabel = new Label(String.format("%.0f%s", forecast.getTempMax(), tempUnit));
        highLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        highLabel.setStyle("-fx-text-fill: #d32f2f;");
        
        Label lowLabel = new Label(String.format("%.0f%s", forecast.getTempMin(), tempUnit));
        lowLabel.setFont(Font.font("System", 14));
        lowLabel.setStyle("-fx-text-fill: #1976d2;");
        
        // Condition
        Label conditionLabel = new Label(forecast.getCondition());
        conditionLabel.setFont(Font.font("System", 11));
        conditionLabel.setStyle("-fx-text-fill: #666;");
        conditionLabel.setWrapText(true);
        conditionLabel.setMaxWidth(100);
        conditionLabel.setAlignment(Pos.CENTER);
        
        card.getChildren().addAll(dayLabel, icon, highLabel, lowLabel, conditionLabel);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #f0f0f0; " +
                         "-fx-background-radius: 8; " +
                         "-fx-border-color: #d0d0d0; " +
                         "-fx-border-radius: 8; " +
                         "-fx-border-width: 1; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 8, 0, 0, 2);");
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #fafafa; " +
                         "-fx-background-radius: 8; " +
                         "-fx-border-color: #e0e0e0; " +
                         "-fx-border-radius: 8; " +
                         "-fx-border-width: 1;");
        });
        
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
            case "clear": return "☀";
            case "clouds": return "☁";
            case "rain": return "⛈";
            case "drizzle": return "☔";
            case "thunderstorm": return "⚡";
            case "snow": return "❄";
            case "mist":
            case "fog": return "≈";
            default: return "◐";
        }
    }
}
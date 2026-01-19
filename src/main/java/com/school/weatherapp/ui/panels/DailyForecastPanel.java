package com.school.weatherapp.ui.panels;

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
    private List<Forecast> currentForecasts;
    
    /**
     * Constructor - builds the UI panel
     */
    public DailyForecastPanel() {
        this.forecastService = new ForecastService();
        
        // Panel basic layout
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        
        // Build UI components FIRST
        buildTitle();
        buildForecastContainer();
        
        // Apply theme LAST (now that all UI elements exist)
        applyLightTheme();   // or applyDarkTheme() if you prefer dark by default
    }
    
    public void applyLightTheme() {
        this.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

        if (titleLabel != null) {
            titleLabel.setStyle("-fx-text-fill: #333;");
        }

        if (forecastCardsContainer != null) {
            forecastCardsContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 20;");
        }

        updateForecastCardsTheme("#333", "#fafafa", "#f0f0f0", "#e0e0e0");
    }

    public void applyDarkTheme() {
        this.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10;");

        if (titleLabel != null) {
            titleLabel.setStyle("-fx-text-fill: #e0e0e0;");
        }

        if (forecastCardsContainer != null) {
            forecastCardsContainer.setStyle("-fx-background-color: #333333; -fx-background-radius: 8; -fx-padding: 20;");
        }

        updateForecastCardsTheme("#e0e0e0", "#3a3a3a", "#444444", "#555555");
    }

    /**
     * Helper method to update forecast cards theme
     */
    private void updateForecastCardsTheme(String textColor, String cardBg, String hoverBg, String borderColor) {
        if (forecastCardsContainer == null) {
            return; // prevent NPE if called too early (defensive)
        }

        for (javafx.scene.Node node : forecastCardsContainer.getChildren()) {
            if (node instanceof VBox card) {
                // Update card background and border
                card.setStyle("-fx-background-color: " + cardBg + "; " +
                              "-fx-background-radius: 8; " +
                              "-fx-border-color: " + borderColor + "; " +
                              "-fx-border-radius: 8; " +
                              "-fx-border-width: 1;");

                // Update text colors in labels
                for (javafx.scene.Node child : card.getChildren()) {
                    if (child instanceof Label label) {
                        label.setStyle("-fx-text-fill: " + textColor + ";");
                    }
                }

                // Update hover effects (re-apply listeners)
                card.setOnMouseEntered(e -> card.setStyle(
                    "-fx-background-color: " + hoverBg + "; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-color: " + borderColor + "; " +
                    "-fx-border-radius: 8; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 8, 0, 0, 2);"
                ));

                card.setOnMouseExited(e -> card.setStyle(
                    "-fx-background-color: " + cardBg + "; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-color: " + borderColor + "; " +
                    "-fx-border-radius: 8; " +
                    "-fx-border-width: 1;"
                ));
            }
        }
    }
    
    private void buildTitle() {
        titleLabel = new Label("7-Day Forecast");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #333;");
        
        this.getChildren().add(titleLabel);
    }
    
    private void buildForecastContainer() {
        forecastCardsContainer = new HBox(15);
        forecastCardsContainer.setAlignment(Pos.CENTER);
        forecastCardsContainer.setStyle("-fx-background-color: white; " +
                                       "-fx-background-radius: 8; " +
                                       "-fx-padding: 20;");
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(150);
        
        forecastCardsContainer.getChildren().add(loadingBox);
        
        this.getChildren().add(forecastCardsContainer);
    }
    
    public void loadDailyForecast(String cityName) {
        Platform.runLater(() -> {
            forecastCardsContainer.getChildren().clear();
            VBox loadingBox = new VBox(loadingIndicator);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPrefHeight(150);
            forecastCardsContainer.getChildren().add(loadingBox);
        });
        
        forecastService.getDailyForecastAsync(cityName)
            .thenAccept(forecasts -> Platform.runLater(() -> {
                forecastCardsContainer.getChildren().clear();
                
                if (forecasts != null && !forecasts.isEmpty()) {
                    displayForecasts(forecasts);
                } else {
                    showError();
                }
            }));
    }
    
    private void displayForecasts(List<Forecast> forecasts) {
        currentForecasts = forecasts;
        refreshForecastCards(false); // Default to false (Celsius) initially
    }

    private void refreshForecastCards(boolean isImperial) {
        if (currentForecasts == null) return;

        forecastCardsContainer.getChildren().clear();
        for (Forecast forecast : currentForecasts) {
            VBox card = createForecastCard(forecast, isImperial);
            forecastCardsContainer.getChildren().add(card);
        }
    }
    
    private VBox createForecastCard(Forecast forecast, boolean isImperial) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setStyle("-fx-background-color: #fafafa; " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 8; " +
                     "-fx-border-width: 1;");
        card.setPrefWidth(110);

        Label dayLabel = new Label(forecast.getTimeLabel());
        dayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        dayLabel.setStyle("-fx-text-fill: #333;");

        Text icon = new Text(getWeatherEmoji(forecast.getCondition()));
        icon.setStyle("-fx-font-size: 36px; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif;");

        // Convert temperatures based on unit preference
        double highTemp = isImperial ?
            com.school.weatherapp.util.TemperatureUtil.celsiusToFahrenheit(forecast.getTempMax()) :
            forecast.getTempMax();
        double lowTemp = isImperial ?
            com.school.weatherapp.util.TemperatureUtil.celsiusToFahrenheit(forecast.getTempMin()) :
            forecast.getTempMin();
        String tempUnit = isImperial ? "°F" : "°C";

        Label highLabel = new Label(String.format("%.0f%s", highTemp, tempUnit));
        highLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        highLabel.setStyle("-fx-text-fill: #d32f2f;");

        Label lowLabel = new Label(String.format("%.0f%s", lowTemp, tempUnit));
        lowLabel.setFont(Font.font("System", 14));
        lowLabel.setStyle("-fx-text-fill: #1976d2;");

        Label conditionLabel = new Label(forecast.getCondition());
        conditionLabel.setFont(Font.font("System", 11));
        conditionLabel.setStyle("-fx-text-fill: #666;");
        conditionLabel.setWrapText(true);
        conditionLabel.setMaxWidth(100);
        conditionLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(dayLabel, icon, highLabel, lowLabel, conditionLabel);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #f0f0f0; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #d0d0d0; " +
            "-fx-border-radius: 8; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 8, 0, 0, 2);"
        ));

        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #fafafa; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 8; " +
            "-fx-border-width: 1;"
        ));

        return card;
    }
    
    private void showError() {
        Label errorLabel = new Label("Could not load forecast data");
        errorLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
        forecastCardsContainer.getChildren().add(errorLabel);
    }
    
    /**
     * Refresh temperature displays with the specified unit system
     *
     * @param isImperial true for Fahrenheit, false for Celsius
     */
    public void refreshTemperatures(boolean isImperial) {
        refreshForecastCards(isImperial);
    }

    private String getWeatherEmoji(String condition) {
        return switch (condition.toLowerCase()) {
            case "clear"        -> "☀";
            case "clouds"       -> "☁";
            case "rain"         -> "⛈";
            case "drizzle"      -> "☔";
            case "thunderstorm" -> "⚡";
            case "snow"         -> "❄";
            case "mist", "fog"  -> "≈";
            default             -> "◐";
        };
    }
}

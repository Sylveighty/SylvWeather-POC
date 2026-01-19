package com.school.weatherapp.ui.panels;

import com.school.weatherapp.data.models.Forecast;
import com.school.weatherapp.data.services.ForecastService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
 * @version 1.1 (Centered Layout)
 */
public class HourlyForecastPanel extends VBox {

    private final ForecastService forecastService;
    private HBox forecastCardsContainer;
    private ProgressIndicator loadingIndicator;
    private Label titleLabel;
    private List<Forecast> currentForecasts;

    public HourlyForecastPanel() {
        this.forecastService = new ForecastService();
        
        setPadding(new Insets(20));
        setSpacing(15);
        
        buildTitle();
        buildForecastContainer();
        applyLightTheme();
    }

    public void applyLightTheme() {
        setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

        if (titleLabel != null) {
            titleLabel.setStyle("-fx-text-fill: #333;");
        }

        if (forecastCardsContainer != null) {
            forecastCardsContainer.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 20;"
            );
        }

        updateForecastCardsTheme("#333", "#fafafa", "#f0f0f0", "#e0e0e0");
    }

    public void applyDarkTheme() {
        setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10;");

        if (titleLabel != null) {
            titleLabel.setStyle("-fx-text-fill: #e0e0e0;");
        }

        if (forecastCardsContainer != null) {
            forecastCardsContainer.setStyle(
                "-fx-background-color: #333; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 20;"
            );
        }

        updateForecastCardsTheme("#e0e0e0", "#3a3a3a", "#444", "#555");
    }

    private void updateForecastCardsTheme(String textColor, String cardBg, String hoverBg, String borderColor) {
        if (forecastCardsContainer == null) return;

        for (Node node : forecastCardsContainer.getChildren()) {
            if (!(node instanceof VBox card)) continue;

            card.setStyle(
                "-fx-background-color: " + cardBg + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;"
            );

            for (Node child : card.getChildren()) {
                if (child instanceof Label label) {
                    label.setStyle("-fx-text-fill: " + textColor + ";");
                }
            }

            card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + hoverBg + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"
            ));

            card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + cardBg + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1;"
            ));
        }
    }

    private void buildTitle() {
        titleLabel = new Label("Hourly Forecast");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        getChildren().add(titleLabel);
    }

    private void buildForecastContainer() {
        forecastCardsContainer = new HBox(12);
        forecastCardsContainer.setAlignment(Pos.CENTER);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);

        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(120);

        forecastCardsContainer.getChildren().add(loadingBox);
        getChildren().add(forecastCardsContainer);
    }

    public void loadHourlyForecast(String cityName) {
        Platform.runLater(() -> {
            forecastCardsContainer.getChildren().clear();
            VBox loadingBox = new VBox(loadingIndicator);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPrefHeight(120);
            forecastCardsContainer.getChildren().add(loadingBox);
        });

        forecastService.getHourlyForecastAsync(cityName)
            .thenAccept(forecasts ->
                Platform.runLater(() -> {
                    forecastCardsContainer.getChildren().clear();

                    if (forecasts == null || forecasts.isEmpty()) {
                        showError();
                    } else {
                        displayForecasts(forecasts);
                    }
                })
            );
    }

    private void displayForecasts(List<Forecast> forecasts) {
        currentForecasts = forecasts;
        refreshForecastCards(false); // Default to false (Celsius) initially
    }

    private void refreshForecastCards(boolean isImperial) {
        if (currentForecasts == null) return;

        forecastCardsContainer.getChildren().clear();
        for (Forecast forecast : currentForecasts) {
            forecastCardsContainer.getChildren().add(createForecastCard(forecast, isImperial));
        }
    }

    private VBox createForecastCard(Forecast forecast, boolean isImperial) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setPrefWidth(90);
        card.setStyle("-fx-background-color: #fafafa; " +
                     "-fx-background-radius: 8; " +
                     "-fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 8; " +
                     "-fx-border-width: 1;");

        Label timeLabel = new Label(forecast.getTimeLabel());
        timeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        timeLabel.setStyle("-fx-text-fill: #333;");

        Text icon = new Text(getWeatherEmoji(forecast.getCondition()));
        icon.setStyle("-fx-font-size: 32px; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif;");

        // Determine if conversion is needed
        boolean needConversion = forecast.getTemperatureUnit() != null && 
            ((forecast.getTemperatureUnit().equals("imperial") && !isImperial) ||
             (forecast.getTemperatureUnit().equals("metric") && isImperial));
        
        // Convert temperature based on unit preference
        double tempValue;
        if (needConversion) {
            if (forecast.getTemperatureUnit().equals("imperial")) {
                // Convert from Fahrenheit to Celsius
                tempValue = com.school.weatherapp.util.TemperatureUtil.fahrenheitToCelsius(forecast.getTemperature());
            } else {
                // Convert from Celsius to Fahrenheit
                tempValue = com.school.weatherapp.util.TemperatureUtil.celsiusToFahrenheit(forecast.getTemperature());
            }
        } else {
            // No conversion needed, use original value
            tempValue = forecast.getTemperature();
        }
        String unit = isImperial ? "°F" : "°C";
        Label tempLabel = new Label(String.format("%.0f%s", tempValue, unit));
        tempLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        tempLabel.setStyle("-fx-text-fill: #333;");

        card.getChildren().addAll(timeLabel, icon, tempLabel);

        if (forecast.getPrecipitation() > 0) {
            Label precip = new Label(forecast.getPrecipitation() + "%");
            precip.setFont(Font.font("System", 11));
            precip.setStyle("-fx-text-fill: #1976d2;");
            card.getChildren().add(precip);
        }

        return card;
    }

    private void showError() {
        Label error = new Label("Could not load forecast data");
        error.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
        forecastCardsContainer.getChildren().add(error);
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
            case "clear" -> "☀";
            case "clouds" -> "☁";
            case "rain" -> "⛈";
            case "drizzle" -> "☔";
            case "thunderstorm" -> "⚡";
            case "snow" -> "❄";
            case "mist", "fog" -> "≈";
            default -> "◐";
        };
    }
}

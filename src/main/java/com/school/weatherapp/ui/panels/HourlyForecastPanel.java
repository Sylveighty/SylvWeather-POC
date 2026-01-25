package com.school.weatherapp.ui.panels;

import com.school.weatherapp.data.models.Forecast;
import com.school.weatherapp.data.services.ForecastService;
import com.school.weatherapp.util.TemperatureUtil;
import com.school.weatherapp.util.ThemeUtil;
import com.school.weatherapp.util.WeatherEmojiUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

/**
 * HourlyForecastPanel - UI panel displaying an hourly forecast strip.
 *
 * Shows a horizontal row of forecast "cards" with:
 * - Time label
 * - Weather icon
 * - Temperature
 * - Optional precipitation chance
 *
 * Styling is handled via theme.css / theme-dark.css.
 */
public class HourlyForecastPanel extends VBox {

    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    private final ForecastService forecastService;

    private Label titleLabel;
    private Label cacheNoticeLabel;
    private HBox forecastCardsContainer;
    private VBox containerBox;

    private ProgressIndicator loadingIndicator;
    private List<Forecast> currentForecasts;

    public HourlyForecastPanel() {
        this.forecastService = new ForecastService();

        setPadding(new Insets(20));
        setSpacing(15);

        // Panel styling.
        getStyleClass().add("panel-background");

        buildTitle();
        buildForecastContainer();

        applyLightTheme();
    }

    // -------------------- Theme Methods --------------------

    public void applyLightTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_LIGHT, THEME_DARK);
    }

    public void applyDarkTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_DARK, THEME_LIGHT);
    }

    // -------------------- UI Build --------------------

    private void buildTitle() {
        titleLabel = new Label("Hourly Forecast");
        titleLabel.getStyleClass().add("section-title");

        cacheNoticeLabel = new Label("Offline • Showing cached forecast");
        cacheNoticeLabel.getStyleClass().add("cache-banner");
        cacheNoticeLabel.setVisible(false);

        VBox header = new VBox(4, titleLabel, cacheNoticeLabel);
        getChildren().add(header);
    }

    private void buildForecastContainer() {
        // Inner container (background comes from CSS).
        containerBox = new VBox();
        containerBox.setPadding(new Insets(20));
        containerBox.getStyleClass().add("panel-content");

        forecastCardsContainer = new HBox(12);
        forecastCardsContainer.setAlignment(Pos.CENTER);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);

        containerBox.getChildren().add(forecastCardsContainer);
        getChildren().add(containerBox);

        showLoading();
    }

    // -------------------- Data Load --------------------

    public void loadHourlyForecast(String cityName) {
        showLoading();

        forecastService.getHourlyForecastAsync(cityName)
            .thenAccept(forecasts -> Platform.runLater(() -> {
                if (forecasts == null || forecasts.isEmpty()) {
                    showError();
                } else {
                    currentForecasts = forecasts;
                    updateCacheNotice(forecasts);
                    refreshForecastCards(false); // Default if MainApp hasn't toggled yet.
                }
            }));
    }

    // -------------------- Rendering --------------------

    private void showLoading() {
        Platform.runLater(() -> {
            forecastCardsContainer.getChildren().clear();
            updateCacheNotice(null);

            VBox loadingBox = new VBox(10);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPrefHeight(120);

            Label loadingLabel = new Label("Loading...");
            loadingLabel.getStyleClass().add("label-subtle");

            loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
            forecastCardsContainer.getChildren().add(loadingBox);
        });
    }

    private void showError() {
        forecastCardsContainer.getChildren().clear();

        Label error = new Label("Could not load forecast data");
        error.getStyleClass().add("label-subtle");

        forecastCardsContainer.getChildren().add(error);
        updateCacheNotice(null);
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

        // Card styling comes from CSS (including hover effect).
        card.getStyleClass().add("forecast-card");

        Label timeLabel = new Label(forecast.getTimeLabel());
        timeLabel.getStyleClass().add("label-secondary");

        Text icon = new Text(WeatherEmojiUtil.emojiForCondition(forecast.getCondition()));
        icon.getStyleClass().add("weather-icon");
        icon.setStyle("-fx-font-size: 32px;"); // Keep size local; the emoji font family is in CSS.

        // Determine if conversion is needed.
        boolean needConversion = forecast.getTemperatureUnit() != null &&
            (("imperial".equalsIgnoreCase(forecast.getTemperatureUnit()) && !isImperial) ||
             ("metric".equalsIgnoreCase(forecast.getTemperatureUnit()) && isImperial));

        double tempValue;
        if (needConversion) {
            if ("imperial".equalsIgnoreCase(forecast.getTemperatureUnit())) {
                tempValue = TemperatureUtil.fahrenheitToCelsius(forecast.getTemperature());
            } else {
                tempValue = TemperatureUtil.celsiusToFahrenheit(forecast.getTemperature());
            }
        } else {
            tempValue = forecast.getTemperature();
        }

        String unit = isImperial ? "°F" : "°C";
        Label tempLabel = new Label(String.format("%.0f%s", tempValue, unit));
        tempLabel.getStyleClass().add("label-primary");

        card.getChildren().addAll(timeLabel, icon, tempLabel);

        Label precip = new Label(formatPrecipSummary(forecast, isImperial));
        precip.getStyleClass().add("label-subtle");
        card.getChildren().add(precip);

        return card;
    }

    private String formatPrecipSummary(Forecast forecast, boolean isImperial) {
        int precipChance = forecast.getPrecipitation();
        StringBuilder summary = new StringBuilder(precipChance + "%");

        double rainAmount = forecast.getRainAmount();
        double snowAmount = forecast.getSnowAmount();

        double unitDivisor = isImperial ? 25.4 : 1.0;
        String unitLabel = isImperial ? "in" : "mm";

        if (rainAmount > 0) {
            double rainDisplay = rainAmount / unitDivisor;
            summary.append(String.format(" • %.1f%s rain", rainDisplay, unitLabel));
        }

        if (snowAmount > 0) {
            double snowDisplay = snowAmount / unitDivisor;
            summary.append(String.format(" • %.1f%s snow", snowDisplay, unitLabel));
        }

        return summary.toString();
    }

    /**
     * Refresh temperature displays with the specified unit system.
     *
     * @param isImperial true for Fahrenheit, false for Celsius
     */
    public void refreshTemperatures(boolean isImperial) {
        refreshForecastCards(isImperial);
    }

    private void updateCacheNotice(List<Forecast> forecasts) {
        if (cacheNoticeLabel == null) {
            return;
        }
        boolean cached = forecasts != null && forecasts.stream().anyMatch(Forecast::isCached);
        cacheNoticeLabel.setVisible(cached);
    }
}

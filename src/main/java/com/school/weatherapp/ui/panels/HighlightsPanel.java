package com.school.weatherapp.ui.panels;

import com.school.weatherapp.data.models.Weather;
import com.school.weatherapp.data.services.WeatherService;
import com.school.weatherapp.features.UserPreferencesService;
import com.school.weatherapp.util.DateTimeUtil;
import com.school.weatherapp.util.TemperatureUtil;
import com.school.weatherapp.util.ThemeUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * HighlightsPanel - UI panel showing key weather highlights.
 */
public class HighlightsPanel extends VBox {

    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    private final WeatherService weatherService;

    private Label feelsLikeValue;
    private Label humidityValue;
    private Label windValue;
    private Label uvValue;
    private Label sunriseValue;
    private Label visibilityValue;

    private ProgressIndicator loadingIndicator;
    private Weather currentWeather;
    private boolean isImperial;
    private Label cacheNoticeLabel;

    public HighlightsPanel() {
        this.weatherService = new WeatherService();
        UserPreferencesService preferencesService = new UserPreferencesService();
        this.isImperial = "imperial".equalsIgnoreCase(preferencesService.getTemperatureUnit());

        setPadding(new Insets(20));
        setSpacing(15);
        getStyleClass().add("panel-background");

        buildTitle();
        buildHighlightsGrid();

        applyLightTheme();
    }

    public void applyLightTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_LIGHT, THEME_DARK);
    }

    public void applyDarkTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_DARK, THEME_LIGHT);
    }

    private void buildTitle() {
        Label title = new Label("Today's Highlights");
        title.getStyleClass().add("section-title");

        cacheNoticeLabel = new Label("Offline • Showing cached data");
        cacheNoticeLabel.getStyleClass().add("cache-banner");
        cacheNoticeLabel.setVisible(false);

        VBox header = new VBox(4, title, cacheNoticeLabel);
        getChildren().add(header);
    }

    private void buildHighlightsGrid() {
        VBox container = new VBox(12);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("panel-content");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            column.setPercentWidth(33.33);
            grid.getColumnConstraints().add(column);
        }

        feelsLikeValue = createValueLabel("--");
        humidityValue = createValueLabel("--");
        windValue = createValueLabel("--");
        uvValue = createValueLabel("--");
        sunriseValue = createValueLabel("--");
        visibilityValue = createValueLabel("--");

        grid.add(createHighlightCard("Feels Like", feelsLikeValue), 0, 0);
        grid.add(createHighlightCard("Humidity", humidityValue), 1, 0);
        grid.add(createHighlightCard("Wind", windValue), 2, 0);
        grid.add(createHighlightCard("UV Index", uvValue), 0, 1);
        grid.add(createHighlightCard("Sunrise / Sunset", sunriseValue), 1, 1);
        grid.add(createHighlightCard("Visibility", visibilityValue), 2, 1);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        loadingIndicator.setVisible(false);

        container.getChildren().addAll(grid, loadingIndicator);
        getChildren().add(container);
    }

    private VBox createHighlightCard(String title, Label valueLabel) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("highlight-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("highlight-title");
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("highlight-value");
        return label;
    }

    public void loadHighlights(String cityName) {
        showLoadingState(true);

        weatherService.getCurrentWeatherAsync(cityName)
            .thenAccept(weather -> Platform.runLater(() -> {
                showLoadingState(false);
                if (weather != null) {
                    currentWeather = weather;
                    updateDisplayWithUnit(weather, isImperial);
                    updateCacheNotice(weather);
                } else {
                    setValuePlaceholders("--");
                    updateCacheNotice(null);
                }
            }));
    }

    public void refreshTemperatures(boolean isImperial) {
        this.isImperial = isImperial;
        if (currentWeather != null) {
            updateDisplayWithUnit(currentWeather, isImperial);
        }
    }

    private void updateDisplayWithUnit(Weather weather, boolean isImperial) {
        boolean needConversion = weather.getTemperatureUnit() != null &&
            (("imperial".equalsIgnoreCase(weather.getTemperatureUnit()) && !isImperial) ||
             ("metric".equalsIgnoreCase(weather.getTemperatureUnit()) && isImperial));

        double feelsLikeValue;
        if (needConversion) {
            feelsLikeValue = "imperial".equalsIgnoreCase(weather.getTemperatureUnit())
                ? TemperatureUtil.fahrenheitToCelsius(weather.getFeelsLike())
                : TemperatureUtil.celsiusToFahrenheit(weather.getFeelsLike());
        } else {
            feelsLikeValue = weather.getFeelsLike();
        }

        String tempUnit = isImperial ? "\u00B0F" : "\u00B0C";
        this.feelsLikeValue.setText(String.format("%.0f%s", feelsLikeValue, tempUnit));

        humidityValue.setText(weather.getHumidity() + "%");

        String windUnit = isImperial ? "mph" : "m/s";
        double windSpeedValue;
        if (needConversion) {
            windSpeedValue = "imperial".equalsIgnoreCase(weather.getTemperatureUnit())
                ? (weather.getWindSpeed() / 2.237)
                : (weather.getWindSpeed() * 2.237);
        } else {
            windSpeedValue = weather.getWindSpeed();
        }
        windValue.setText(String.format("%.1f %s %s", windSpeedValue, windUnit, weather.getWindDirectionCompass()));

        if (weather.getUvIndex() >= 0) {
            uvValue.setText(String.valueOf(weather.getUvIndex()));
        } else {
            uvValue.setText("--");
        }

        if (weather.getSunriseTimestamp() > 0 && weather.getSunsetTimestamp() > 0) {
            String sunrise = DateTimeUtil.formatTime(weather.getSunriseTimestamp());
            String sunset = DateTimeUtil.formatTime(weather.getSunsetTimestamp());
            sunriseValue.setText(String.format("%s / %s", sunrise, sunset));
        } else {
            sunriseValue.setText("--");
        }

        if (weather.getVisibility() > 0) {
            if (isImperial) {
                double miles = weather.getVisibility() / 1609.34;
                visibilityValue.setText(String.format("%.1f mi", miles));
            } else {
                double km = weather.getVisibility() / 1000.0;
                visibilityValue.setText(String.format("%.1f km", km));
            }
        } else {
            visibilityValue.setText("--");
        }
    }

    private void updateCacheNotice(Weather weather) {
        if (cacheNoticeLabel == null) {
            return;
        }
        cacheNoticeLabel.setVisible(weather != null && weather.isCached());
    }

    private void showLoadingState(boolean isLoading) {
        loadingIndicator.setVisible(isLoading);
        if (isLoading) {
            setValuePlaceholders("Loading...");
            updateCacheNotice(null);
        }
    }

    private void setValuePlaceholders(String text) {
        feelsLikeValue.setText(text);
        humidityValue.setText(text);
        windValue.setText(text);
        uvValue.setText(text);
        sunriseValue.setText(text);
        visibilityValue.setText(text);
    }
}

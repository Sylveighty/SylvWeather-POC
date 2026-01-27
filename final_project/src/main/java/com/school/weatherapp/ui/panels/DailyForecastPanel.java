package com.school.weatherapp.ui.panels;

import com.school.weatherapp.data.models.Forecast;
import com.school.weatherapp.data.services.ForecastService;
import com.school.weatherapp.util.TemperatureUtil;
import com.school.weatherapp.util.ThemeUtil;
import com.school.weatherapp.util.WeatherEmojiResolver;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

/**
 * DailyForecastPanel - UI panel displaying a daily forecast summary.
 *
 * Displays a vertical list of day "cards" summarizing:
 * - Day label
 * - Weather icon / condition
 * - Min / max temperature range
 *
 * Styling is handled via theme.css / theme-dark.css.
 */
public class DailyForecastPanel extends VBox {

    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    private final ForecastService forecastService;

    private Label titleLabel;
    private VBox dailyCardsContainer;
    private VBox containerBox;

    private ProgressIndicator loadingIndicator;
    private List<Forecast> currentDailyForecasts;

    public DailyForecastPanel() {
        this.forecastService = new ForecastService();

        setPadding(new Insets(12));
        setSpacing(10);

        getStyleClass().add("panel-background");

        buildTitle();
        buildDailyContainer();

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
        titleLabel = new Label("7–10 Day Forecast");
        titleLabel.getStyleClass().add("section-title");

        VBox header = new VBox(3, titleLabel);
        getChildren().add(header);
    }

    private void buildDailyContainer() {
        containerBox = new VBox(8);
        containerBox.setPadding(new Insets(12));
        containerBox.getStyleClass().add("panel-content");

        dailyCardsContainer = new VBox(8);
        dailyCardsContainer.setAlignment(Pos.TOP_CENTER);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(28, 28);

        containerBox.getChildren().add(dailyCardsContainer);
        getChildren().add(containerBox);

        showLoading();
    }

    // -------------------- Data Load --------------------

    public void loadDailyForecast(String cityName) {
        showLoading();

        forecastService.getDailyForecastAsync(cityName)
            .thenAccept(forecasts -> Platform.runLater(() -> {
                if (forecasts == null || forecasts.isEmpty()) {
                    showError();
                } else {
                    currentDailyForecasts = forecasts;
                    refreshDailyCards(false); // Default; MainApp unit toggle will call refreshTemperatures(...).
                }
            }));
    }

    // -------------------- Rendering --------------------

    private void showLoading() {
        Platform.runLater(() -> {
            dailyCardsContainer.getChildren().clear();

            VBox loadingBox = new VBox(6);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(10));

            Label loadingLabel = new Label("Loading...");
            loadingLabel.getStyleClass().add("label-subtle");

            loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
            dailyCardsContainer.getChildren().add(loadingBox);
        });
    }

    private void showError() {
        dailyCardsContainer.getChildren().clear();

        Label error = new Label("Could not load daily forecast data");
        error.getStyleClass().add("label-subtle");

        dailyCardsContainer.getChildren().add(error);
    }

    private void refreshDailyCards(boolean isImperial) {
        if (currentDailyForecasts == null) return;

        dailyCardsContainer.getChildren().clear();
        for (Forecast daily : currentDailyForecasts) {
            dailyCardsContainer.getChildren().add(createDailyCard(daily, isImperial));
        }
    }

    private HBox createDailyCard(Forecast daily, boolean isImperial) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.getStyleClass().add("forecast-card");

        // Day label.
        Label dayLabel = new Label(daily.getDayOfWeek() != null ? daily.getDayOfWeek() : "--");
        dayLabel.getStyleClass().add("label-primary");
        dayLabel.setMinWidth(70);

        // Icon.
        Text icon = new Text(WeatherEmojiResolver.resolveEmoji(daily.getIconCode(), daily.getCondition()));
        icon.getStyleClass().add("weather-icon");
        icon.setStyle("-fx-font-size: 22px;");

        // Condition text (optional).
        Label condLabel = new Label(daily.getCondition() != null ? daily.getCondition() : "");
        condLabel.getStyleClass().add("label-secondary");
        condLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(condLabel, Priority.ALWAYS);

        // Min/max temperatures.
        String rangeText = formatTempRange(daily, isImperial);
        Label rangeLabel = new Label(rangeText);
        rangeLabel.getStyleClass().add("label-primary");
        rangeLabel.setMinWidth(110);
        rangeLabel.setAlignment(Pos.CENTER_RIGHT);

        Label precipLabel = new Label(formatPrecipSummary(daily, isImperial));
        precipLabel.getStyleClass().add("label-subtle");
        precipLabel.setAlignment(Pos.CENTER_RIGHT);

        VBox detailsBox = new VBox(2, rangeLabel, precipLabel);
        detailsBox.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(dayLabel, icon, condLabel, detailsBox);
        return row;
    }

    private String formatTempRange(Forecast daily, boolean isImperial) {
        String unit = isImperial ? "\u00B0F" : "\u00B0C";

        boolean needConversion = daily.getTemperatureUnit() != null &&
            (("imperial".equalsIgnoreCase(daily.getTemperatureUnit()) && !isImperial) ||
             ("metric".equalsIgnoreCase(daily.getTemperatureUnit()) && isImperial));

        double min = daily.getTempMin();
        double max = daily.getTempMax();

        if (needConversion) {
            if ("imperial".equalsIgnoreCase(daily.getTemperatureUnit())) {
                min = TemperatureUtil.fahrenheitToCelsius(min);
                max = TemperatureUtil.fahrenheitToCelsius(max);
            } else {
                min = TemperatureUtil.celsiusToFahrenheit(min);
                max = TemperatureUtil.celsiusToFahrenheit(max);
            }
        }

        return String.format("%.0f%s / %.0f%s", min, unit, max, unit);
    }

    private String formatPrecipSummary(Forecast daily, boolean isImperial) {
        int precipChance = daily.getPrecipitation();
        StringBuilder summary = new StringBuilder(precipChance + "%");

        double rainAmount = daily.getRainAmount();
        double snowAmount = daily.getSnowAmount();

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
        refreshDailyCards(isImperial);
    }
}

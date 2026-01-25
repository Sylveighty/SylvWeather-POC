package com.school.weatherapp.ui.panels;

import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.data.models.Weather;
import com.school.weatherapp.data.services.WeatherService;
import com.school.weatherapp.features.FavoritesService;
import com.school.weatherapp.util.DateTimeUtil;
import com.school.weatherapp.util.TemperatureUtil;
import com.school.weatherapp.util.ThemeUtil;
import com.school.weatherapp.util.WeatherEmojiUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * CurrentWeatherPanel - UI panel displaying current weather conditions.
 *
 * This panel shows:
 * - City name and search box
 * - Current temperature (large display)
 * - Weather condition and description
 * - Weather icon representation
 * - Additional details (feels like, humidity, wind, pressure)
 * - Last updated timestamp
 *
 * POC note: error handling is intentionally lightweight and UI-focused.
 */
public class CurrentWeatherPanel extends VBox {

    // Stylesheet resource paths.
    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    // Services.
    private final WeatherService weatherService;
    private final FavoritesService favoritesService;

    // UI components.
    private TextField searchField;
    private Button searchButton;
    private Button favoriteButton;
    private Label errorLabel;

    private Label cityLabel;
    private Label temperatureLabel;
    private Label conditionLabel;
    private Label descriptionLabel;
    private Text weatherIcon;

    private Label feelsLikeLabel;
    private Label humidityLabel;
    private Label windLabel;
    private Label pressureLabel;
    private Label uvLabel;
    private Label sunriseLabel;

    private Label lastUpdatedLabel;
    private ProgressIndicator loadingIndicator;

    private GridPane detailsGrid;

    // Current weather data.
    private Weather currentWeather;

    // Callbacks.
    private Consumer<String> onCityChangeCallback;
    private Runnable onFavoritesChangeCallback;

    public CurrentWeatherPanel(FavoritesService favoritesService) {
        this.weatherService = new WeatherService();
        this.favoritesService = favoritesService;

        // Layout (no color/typography here; CSS handles that).
        setPadding(new Insets(20));
        setSpacing(15);
        setMaxWidth(450);

        // Base style class for panel container.
        getStyleClass().add("panel-background");

        buildSearchBar();
        buildMainDisplay();
        buildDetailsGrid();
        buildFooter();

        // Load default city weather.
        loadWeather(AppConfig.DEFAULT_CITY);
    }

    // -------------------- Theme Methods --------------------

    /**
     * Apply light theme styling.
     * For CSS-based themes, this ensures theme.css is the last loaded stylesheet.
     */
    public void applyLightTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_LIGHT, THEME_DARK);
    }

    /**
     * Apply dark theme styling.
     * For CSS-based themes, this ensures theme-dark.css is the last loaded stylesheet.
     */
    public void applyDarkTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_DARK, THEME_LIGHT);
    }

    // -------------------- Callback Methods --------------------

    public void setOnCityChange(Consumer<String> callback) {
        this.onCityChangeCallback = callback;
    }

    public void setOnFavoritesChange(Runnable callback) {
        this.onFavoritesChangeCallback = callback;
    }

    // -------------------- UI Building Methods --------------------

    private void buildSearchBar() {
        VBox searchArea = new VBox(6);
        searchArea.getStyleClass().add("search-area");

        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Enter city name...");
        searchField.setPrefWidth(220);
        searchField.setAccessibleText("City search input");
        // JavaFX TextField already has "text-field"; keep it and optionally add more if desired.

        searchButton = new Button("Search");
        searchButton.getStyleClass().add("search-button");
        searchButton.setAccessibleText("Search for city weather");

        favoriteButton = new Button("☆ Add to Favorites");
        favoriteButton.setAccessibleText("Add city to favorites");
        // Default state: add.
        setFavoriteButtonState(false);

        searchButton.setOnAction(e -> handleSearch());
        searchField.setOnAction(e -> handleSearch());
        favoriteButton.setOnAction(e -> handleFavoriteToggle());

        searchBar.getChildren().addAll(searchField, searchButton, favoriteButton);

        errorLabel = new Label();
        errorLabel.getStyleClass().add("input-error");
        errorLabel.setVisible(false);

        searchArea.getChildren().addAll(searchBar, errorLabel);
        getChildren().add(searchArea);
    }

    private void buildMainDisplay() {
        VBox mainDisplay = new VBox(10);
        mainDisplay.setAlignment(Pos.CENTER);
        mainDisplay.setPadding(new Insets(20, 0, 20, 0));

        cityLabel = new Label("Loading...");
        cityLabel.getStyleClass().add("city-label");

        weatherIcon = new Text("☀");
        weatherIcon.getStyleClass().add("weather-icon");

        temperatureLabel = new Label("--°");
        temperatureLabel.getStyleClass().add("temperature-large");

        conditionLabel = new Label("--");
        conditionLabel.getStyleClass().add("condition-label");

        descriptionLabel = new Label("--");
        descriptionLabel.getStyleClass().add("description-label");

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        loadingIndicator.setVisible(false);

        mainDisplay.getChildren().addAll(
            cityLabel,
            weatherIcon,
            temperatureLabel,
            conditionLabel,
            descriptionLabel,
            loadingIndicator
        );

        getChildren().add(mainDisplay);
    }

    private void buildDetailsGrid() {
        detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(15);
        detailsGrid.setAlignment(Pos.CENTER);
        detailsGrid.setPadding(new Insets(10));
        detailsGrid.getStyleClass().add("panel-content");

        // Feels like.
        Label feelsLikeTitle = createDetailTitle("Feels Like");
        feelsLikeLabel = createDetailValue("--°");
        detailsGrid.add(feelsLikeTitle, 0, 0);
        detailsGrid.add(feelsLikeLabel, 0, 1);

        // Humidity.
        Label humidityTitle = createDetailTitle("Humidity");
        humidityLabel = createDetailValue("--%");
        detailsGrid.add(humidityTitle, 1, 0);
        detailsGrid.add(humidityLabel, 1, 1);

        // Wind.
        Label windTitle = createDetailTitle("Wind Speed");
        windLabel = createDetailValue("-- mph");
        detailsGrid.add(windTitle, 0, 2);
        detailsGrid.add(windLabel, 0, 3);

        // Pressure.
        Label pressureTitle = createDetailTitle("Pressure");
        pressureLabel = createDetailValue("-- hPa");
        detailsGrid.add(pressureTitle, 1, 2);
        detailsGrid.add(pressureLabel, 1, 3);

        // UV Index.
        Label uvTitle = createDetailTitle("UV Index");
        uvLabel = createDetailValue("--");
        detailsGrid.add(uvTitle, 0, 4);
        detailsGrid.add(uvLabel, 0, 5);

        // Sunrise / Sunset.
        Label sunriseTitle = createDetailTitle("Sunrise / Sunset");
        sunriseLabel = createDetailValue("--");
        detailsGrid.add(sunriseTitle, 1, 4);
        detailsGrid.add(sunriseLabel, 1, 5);

        getChildren().add(detailsGrid);
    }

    private void buildFooter() {
        lastUpdatedLabel = new Label("Last updated: --");
        lastUpdatedLabel.getStyleClass().add("footer-label");
        lastUpdatedLabel.setAlignment(Pos.CENTER);
        lastUpdatedLabel.setMaxWidth(Double.MAX_VALUE);

        getChildren().add(lastUpdatedLabel);
    }

    private Label createDetailTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("detail-title");
        return label;
    }

    private Label createDetailValue(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("detail-value");
        return label;
    }

    // -------------------- Event Handlers --------------------

    private void handleSearch() {
        String city = searchField.getText().trim();
        if (!city.isEmpty()) {
            clearError();
            loadWeather(city);
            searchField.clear();
            return;
        }
        showError("City can't be empty.");
    }

    private void handleFavoriteToggle() {
        if (currentWeather == null) return;

        String cityName = currentWeather.getCityName();
        boolean isFavorite = favoritesService.isFavorite(cityName);

        if (isFavorite) {
            favoritesService.removeFavorite(cityName);
        } else {
            favoritesService.addFavorite(cityName);
        }

        // Update button UI.
        setFavoriteButtonState(!isFavorite);

        if (onFavoritesChangeCallback != null) {
            onFavoritesChangeCallback.run();
        }
    }

    private void setFavoriteButtonState(boolean isFavorite) {
        // Remove both state classes first.
        favoriteButton.getStyleClass().remove("favorite-add");
        favoriteButton.getStyleClass().remove("favorite-remove");

        if (isFavorite) {
            favoriteButton.setText("★ Remove from Favorites");
            favoriteButton.getStyleClass().add("favorite-remove");
        } else {
            favoriteButton.setText("☆ Add to Favorites");
            favoriteButton.getStyleClass().add("favorite-add");
        }
    }

    private void updateFavoriteButtonState(String cityName) {
        if (cityName == null) return;
        setFavoriteButtonState(favoritesService.isFavorite(cityName));
    }

    // -------------------- Weather Loading and Display --------------------

    private void loadWeather(String cityName) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            searchButton.setDisable(true);
            searchField.setDisable(true);
            favoriteButton.setDisable(true);
            clearError();
        });

        weatherService.getCurrentWeatherAsync(cityName)
            .thenAccept(weather -> Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                searchButton.setDisable(false);
                searchField.setDisable(false);
                favoriteButton.setDisable(false);

                if (weather != null) {
                    currentWeather = weather;
                    updateDisplay(weather);
                } else {
                    showError("Could not fetch weather for: " + cityName);
                }
            }));
    }

    private void updateDisplay(Weather weather) {
        // City.
        cityLabel.setText(weather.getFullLocation());

        // Temperature (uses the unit in AppConfig at time of fetch).
        String tempUnit = "imperial".equalsIgnoreCase(AppConfig.TEMPERATURE_UNIT) ? "°F" : "°C";
        temperatureLabel.setText(String.format("%.0f%s", weather.getTemperature(), tempUnit));

        // Condition and description.
        conditionLabel.setText(weather.getCondition());
        descriptionLabel.setText(capitalize(weather.getDescription()));

        // Icon.
        weatherIcon.setText(WeatherEmojiUtil.emojiForCondition(weather.getCondition()));

        // Details.
        feelsLikeLabel.setText(String.format("%.0f%s", weather.getFeelsLike(), tempUnit));
        humidityLabel.setText(weather.getHumidity() + "%");

        String windUnit = "imperial".equalsIgnoreCase(AppConfig.TEMPERATURE_UNIT) ? "mph" : "m/s";
        windLabel.setText(String.format("%.1f %s %s",
            weather.getWindSpeed(),
            windUnit,
            weather.getWindDirectionCompass()
        ));

        pressureLabel.setText(weather.getPressure() + " hPa");

        if (weather.getUvIndex() >= 0) {
            uvLabel.setText(String.valueOf(weather.getUvIndex()));
        } else {
            uvLabel.setText("--");
        }

        if (weather.getSunriseTimestamp() > 0 && weather.getSunsetTimestamp() > 0) {
            String sunrise = DateTimeUtil.formatTime(weather.getSunriseTimestamp());
            String sunset = DateTimeUtil.formatTime(weather.getSunsetTimestamp());
            sunriseLabel.setText(String.format("%s / %s", sunrise, sunset));
        } else {
            sunriseLabel.setText("--");
        }

        // Timestamp.
        String timestamp = DateTimeUtil.formatDateTime(weather.getTimestamp());
        lastUpdatedLabel.setText("Last updated: " + timestamp);

        // Favorites button.
        updateFavoriteButtonState(weather.getCityName());

        // Notify city change.
        if (onCityChangeCallback != null) {
            onCityChangeCallback.accept(weather.getCityName());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    // -------------------- Utility Methods --------------------

    private String capitalize(String text) {
        if (text == null || text.isBlank()) return "";
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
            }
        }
        return result.toString().trim();
    }

    // -------------------- Public Methods --------------------

    public Weather getCurrentWeather() {
        return currentWeather;
    }

    public void refresh() {
        if (currentWeather != null) {
            loadWeather(currentWeather.getCityName());
        }
    }

    public void loadCityWeather(String cityName) {
        loadWeather(cityName);
    }

    public void refreshTemperatures(boolean isImperial) {
        if (currentWeather != null) {
            updateDisplayWithUnit(currentWeather, isImperial);
        }
    }

    private void updateDisplayWithUnit(Weather weather, boolean isImperial) {
        boolean needConversion = weather.getTemperatureUnit() != null &&
            (("imperial".equalsIgnoreCase(weather.getTemperatureUnit()) && !isImperial) ||
             ("metric".equalsIgnoreCase(weather.getTemperatureUnit()) && isImperial));

        // Temperature.
        double tempValue;
        if (needConversion) {
            tempValue = "imperial".equalsIgnoreCase(weather.getTemperatureUnit())
                ? TemperatureUtil.fahrenheitToCelsius(weather.getTemperature())
                : TemperatureUtil.celsiusToFahrenheit(weather.getTemperature());
        } else {
            tempValue = weather.getTemperature();
        }

        String tempUnit = isImperial ? "°F" : "°C";
        temperatureLabel.setText(String.format("%.0f%s", tempValue, tempUnit));

        // Feels like.
        double feelsLikeValue;
        if (needConversion) {
            feelsLikeValue = "imperial".equalsIgnoreCase(weather.getTemperatureUnit())
                ? TemperatureUtil.fahrenheitToCelsius(weather.getFeelsLike())
                : TemperatureUtil.celsiusToFahrenheit(weather.getFeelsLike());
        } else {
            feelsLikeValue = weather.getFeelsLike();
        }
        feelsLikeLabel.setText(String.format("%.0f%s", feelsLikeValue, tempUnit));

        // Wind speed.
        String windUnit = isImperial ? "mph" : "m/s";
        double windSpeedValue;
        if (needConversion) {
            // mph <-> m/s conversion.
            windSpeedValue = "imperial".equalsIgnoreCase(weather.getTemperatureUnit())
                ? (weather.getWindSpeed() / 2.237)
                : (weather.getWindSpeed() * 2.237);
        } else {
            windSpeedValue = weather.getWindSpeed();
        }

        windLabel.setText(String.format("%.1f %s %s",
            windSpeedValue,
            windUnit,
            weather.getWindDirectionCompass()
        ));
    }
}

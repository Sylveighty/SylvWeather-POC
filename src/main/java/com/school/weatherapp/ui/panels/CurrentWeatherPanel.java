package com.school.weatherapp.ui.panels;

import com.school.weatherapp.data.models.Weather;
import com.school.weatherapp.data.services.WeatherService;
import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.features.FavoritesService;
import com.school.weatherapp.util.DateTimeUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * CurrentWeatherPanel - UI panel displaying current weather conditions
 * 
 * This panel shows:
 * - City name and search box
 * - Current temperature (large display)
 * - Weather condition and description
 * - Weather icon representation
 * - Additional details (feels like, humidity, wind, pressure)
 * - Last updated timestamp
 * 
 * @author Weather App Team
 * @version 1.1 (Phase 2)
 */
public class CurrentWeatherPanel extends VBox {
    
    // Services
    private final WeatherService weatherService;
    private final FavoritesService favoritesService;
    
    // UI Components
    private TextField searchField;
    private Button searchButton;
    private Button favoriteButton;
    private Label cityLabel;
    private Label temperatureLabel;
    private Label conditionLabel;
    private Label descriptionLabel;
    private Text weatherIcon;
    private Label feelsLikeLabel;
    private Label humidityLabel;
    private Label windLabel;
    private Label pressureLabel;
    private Label lastUpdatedLabel;
    private ProgressIndicator loadingIndicator;
    private GridPane detailsGrid;
    
    // Current weather data
    private Weather currentWeather;
    
    // Callback for when city changes
    private Consumer<String> onCityChangeCallback;
    
    // Callback for when favorites change
    private Runnable onFavoritesChangeCallback;
    
    /**
     * Constructor - builds the UI panel
     */
    public CurrentWeatherPanel() {
        this.weatherService = new WeatherService();
        this.favoritesService = new FavoritesService();

        // Panel styling
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.applyLightTheme();
        this.setMaxWidth(450);

        // Build UI
        buildSearchBar();
        buildMainDisplay();
        buildDetailsGrid();
        buildFooter();

        // Load default city weather
        loadWeather(AppConfig.DEFAULT_CITY);
    }
    
    /**
     * Apply light theme colors
     */
    public void applyLightTheme() {
        this.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");

        // Update search field
        if (searchField != null) {
            searchField.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-control-inner-background: white; -fx-border-color: #ccc; -fx-border-radius: 4; -fx-padding: 8;");
        }

        // Update main labels
        if (cityLabel != null) cityLabel.setStyle("-fx-text-fill: #333;");
        if (temperatureLabel != null) temperatureLabel.setStyle("-fx-text-fill: #333;");
        if (conditionLabel != null) conditionLabel.setStyle("-fx-text-fill: #666;");
        if (descriptionLabel != null) descriptionLabel.setStyle("-fx-text-fill: #888;");
        if (weatherIcon != null) weatherIcon.setStyle("-fx-font-size: 80px; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif;");

        // Update details grid background
        if (detailsGrid != null) {
            detailsGrid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        }

        // Update detail labels
        if (detailsGrid != null) {
            updateDetailLabelsTextColor("#333", "#999");
        }

        // Update footer
        if (lastUpdatedLabel != null) lastUpdatedLabel.setStyle("-fx-text-fill: #999;");

        // Update buttons
        if (searchButton != null) {
            searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                 "-fx-font-weight: bold; -fx-cursor: hand;");
        }

        // Update favorite button for light theme
        if (favoriteButton != null) {
            if (currentWeather != null && favoritesService.isFavorite(currentWeather.getCityName())) {
                favoriteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                favoriteButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-cursor: hand;");
            }
        }
    }

    /**
     * Apply dark theme colors (Discord/VS Code style)
     */
    public void applyDarkTheme() {
        this.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 10;");

        // Update search field - white background for better readability in dark mode
        if (searchField != null) {
            searchField.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-control-inner-background: white; -fx-border-color: #ccc; -fx-border-radius: 4; -fx-padding: 8;");
        }

        // Update main labels (Discord/VS Code style colors)
        if (cityLabel != null) cityLabel.setStyle("-fx-text-fill: #e0e0e0;");
        if (temperatureLabel != null) temperatureLabel.setStyle("-fx-text-fill: #e0e0e0;");
        if (conditionLabel != null) conditionLabel.setStyle("-fx-text-fill: #b0b0b0;");
        if (descriptionLabel != null) descriptionLabel.setStyle("-fx-text-fill: #b0b0b0;");
        if (weatherIcon != null) weatherIcon.setStyle("-fx-font-size: 80px; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif;");

        // Update details grid background (VS Code dark theme style)
        if (detailsGrid != null) {
            detailsGrid.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-border-color: #3e3e42; -fx-border-width: 1;");
        }

        // Update detail labels
        if (detailsGrid != null) {
            updateDetailLabelsTextColor("#e0e0e0", "#b0b0b0");
        }

        // Update footer
        if (lastUpdatedLabel != null) lastUpdatedLabel.setStyle("-fx-text-fill: #b0b0b0;");

        // Update buttons
        if (searchButton != null) {
            searchButton.setStyle("-fx-background-color: #2d2d30; -fx-text-fill: #ffffff; " +
                                 "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #3e3e42; -fx-border-width: 1;");
        }

        // Update favorite button for dark theme (same colors as light theme for visibility)
        if (favoriteButton != null) {
            if (currentWeather != null && favoritesService.isFavorite(currentWeather.getCityName())) {
                favoriteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                favoriteButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-cursor: hand;");
            }
        }
    }

    /**
     * Helper method to update detail labels text colors
     */
    private void updateDetailLabelsTextColor(String primaryColor, String secondaryColor) {
        if (feelsLikeLabel != null) feelsLikeLabel.setStyle("-fx-text-fill: " + primaryColor + ";");
        if (humidityLabel != null) humidityLabel.setStyle("-fx-text-fill: " + primaryColor + ";");
        if (windLabel != null) windLabel.setStyle("-fx-text-fill: " + primaryColor + ";");
        if (pressureLabel != null) pressureLabel.setStyle("-fx-text-fill: " + primaryColor + ";");

        // Update detail titles
        for (javafx.scene.Node node : detailsGrid.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getText().equals("Feels Like") || label.getText().equals("Humidity") ||
                    label.getText().equals("Wind Speed") || label.getText().equals("Pressure")) {
                    label.setStyle("-fx-text-fill: " + secondaryColor + ";");
                }
            }
        }
    }
    
    /**
     * Set callback to be notified when city changes
     * 
     * @param callback Function to call with new city name
     */
    public void setOnCityChange(Consumer<String> callback) {
        this.onCityChangeCallback = callback;
    }
    
    /**
     * Set callback to be notified when favorites change
     * 
     * @param callback Runnable to call when favorites are added/removed
     */
    public void setOnFavoritesChange(Runnable callback) {
        this.onFavoritesChangeCallback = callback;
    }
    
    /**
     * Build search bar with text field and button
     */
    private void buildSearchBar() {
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER);

        // Search field with enhanced styling for better usability
        searchField = new TextField();
        searchField.setPromptText("Enter city name (e.g., New York, London, Tokyo)...");
        searchField.setPrefWidth(250);
        searchField.setPrefHeight(40);
        searchField.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #333; " +
            "-fx-control-inner-background: white; " +
            "-fx-border-color: #ccc; " +
            "-fx-border-radius: 6; " +
            "-fx-border-width: 2; " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 6;"
        );
        
        // Add hover effect for better user feedback
        searchField.setOnMouseEntered(e -> searchField.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #333; " +
            "-fx-control-inner-background: white; " +
            "-fx-border-color: #4CAF50; " +
            "-fx-border-radius: 6; " +
            "-fx-border-width: 2; " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 6; " +
            "-fx-effect: dropshadow(gaussian, rgba(76, 175, 80, 0.3), 6, 0, 0, 1);"
        ));
        
        searchField.setOnMouseExited(e -> searchField.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #333; " +
            "-fx-control-inner-background: white; " +
            "-fx-border-color: #ccc; " +
            "-fx-border-radius: 6; " +
            "-fx-border-width: 2; " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 6;"
        ));

        // Search button
        searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                             "-fx-font-weight: bold; -fx-cursor: hand;");

        // Favorite button
        favoriteButton = new Button("☆ Add to Favorites");
        favoriteButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                               "-fx-font-weight: bold; -fx-cursor: hand;");
        favoriteButton.setOnAction(e -> handleFavoriteToggle());

        // Handle search on button click or Enter key
        searchButton.setOnAction(e -> handleSearch());
        searchField.setOnAction(e -> handleSearch());

        searchBar.getChildren().addAll(searchField, searchButton, favoriteButton);
        this.getChildren().add(searchBar);
    }
    
    /**
     * Build main weather display (city, temp, condition)
     */
    private void buildMainDisplay() {
        VBox mainDisplay = new VBox(10);
        mainDisplay.setAlignment(Pos.CENTER);
        mainDisplay.setPadding(new Insets(20, 0, 20, 0));
        
        // City name
        cityLabel = new Label("Loading...");
        cityLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        cityLabel.setStyle("-fx-text-fill: #333;");
        
        // Weather icon (using emoji/unicode)
        weatherIcon = new Text("☀");
        weatherIcon.setStyle("-fx-font-size: 80px; -fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif;");
        
        // Temperature (large display)
        temperatureLabel = new Label("--°");
        temperatureLabel.setFont(Font.font("System", FontWeight.BOLD, 72));
        temperatureLabel.setStyle("-fx-text-fill: #333;");
        
        // Condition (e.g., "Clear")
        conditionLabel = new Label("--");
        conditionLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        conditionLabel.setStyle("-fx-text-fill: #666;");
        
        // Description (e.g., "clear sky")
        descriptionLabel = new Label("--");
        descriptionLabel.setFont(Font.font("System", 16));
        descriptionLabel.setStyle("-fx-text-fill: #888;");
        
        // Loading indicator
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
        
        this.getChildren().add(mainDisplay);
    }
    
    /**
     * Build details grid (feels like, humidity, wind, pressure)
     */
    private void buildDetailsGrid() {
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(15);
        detailsGrid.setAlignment(Pos.CENTER);
        detailsGrid.setPadding(new Insets(10));
        detailsGrid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        
        // Feels Like
        Label feelsLikeTitle = createDetailTitle("Feels Like");
        feelsLikeLabel = createDetailValue("--°");
        detailsGrid.add(feelsLikeTitle, 0, 0);
        detailsGrid.add(feelsLikeLabel, 0, 1);
        
        // Humidity
        Label humidityTitle = createDetailTitle("Humidity");
        humidityLabel = createDetailValue("--%");
        detailsGrid.add(humidityTitle, 1, 0);
        detailsGrid.add(humidityLabel, 1, 1);
        
        // Wind
        Label windTitle = createDetailTitle("Wind Speed");
        windLabel = createDetailValue("-- mph");
        detailsGrid.add(windTitle, 0, 2);
        detailsGrid.add(windLabel, 0, 3);
        
        // Pressure
        Label pressureTitle = createDetailTitle("Pressure");
        pressureLabel = createDetailValue("-- hPa");
        detailsGrid.add(pressureTitle, 1, 2);
        detailsGrid.add(pressureLabel, 1, 3);
        
        this.getChildren().add(detailsGrid);
    }
    
    /**
     * Build footer with last updated timestamp
     */
    private void buildFooter() {
        lastUpdatedLabel = new Label("Last updated: --");
        lastUpdatedLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        lastUpdatedLabel.setStyle("-fx-text-fill: #666; -fx-font-weight: bold;");
        lastUpdatedLabel.setAlignment(Pos.CENTER);
        lastUpdatedLabel.setMaxWidth(Double.MAX_VALUE);
        
        this.getChildren().add(lastUpdatedLabel);
    }
    
    /**
     * Helper method to create detail title labels
     */
    private Label createDetailTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", 12));
        label.setStyle("-fx-text-fill: #999;");
        return label;
    }
    
    /**
     * Helper method to create detail value labels
     */
    private Label createDetailValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 18));
        label.setStyle("-fx-text-fill: #333;");
        return label;
    }
    
    /**
     * Handle search button click with input validation
     */
    private void handleSearch() {
        String city = searchField.getText().trim();
        
        // Input validation
        if (city.isEmpty()) {
            showError("Please enter a city name");
            return;
        }
        
        // Basic validation for city name format
        if (!city.matches("^[a-zA-Z\\s\\-'.]+$")) {
            showError("City name can only contain letters, spaces, hyphens, apostrophes, and periods");
            return;
        }
        
        // Trim and capitalize for better API compatibility
        String formattedCity = city.replaceAll("\\s+", " ");
        
        loadWeather(formattedCity);
        searchField.clear();

        // Trigger event to update other panels (forecasts)
        // This allows MainApp to listen and update forecasts
        this.fireEvent(new javafx.event.ActionEvent());
    }

    /**
     * Handle favorite button toggle
     */
    private void handleFavoriteToggle() {
        if (currentWeather != null) {
            String cityName = currentWeather.getCityName();
            if (favoritesService.isFavorite(cityName)) {
                favoritesService.removeFavorite(cityName);
                favoriteButton.setText("☆ Add to Favorites");
                favoriteButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                favoritesService.addFavorite(cityName);
                favoriteButton.setText("★ Remove from Favorites");
                favoriteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-cursor: hand;");
            }
            
            // Notify listeners that favorites changed
            if (onFavoritesChangeCallback != null) {
                onFavoritesChangeCallback.run();
            }
        }
    }

    /**
     * Update favorite button state based on current city
     */
    private void updateFavoriteButtonState(String cityName) {
        if (favoriteButton != null && cityName != null) {
            if (favoritesService.isFavorite(cityName)) {
                favoriteButton.setText("★ Remove from Favorites");
                favoriteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                favoriteButton.setText("☆ Add to Favorites");
                favoriteButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                                       "-fx-font-weight: bold; -fx-cursor: hand;");
            }
        }
    }
    
    /**
     * Load weather data for a city
     */
    private void loadWeather(String cityName) {
        // Show loading indicator
        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            searchButton.setDisable(true);
        });
        
        // Fetch weather data asynchronously
        weatherService.getCurrentWeatherAsync(cityName)
            .thenAccept(weather -> {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    searchButton.setDisable(false);
                    
                    if (weather != null) {
                        currentWeather = weather;
                        updateDisplay(weather);
                    } else {
                        showError("Could not fetch weather for: " + cityName);
                    }
                });
            });
    }
    
    /**
     * Update UI with weather data
     */
    private void updateDisplay(Weather weather) {
        // Update city
        cityLabel.setText(weather.getFullLocation());
        
        // Update temperature
        String tempUnit = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "°F" : "°C";
        temperatureLabel.setText(String.format("%.0f%s", weather.getTemperature(), tempUnit));
        
        // Update condition
        conditionLabel.setText(weather.getCondition());
        descriptionLabel.setText(capitalize(weather.getDescription()));
        
        // Update icon
        weatherIcon.setText(getWeatherEmoji(weather.getCondition()));
        
        // Update details
        feelsLikeLabel.setText(String.format("%.0f%s", weather.getFeelsLike(), tempUnit));
        humidityLabel.setText(weather.getHumidity() + "%");
        
        String windUnit = AppConfig.TEMPERATURE_UNIT.equals("imperial") ? "mph" : "m/s";
        windLabel.setText(String.format("%.1f %s %s", 
            weather.getWindSpeed(), windUnit, weather.getWindDirectionCompass()));
        
        pressureLabel.setText(weather.getPressure() + " hPa");
        
        // Update timestamp
        String timestamp = formatTimestamp(weather.getTimestamp());
        lastUpdatedLabel.setText("Last updated: " + timestamp);

        // Update favorite button state
        updateFavoriteButtonState(weather.getCityName());

        // Notify listeners that city changed
        if (onCityChangeCallback != null) {
            onCityChangeCallback.accept(weather.getCityName());
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Weather Data Error");
        alert.setContentText(message);
        alert.showAndWait();
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
    
    /**
     * Capitalize first letter of each word
     */
    private String capitalize(String text) {
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
    
    /**
     * Format Unix timestamp to readable string with time of day
     */
    private String formatTimestamp(long timestamp) {
        return DateTimeUtil.formatDateTimeWithOfDay(timestamp);
    }
    
    /**
     * Get current weather data
     */
    public Weather getCurrentWeather() {
        return currentWeather;
    }
    
    /**
     * Refresh current weather
     */
    public void refresh() {
        if (currentWeather != null) {
            loadWeather(currentWeather.getCityName());
        }
    }

    /**
     * Load weather for a specific city (public method for external calls)
     *
     * @param cityName Name of the city to load
     */
    public void loadCityWeather(String cityName) {
        loadWeather(cityName);
    }
}

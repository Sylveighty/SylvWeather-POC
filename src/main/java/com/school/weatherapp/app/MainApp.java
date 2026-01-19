package com.school.weatherapp.app;

import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.ui.panels.AlertPanel;
import com.school.weatherapp.ui.panels.CurrentWeatherPanel;
import com.school.weatherapp.ui.panels.DailyForecastPanel;
import com.school.weatherapp.ui.panels.FavoritesPanel;
import com.school.weatherapp.ui.panels.HourlyForecastPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * MainApp - JavaFX entry point for SylvWeather-POC.
 *
 * Responsibilities:
 * - Create the primary Stage/Scene
 * - Compose the UI from modular panels
 * - Wire cross-panel interactions (city change, favorites selection)
 * - Manage global UI state (theme + temperature unit toggle)
 *
 * Proof-of-concept note: the goal is clarity and structure, not production completeness.
 */
public class MainApp extends Application {

    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final String APP_TITLE = "Weather Dashboard (POC)";

    // Stylesheet resources
    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    private BorderPane root;
    private ScrollPane scrollPane;
    private Scene scene;

    // Panels
    private CurrentWeatherPanel currentWeatherPanel;
    private FavoritesPanel favoritesPanel;
    private HourlyForecastPanel hourlyForecastPanel;
    private DailyForecastPanel dailyForecastPanel;
    private AlertPanel alertPanel;

    // Global state
    private boolean darkThemeEnabled = false;

    // Your project already uses AppConfig.TEMPERATURE_UNIT as the default preference.
    // The toggle overrides display for the session via refreshTemperatures(...) calls.
    private boolean isImperial = "imperial".equalsIgnoreCase(AppConfig.TEMPERATURE_UNIT);

    @Override
    public void start(Stage primaryStage) {
        buildUi(primaryStage);
        wireInteractions();
        loadInitialData();
    }

    private void buildUi(Stage stage) {
        root = new BorderPane();
        root.getStyleClass().add("main-container");

        // Top controls
        root.setTop(createTopBar());

        // Panels
        initializePanels();

        // Layout composition
        VBox mainLayout = createMainLayout();

        scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);

        // Keep scroll pane visually clean; colors come from CSS
        scrollPane.getStyleClass().add("scroll-pane");

        root.setCenter(scrollPane);

        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Attach stylesheets (both), then apply initial theme order
        attachStylesheets(scene);
        applyTheme(darkThemeEnabled);

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.show();
    }

    private void attachStylesheets(Scene scene) {
        String lightUrl = getClass().getResource(THEME_LIGHT) != null
            ? getClass().getResource(THEME_LIGHT).toExternalForm()
            : null;

        String darkUrl = getClass().getResource(THEME_DARK) != null
            ? getClass().getResource(THEME_DARK).toExternalForm()
            : null;

        if (lightUrl == null || darkUrl == null) {
            // If CSS is missing, fail silently for POC (UI still runs with defaults).
            System.err.println("Warning: theme CSS resources not found.");
            return;
        }

        // Add both; ordering is controlled in applyTheme(...)
        scene.getStylesheets().add(lightUrl);
        scene.getStylesheets().add(darkUrl);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(10, 20, 0, 0));

        Button unitToggle = createUnitToggleButton();
        Button themeToggle = createThemeToggleButton();

        topBar.getChildren().addAll(unitToggle, themeToggle);
        return topBar;
    }

    private Button createUnitToggleButton() {
        Button unitToggle = new Button(isImperial ? "°F" : "°C");
        unitToggle.getStyleClass().add("pill-button");
        // Keep this blue in both themes for clarity (optional). If you prefer, move to CSS.
        unitToggle.setStyle("-fx-background-color: #2196F3;");

        unitToggle.setOnAction(e -> {
            isImperial = !isImperial;
            unitToggle.setText(isImperial ? "°F" : "°C");

            showUnitChangeMessage(isImperial ? "Fahrenheit" : "Celsius");
            refreshAllTemperatures();
        });

        return unitToggle;
    }

    private Button createThemeToggleButton() {
        Button themeToggle = new Button(darkThemeEnabled ? "Light" : "Dark");
        themeToggle.getStyleClass().add("pill-button");
        // Color indicates action: if currently light, show dark button, etc.
        themeToggle.setStyle("-fx-background-color: #1976d2;");

        themeToggle.setOnAction(e -> {
            darkThemeEnabled = !darkThemeEnabled;
            applyTheme(darkThemeEnabled);

            themeToggle.setText(darkThemeEnabled ? "Light" : "Dark");
            // Change button color for visual feedback
            themeToggle.setStyle("-fx-background-color: " + (darkThemeEnabled ? "#FF9800" : "#1976d2") + ";");
        });

        return themeToggle;
    }

    private void initializePanels() {
        currentWeatherPanel = new CurrentWeatherPanel();
        favoritesPanel = new FavoritesPanel();
        hourlyForecastPanel = new HourlyForecastPanel();
        dailyForecastPanel = new DailyForecastPanel();
        alertPanel = new AlertPanel();
    }

    private VBox createMainLayout() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Top section: current + favorites
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.TOP_CENTER);

        HBox.setHgrow(currentWeatherPanel, Priority.ALWAYS);
        HBox.setHgrow(favoritesPanel, Priority.ALWAYS);

        topSection.getChildren().addAll(currentWeatherPanel, favoritesPanel);

        // Forecast/alert panels should stretch
        alertPanel.setMaxWidth(Double.MAX_VALUE);
        hourlyForecastPanel.setMaxWidth(Double.MAX_VALUE);
        dailyForecastPanel.setMaxWidth(Double.MAX_VALUE);

        mainLayout.getChildren().addAll(
            topSection,
            alertPanel,
            hourlyForecastPanel,
            dailyForecastPanel
        );

        return mainLayout;
    }

    private void wireInteractions() {
        currentWeatherPanel.setOnCityChange(this::loadForecasts);
        currentWeatherPanel.setOnFavoritesChange(() -> favoritesPanel.refreshFavorites());

        favoritesPanel.setOnCitySelect(cityName -> currentWeatherPanel.loadCityWeather(cityName));
    }

    private void loadInitialData() {
        loadForecasts(AppConfig.DEFAULT_CITY);
        refreshAllTemperatures();
    }

    /**
     * Theme switching strategy:
     * - Both stylesheets are present
     * - The last stylesheet wins if selectors are the same
     * - So we reorder stylesheets: (light, dark) for dark mode; (dark, light) for light mode
     */
    private void applyTheme(boolean dark) {
        if (scene == null) return;

        String lightUrl = getClass().getResource(THEME_LIGHT) != null
            ? getClass().getResource(THEME_LIGHT).toExternalForm()
            : null;

        String darkUrl = getClass().getResource(THEME_DARK) != null
            ? getClass().getResource(THEME_DARK).toExternalForm()
            : null;

        if (lightUrl == null || darkUrl == null) return;

        var stylesheets = scene.getStylesheets();

        // Remove and re-add in correct order
        stylesheets.remove(lightUrl);
        stylesheets.remove(darkUrl);

        if (dark) {
            stylesheets.add(lightUrl);
            stylesheets.add(darkUrl);
        } else {
            stylesheets.add(darkUrl);
            stylesheets.add(lightUrl);
        }

        // Panels may do additional per-theme adjustments; keep calls for compatibility.
        if (dark) {
            currentWeatherPanel.applyDarkTheme();
            favoritesPanel.applyDarkTheme();
            hourlyForecastPanel.applyDarkTheme();
            dailyForecastPanel.applyDarkTheme();
            alertPanel.applyDarkTheme();
        } else {
            currentWeatherPanel.applyLightTheme();
            favoritesPanel.applyLightTheme();
            hourlyForecastPanel.applyLightTheme();
            dailyForecastPanel.applyLightTheme();
            alertPanel.applyLightTheme();
        }
    }

    private void loadForecasts(String cityName) {
        hourlyForecastPanel.loadHourlyForecast(cityName);
        dailyForecastPanel.loadDailyForecast(cityName);
        alertPanel.loadAlerts(cityName);
    }

    private void refreshAllTemperatures() {
        currentWeatherPanel.refreshTemperatures(isImperial);
        hourlyForecastPanel.refreshTemperatures(isImperial);
        dailyForecastPanel.refreshTemperatures(isImperial);
    }

    private void showUnitChangeMessage(String newUnit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Unit Changed");
        alert.setHeaderText("Temperature Units Updated");
        alert.setContentText("Temperature units have been switched to " + newUnit + ".");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

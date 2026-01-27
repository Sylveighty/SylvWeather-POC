package com.school.weatherapp.app;

import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.features.FavoritesService;
import com.school.weatherapp.features.UserPreferencesService;
import com.school.weatherapp.ui.panels.AlertPanel;
import com.school.weatherapp.ui.panels.CurrentWeatherPanel;
import com.school.weatherapp.ui.panels.DailyForecastPanel;
import com.school.weatherapp.ui.panels.FavoritesPanel;
import com.school.weatherapp.ui.panels.HighlightsPanel;
import com.school.weatherapp.ui.panels.HourlyForecastPanel;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * MainApp - JavaFX entry point for Weathering with You.
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
    private static final String APP_TITLE = "Weathering with You";

    // Stylesheet resources.
    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    private BorderPane root;
    private ScrollPane scrollPane;
    private Scene scene;
    private Label toastLabel;

    // Panels.
    private CurrentWeatherPanel currentWeatherPanel;
    private FavoritesPanel favoritesPanel;
    private HighlightsPanel highlightsPanel;
    private HourlyForecastPanel hourlyForecastPanel;
    private DailyForecastPanel dailyForecastPanel;
    private AlertPanel alertPanel;

    // Global state.
    private boolean darkThemeEnabled = false;
    private FavoritesService favoritesService;
    private UserPreferencesService preferencesService;

    // Your project already uses AppConfig.TEMPERATURE_UNIT as the default preference.
    // The toggle overrides display for the session via refreshTemperatures(...) calls.
    private boolean isImperial = "imperial".equalsIgnoreCase(AppConfig.TEMPERATURE_UNIT);

    @Override
    public void start(Stage primaryStage) {
        preferencesService = new UserPreferencesService();
        initializePreferences();
        buildUi(primaryStage);
        wireInteractions();
        loadInitialData();
    }

    private void initializePreferences() {
        darkThemeEnabled = preferencesService.isDarkThemeEnabled();
        isImperial = "imperial".equalsIgnoreCase(preferencesService.getTemperatureUnit());
    }

    private void buildUi(Stage stage) {
        root = new BorderPane();
        root.getStyleClass().add("main-container");

        // Top controls.
        root.setTop(createTopBar());

        // Panels.
        initializePanels();

        // Layout composition.
        VBox mainLayout = createMainLayout();

        scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);

        // Keep scroll pane visually clean; colors come from CSS.
        scrollPane.getStyleClass().add("scroll-pane");

        root.setCenter(scrollPane);

        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Attach stylesheets (both), then apply initial theme order.
        attachStylesheets(scene);
        applyTheme(darkThemeEnabled);

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.show();
    }

    private void attachStylesheets(Scene scene) {
        String lightUrl = resolveStylesheetUrl(THEME_LIGHT);
        String darkUrl = resolveStylesheetUrl(THEME_DARK);

        if (lightUrl == null || darkUrl == null) {
            // If CSS is missing, fail silently for POC (UI still runs with defaults).
            System.err.println("Warning: theme CSS resources not found.");
            return;
        }

        // Add both; ordering is controlled in applyTheme(...).
        scene.getStylesheets().add(lightUrl);
        scene.getStylesheets().add(darkUrl);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(12);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 24, 8, 24));

        Label title = new Label(APP_TITLE);
        title.getStyleClass().add("title-text");

        toastLabel = new Label();
        toastLabel.getStyleClass().add("toast-banner");
        toastLabel.setVisible(false);
        HBox.setHgrow(toastLabel, Priority.ALWAYS);
        toastLabel.setMaxWidth(Double.MAX_VALUE);

        Button unitToggle = createUnitToggleButton();
        Button themeToggle = createThemeToggleButton();

        HBox actionButtons = new HBox(10, unitToggle, themeToggle);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        topBar.getChildren().addAll(title, toastLabel, actionButtons);
        return topBar;
    }

    private Button createUnitToggleButton() {
        Button unitToggle = new Button(isImperial ? "\u00B0F" : "\u00B0C");
        unitToggle.getStyleClass().add("pill-button");
        unitToggle.setAccessibleText("Toggle temperature unit");
        // Keep this blue in both themes for clarity (optional). If you prefer, move to CSS.
        unitToggle.setStyle("-fx-background-color: #2196F3;");

        unitToggle.setOnAction(e -> {
            isImperial = !isImperial;
            unitToggle.setText(isImperial ? "\u00B0F" : "\u00B0C");
            preferencesService.setTemperatureUnit(isImperial ? "imperial" : "metric");

            showUnitChangeMessage(isImperial ? "Fahrenheit" : "Celsius");
            refreshAllTemperatures();
        });

        return unitToggle;
    }

    private Button createThemeToggleButton() {
        Button themeToggle = new Button(darkThemeEnabled ? "Light" : "Dark");
        themeToggle.getStyleClass().add("pill-button");
        themeToggle.setAccessibleText("Toggle light or dark theme");
        // Color indicates action: if currently light, show dark button, etc.
        themeToggle.setStyle("-fx-background-color: #1976d2;");

        themeToggle.setOnAction(e -> {
            darkThemeEnabled = !darkThemeEnabled;
            applyTheme(darkThemeEnabled);
            preferencesService.setDarkThemeEnabled(darkThemeEnabled);

            themeToggle.setText(darkThemeEnabled ? "Light" : "Dark");
            // Change button color for visual feedback.
            themeToggle.setStyle("-fx-background-color: " + (darkThemeEnabled ? "#FF9800" : "#1976d2") + ";");
        });

        return themeToggle;
    }

    private void initializePanels() {
        favoritesService = new FavoritesService();
        currentWeatherPanel = new CurrentWeatherPanel(favoritesService);
        favoritesPanel = new FavoritesPanel(favoritesService, preferencesService);
        highlightsPanel = new HighlightsPanel();
        hourlyForecastPanel = new HourlyForecastPanel();
        dailyForecastPanel = new DailyForecastPanel();
        alertPanel = new AlertPanel();
    }

    private VBox createMainLayout() {
        VBox mainLayout = new VBox(12);
        mainLayout.setPadding(new Insets(12));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Top section: current + favorites.
        HBox topSection = new HBox(12);
        topSection.setAlignment(Pos.TOP_CENTER);
        topSection.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(topSection, Priority.ALWAYS);

        HBox.setHgrow(currentWeatherPanel, Priority.ALWAYS);
        HBox.setHgrow(favoritesPanel, Priority.ALWAYS);
        currentWeatherPanel.setMaxHeight(Double.MAX_VALUE);
        favoritesPanel.setMaxHeight(Double.MAX_VALUE);

        topSection.getChildren().addAll(currentWeatherPanel, favoritesPanel);

        highlightsPanel.setMaxWidth(Double.MAX_VALUE);
        alertPanel.setMaxWidth(Double.MAX_VALUE);
        hourlyForecastPanel.setMaxWidth(Double.MAX_VALUE);
        dailyForecastPanel.setMaxWidth(Double.MAX_VALUE);

        mainLayout.getChildren().addAll(
            topSection,
            highlightsPanel,
            alertPanel,
            hourlyForecastPanel,
            dailyForecastPanel
        );

        return mainLayout;
    }

    private void wireInteractions() {
        currentWeatherPanel.setOnCityChange(this::loadForecasts);
        currentWeatherPanel.setOnFavoritesChange(() -> {
            favoritesPanel.refreshFavorites();
        });

        favoritesPanel.setOnCitySelectCallback(cityName -> currentWeatherPanel.loadCityWeather(cityName));

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

        String lightUrl = resolveStylesheetUrl(THEME_LIGHT);
        String darkUrl = resolveStylesheetUrl(THEME_DARK);

        if (lightUrl == null || darkUrl == null) return;

        var stylesheets = scene.getStylesheets();

        // Remove and re-add in correct order.
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
            highlightsPanel.applyDarkTheme();
            hourlyForecastPanel.applyDarkTheme();
            dailyForecastPanel.applyDarkTheme();
            alertPanel.applyDarkTheme();
        } else {
            currentWeatherPanel.applyLightTheme();
            favoritesPanel.applyLightTheme();
            highlightsPanel.applyLightTheme();
            hourlyForecastPanel.applyLightTheme();
            dailyForecastPanel.applyLightTheme();
            alertPanel.applyLightTheme();
        }
    }

    private void loadForecasts(String cityName) {
        highlightsPanel.loadHighlights(cityName);
        hourlyForecastPanel.loadHourlyForecast(cityName);
        dailyForecastPanel.loadDailyForecast(cityName);
        alertPanel.loadAlerts(cityName);
    }

    private String resolveStylesheetUrl(String resourcePath) {
        return getClass().getResource(resourcePath) != null
            ? getClass().getResource(resourcePath).toExternalForm()
            : null;
    }

    private void refreshAllTemperatures() {
        currentWeatherPanel.refreshTemperatures(isImperial);
        highlightsPanel.refreshTemperatures(isImperial);
        hourlyForecastPanel.refreshTemperatures(isImperial);
        dailyForecastPanel.refreshTemperatures(isImperial);
    }

    private void showUnitChangeMessage(String newUnit) {
        if (toastLabel == null) {
            return;
        }

        toastLabel.setText("Temperature units switched to " + newUnit + ".");
        toastLabel.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> toastLabel.setVisible(false));
        pause.playFromStart();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

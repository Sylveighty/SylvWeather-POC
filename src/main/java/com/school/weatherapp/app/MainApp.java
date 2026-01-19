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
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * MainApp - Entry point for the Weather Application
 * 
 * @author Weather App Team
 * @version 1.3 (Complete with all features)
 */
public class MainApp extends Application {
    
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final String APP_TITLE = "Weather Dashboard";
    
    private CurrentWeatherPanel currentWeatherPanel;
    private FavoritesPanel favoritesPanel;
    private HourlyForecastPanel hourlyForecastPanel;
    private DailyForecastPanel dailyForecastPanel;
    private AlertPanel alertPanel;
    private Scene scene;
    private BorderPane root;
    private ScrollPane scrollPane;
    private boolean darkThemeEnabled = false;
    
    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #e8eaf6;");
        
        // Top bar with theme toggle
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(10, 20, 0, 0));
        
        Button themeToggle = new Button("🌙 Dark");
        themeToggle.setStyle("-fx-font-size: 13; -fx-padding: 10 18; " +
                            "-fx-background-color: #1976d2; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 20;");
        themeToggle.setOnAction(e -> {
            toggleTheme();
            themeToggle.setText(darkThemeEnabled ? "☀ Light" : "🌙 Dark");
            themeToggle.setStyle("-fx-font-size: 13; -fx-padding: 10 18; " +
                                "-fx-background-color: " + (darkThemeEnabled ? "#FF9800" : "#1976d2") + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 20;");
        });
        
        topBar.getChildren().add(themeToggle);
        root.setTop(topBar);
        
        // Create panels
        currentWeatherPanel = new CurrentWeatherPanel();
        favoritesPanel = new FavoritesPanel();
        hourlyForecastPanel = new HourlyForecastPanel();
        dailyForecastPanel = new DailyForecastPanel();
        alertPanel = new AlertPanel();
        
        // Create layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        
        // Top section: Current weather (left) and favorites (right) - make them grow
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(currentWeatherPanel, Priority.ALWAYS);
        HBox.setHgrow(favoritesPanel, Priority.ALWAYS);
        topSection.getChildren().addAll(currentWeatherPanel, favoritesPanel);
        
        // Set max widths to fill space
        alertPanel.setMaxWidth(Double.MAX_VALUE);
        hourlyForecastPanel.setMaxWidth(Double.MAX_VALUE);
        dailyForecastPanel.setMaxWidth(Double.MAX_VALUE);
        
        // Add all sections
        mainLayout.getChildren().addAll(
            topSection, 
            alertPanel,
            hourlyForecastPanel, 
            dailyForecastPanel
        );
        
        // Scroll pane
        scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #e8eaf6;");
        
        root.setCenter(scrollPane);
        
        // Create scene
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Configure stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
        
        // Load initial data
        loadForecasts(AppConfig.DEFAULT_CITY);
        setupCityChangeListener();
        
        System.out.println("Weather App launched successfully!");
    }
    
    private void setupCityChangeListener() {
        currentWeatherPanel.setOnCityChange(cityName -> {
            loadForecasts(cityName);
        });

        currentWeatherPanel.setOnFavoritesChange(() -> {
            favoritesPanel.refreshFavorites();
        });

        favoritesPanel.setOnCitySelect(cityName -> {
            currentWeatherPanel.loadCityWeather(cityName);
        });
    }
    
    private void toggleTheme() {
        darkThemeEnabled = !darkThemeEnabled;

        if (darkThemeEnabled) {
            root.setStyle("-fx-background-color: #1a1a1a;");
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1a1a1a;");
            
            currentWeatherPanel.applyDarkTheme();
            favoritesPanel.applyDarkTheme();
            hourlyForecastPanel.applyDarkTheme();
            dailyForecastPanel.applyDarkTheme();
            alertPanel.applyDarkTheme();
        } else {
            root.setStyle("-fx-background-color: #e8eaf6;");
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #e8eaf6;");
            
            currentWeatherPanel.applyLightTheme();
            favoritesPanel.applyLightTheme();
            hourlyForecastPanel.applyLightTheme();
            dailyForecastPanel.applyLightTheme();
            alertPanel.applyLightTheme();
        }

        System.out.println("Theme: " + (darkThemeEnabled ? "Dark" : "Light"));
    }
    
    private void loadForecasts(String cityName) {
        hourlyForecastPanel.loadHourlyForecast(cityName);
        dailyForecastPanel.loadDailyForecast(cityName);
        alertPanel.loadAlerts(cityName);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

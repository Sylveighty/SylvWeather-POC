package com.school.weatherapp.app;

import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.ui.panels.AlertPanel;
import com.school.weatherapp.ui.panels.CurrentWeatherPanel;
import com.school.weatherapp.ui.panels.DailyForecastPanel;
import com.school.weatherapp.ui.panels.HourlyForecastPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * MainApp - Entry point for the Weather Application
 * 
 * This class launches the JavaFX application and sets up the primary stage.
 * Displays current weather, hourly forecast, and 7-day forecast panels.
 * 
 * @author Weather App Team
 * @version 1.2 (Phase 2)
 */
public class MainApp extends Application {
    
    // Window dimensions
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final String APP_TITLE = "Weather Dashboard";
    
    // UI Components
    private CurrentWeatherPanel currentWeatherPanel;
    private HourlyForecastPanel hourlyForecastPanel;
    private DailyForecastPanel dailyForecastPanel;
    private AlertPanel alertPanel;
    
    /**
     * JavaFX start method - called when application launches
     * 
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Create root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #e8eaf6;");
        
        // Create panels
        currentWeatherPanel = new CurrentWeatherPanel();
        hourlyForecastPanel = new HourlyForecastPanel();
        dailyForecastPanel = new DailyForecastPanel();
        alertPanel = new AlertPanel();
        
        // Create layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        
        // Top section: Current weather (left)
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.TOP_CENTER);
        topSection.getChildren().add(currentWeatherPanel);
        
        // Middle section: Alert panel
        alertPanel.setMaxWidth(1200);
        
        // Middle section: Hourly forecast
        hourlyForecastPanel.setMaxWidth(1200);
        
        // Bottom section: Daily forecast
        dailyForecastPanel.setMaxWidth(1200);
        
        // Add all sections to main layout
        mainLayout.getChildren().addAll(
            topSection, 
            alertPanel,
            hourlyForecastPanel, 
            dailyForecastPanel
        );
        
        // Wrap in scroll pane for smaller screens
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #e8eaf6;");
        
        root.setCenter(scrollPane);
        
        // Create scene
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Load theme CSS
        String themeCSS = getClass().getResource("/theme.css").toExternalForm();
        scene.getStylesheets().add(themeCSS);
        
        // Configure primary stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        
        // Show the window
        primaryStage.show();
        
        // Load initial forecast data
        loadForecasts(AppConfig.DEFAULT_CITY);
        
        // Set up listener for when current weather changes city
        setupCityChangeListener();
        
        System.out.println("Weather App launched successfully!");
        System.out.println("Phase 2: Forecast Panels loaded");
    }
    
    /**
     * Set up listener to update forecasts when city changes
     */
    private void setupCityChangeListener() {
        // Listen for city changes from current weather panel
        currentWeatherPanel.setOnCityChange(cityName -> {
            loadForecasts(cityName);
        });
    }
    
    /**
     * Load forecast data for a city
     * 
     * @param cityName Name of the city
     */
    private void loadForecasts(String cityName) {
        hourlyForecastPanel.loadHourlyForecast(cityName);
        dailyForecastPanel.loadDailyForecast(cityName);
        alertPanel.loadAlerts(cityName);
    }
    
    /**
     * Main entry point
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
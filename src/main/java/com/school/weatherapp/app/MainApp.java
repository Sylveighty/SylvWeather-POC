package com.school.weatherapp.app;

import com.school.weatherapp.ui.panels.CurrentWeatherPanel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * MainApp - Entry point for the Weather Application
 * 
 * This class launches the JavaFX application and sets up the primary stage.
 * Currently displays current weather panel (Phase 1).
 * 
 * @author Weather App Team
 * @version 1.1 (Phase 1)
 */
public class MainApp extends Application {
    
    // Window dimensions
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;
    private static final String APP_TITLE = "Weather Dashboard";
    
    // UI Components
    private CurrentWeatherPanel currentWeatherPanel;
    
    /**
     * JavaFX start method - called when application launches
     * 
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Create root layout (will hold all UI panels)
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #e8eaf6;");
        
        // Create current weather panel
        currentWeatherPanel = new CurrentWeatherPanel();
        
        // Create center container to hold the weather panel
        HBox centerContainer = new HBox();
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.setPadding(new Insets(20));
        centerContainer.getChildren().add(currentWeatherPanel);
        
        // Add to root layout
        root.setCenter(centerContainer);
        
        // Create scene with root layout
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Configure primary stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Show the window
        primaryStage.show();
        
        System.out.println("Weather App launched successfully!");
        System.out.println("Phase 1: Current Weather Panel loaded");
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
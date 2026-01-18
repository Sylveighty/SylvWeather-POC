package com.school.weatherapp.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * MainApp - Entry point for the Weather Application
 * 
 * This class launches the JavaFX application and sets up the primary stage.
 * Currently displays a basic window; features will be added in subsequent phases.
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 0)
 */
public class MainApp extends Application {
    
    // Window dimensions
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;
    private static final String APP_TITLE = "Weather Dashboard";
    
    /**
     * JavaFX start method - called when application launches
     * 
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        // Create root layout (will hold all UI panels)
        BorderPane root = new BorderPane();
        
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
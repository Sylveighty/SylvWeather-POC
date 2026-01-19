package com.school.weatherapp.ui.panels;

import com.school.weatherapp.features.FavoritesService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * FavoritesPanel - UI panel displaying and managing favorite cities
 *
 * This panel shows:
 * - List of favorite cities
 * - Buttons to select or remove favorites
 * - Empty state when no favorites exist
 *
 * @author Weather App Team
 * @version 1.1 (Theme Support)
 */
public class FavoritesPanel extends VBox {

    private final FavoritesService favoritesService;
    private Consumer<String> onCitySelectCallback;
    private VBox favoritesList;
    private Label headerLabel;
    private ScrollPane scrollPane;
    private boolean isDarkTheme = false;

    /**
     * Constructor - builds the UI panel
     */
    public FavoritesPanel() {
        this.favoritesService = new FavoritesService();

        // Panel styling - responsive sizing
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.setMinWidth(350);
        this.setPrefWidth(450);
        this.setMaxWidth(550);
        this.setPrefHeight(300);

        // Build UI
        buildHeader();
        buildFavoritesList();
        refreshFavorites();
        
        // Apply default theme
        applyLightTheme();
    }

    /**
     * Apply light theme colors
     */
    public void applyLightTheme() {
        isDarkTheme = false;
        this.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-text-fill: #333;");
        }
        
        if (favoritesList != null) {
            favoritesList.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        }
        
        if (scrollPane != null) {
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: white;");
        }
        
        // Refresh to update all city items
        refreshFavorites();
    }

    /**
     * Apply dark theme colors
     */
    public void applyDarkTheme() {
        isDarkTheme = true;
        this.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 10;");
        
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-text-fill: #e0e0e0;");
        }
        
        if (favoritesList != null) {
            favoritesList.setStyle("-fx-background-color: #333333; -fx-background-radius: 8;");
        }
        
        if (scrollPane != null) {
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #333333;");
        }
        
        // Refresh to update all city items
        refreshFavorites();
    }

    /**
     * Set callback to be notified when a favorite city is selected
     *
     * @param callback Function to call with selected city name
     */
    public void setOnCitySelect(Consumer<String> callback) {
        this.onCitySelectCallback = callback;
    }

    /**
     * Build header with title
     */
    private void buildHeader() {
        headerLabel = new Label("Favorite Cities");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        headerLabel.setAlignment(Pos.CENTER);
        headerLabel.setMaxWidth(Double.MAX_VALUE);

        this.getChildren().add(headerLabel);
    }

    /**
     * Build scrollable favorites list container
     */
    private void buildFavoritesList() {
        favoritesList = new VBox(10);
        favoritesList.setPadding(new Insets(10));

        scrollPane = new ScrollPane(favoritesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);

        this.getChildren().add(scrollPane);
    }

    /**
     * Refresh the favorites list display
     */
    public void refreshFavorites() {
        favoritesList.getChildren().clear();

        var favorites = favoritesService.getFavorites();

        if (favorites.isEmpty()) {
            showEmptyState();
        } else {
            showFavorites(favorites);
        }
    }

    /**
     * Show empty state when no favorites exist
     */
    private void showEmptyState() {
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(20));

        Label emptyLabel = new Label("No favorite cities yet");
        emptyLabel.setFont(Font.font("System", 14));
        emptyLabel.setStyle(isDarkTheme ? "-fx-text-fill: #b0b0b0;" : "-fx-text-fill: #999;");

        Label hintLabel = new Label("Search for a city and click the favorite button to add one!");
        hintLabel.setFont(Font.font("System", 12));
        hintLabel.setStyle(isDarkTheme ? "-fx-text-fill: #888;" : "-fx-text-fill: #bbb;");
        hintLabel.setWrapText(true);
        hintLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        emptyState.getChildren().addAll(emptyLabel, hintLabel);
        favoritesList.getChildren().add(emptyState);
    }

    /**
     * Show the list of favorite cities
     */
    private void showFavorites(java.util.List<String> favorites) {
        for (String city : favorites) {
            HBox cityItem = createCityItem(city);
            favoritesList.getChildren().add(cityItem);
        }
    }

    /**
     * Create a UI item for a favorite city
     */
    private HBox createCityItem(String cityName) {
        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(8, 12, 8, 12));
        
        // Theme-aware styling
        String boxBg = isDarkTheme ? "#3a3a3a" : "#f8f9fa";
        String boxBorder = isDarkTheme ? "#555555" : "#e9ecef";
        String textColor = isDarkTheme ? "#e0e0e0" : "#333";
        
        itemBox.setStyle("-fx-background-color: " + boxBg + "; -fx-background-radius: 6; " +
                        "-fx-border-color: " + boxBorder + "; -fx-border-radius: 6;");

        // City name label
        Label cityLabel = new Label(cityName);
        cityLabel.setFont(Font.font("System", 14));
        cityLabel.setStyle("-fx-text-fill: " + textColor + ";");
        cityLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cityLabel, Priority.ALWAYS);

        // Select button
        Button selectButton = new Button("View");
        selectButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                             "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 12;");
        selectButton.setOnAction(e -> handleCitySelect(cityName));

        // Remove button
        Button removeButton = new Button("✕");
        removeButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                             "-fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; " +
                             "-fx-min-width: 30px; -fx-max-width: 30px; -fx-padding: 6 12;");
        removeButton.setOnAction(e -> handleCityRemove(cityName));

        itemBox.getChildren().addAll(cityLabel, selectButton, removeButton);
        return itemBox;
    }

    /**
     * Handle city selection
     */
    private void handleCitySelect(String cityName) {
        if (onCitySelectCallback != null) {
            onCitySelectCallback.accept(cityName);
        }
    }

    /**
     * Handle city removal from favorites
     */
    private void handleCityRemove(String cityName) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Remove Favorite");
        confirmDialog.setHeaderText("Remove " + cityName + " from favorites?");
        confirmDialog.setContentText("This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                favoritesService.removeFavorite(cityName);
                refreshFavorites();
            }
        });
    }
}
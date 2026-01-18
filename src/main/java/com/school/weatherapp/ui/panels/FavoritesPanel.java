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
 * @version 1.0
 */
public class FavoritesPanel extends VBox {

    private final FavoritesService favoritesService;
    private Consumer<String> onCitySelectCallback;
    private VBox favoritesList;

    /**
     * Constructor - builds the UI panel
     */
    public FavoritesPanel() {
    this.favoritesService = new FavoritesService();

    this.setPadding(new Insets(20));
    this.setSpacing(15);
    this.setMaxWidth(400);
    this.setPrefHeight(300);

    buildHeader();
    buildFavoritesList();           // ← create favoritesList first
    refreshFavorites();

    applyLightTheme();              // ← now safe
}

    /**
     * Apply light theme colors
     */
    public void applyLightTheme() {
        this.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        
        // Update favorites list background
        if (favoritesList != null) {
            favoritesList.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        }
        
        // Update all city items in the list
        updateCityItemsTheme("#333", "#f8f9fa", "#e9ecef");
    }

    /**
     * Apply dark theme colors (Discord/VS Code style)
     */
    public void applyDarkTheme() {
        this.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 10;");
        
        // Update favorites list background
        if (favoritesList != null) {
            favoritesList.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8;");
        }
        
        // Update all city items in the list
        updateCityItemsTheme("#e0e0e0", "#2d2d30", "#3e3e42");
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
        Label headerLabel = new Label("Favorite Cities");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        headerLabel.setStyle("-fx-text-fill: #333;");
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
        favoritesList.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        ScrollPane scrollPane = new ScrollPane(favoritesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #f5f5f5;");

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
        emptyLabel.setStyle("-fx-text-fill: #999;");

        Label hintLabel = new Label("Search for a city and click the favorite button to add one!");
        hintLabel.setFont(Font.font("System", 12));
        hintLabel.setStyle("-fx-text-fill: #bbb;");
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
        itemBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; -fx-border-color: #e9ecef; -fx-border-radius: 6;");

        // City name label
        Label cityLabel = new Label(cityName);
        cityLabel.setFont(Font.font("System", 14));
        cityLabel.setStyle("-fx-text-fill: #333;");
        cityLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cityLabel, Priority.ALWAYS);

        // Select button
        Button selectButton = new Button("View");
        selectButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                             "-fx-font-size: 12px; -fx-cursor: hand;");
        selectButton.setOnAction(e -> handleCitySelect(cityName));

        // Remove button
        Button removeButton = new Button("✕");
        removeButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; " +
                             "-fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; " +
                             "-fx-min-width: 30px; -fx-max-width: 30px;");
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
    
    /**
     * Update all city items in the list with new theme colors
     */
    private void updateCityItemsTheme(String textColor, String backgroundColor, String borderColor) {
        for (javafx.scene.Node node : favoritesList.getChildren()) {
            if (node instanceof HBox cityItem) {
                // Update city item background and border
                cityItem.setStyle("-fx-background-color: " + backgroundColor + "; " +
                                 "-fx-background-radius: 6; " +
                                 "-fx-border-color: " + borderColor + "; " +
                                 "-fx-border-radius: 6;");
                
                // Update city label text color
                for (javafx.scene.Node child : cityItem.getChildren()) {
                    if (child instanceof Label label) {
                        label.setStyle("-fx-text-fill: " + textColor + ";");
                    }
                }
            }
        }
    }
}

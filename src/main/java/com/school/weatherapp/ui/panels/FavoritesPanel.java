package com.school.weatherapp.ui.panels;

import com.school.weatherapp.features.FavoritesService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.function.Consumer;

/**
 * FavoritesPanel - UI panel for displaying and managing favorite cities.
 *
 * Responsibilities:
 * - Display list of favorite cities
 * - Allow selecting a city (callback to MainApp)
 * - Allow removing a city from favorites
 *
 * Styling is handled primarily through theme.css / theme-dark.css.
 */
public class FavoritesPanel extends VBox {

    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    private final FavoritesService favoritesService;

    private Consumer<String> onCitySelectCallback;

    private VBox favoritesList;
    private Label headerLabel;
    private ScrollPane scrollPane;

    public FavoritesPanel() {
        this.favoritesService = new FavoritesService();

        setPadding(new Insets(20));
        setSpacing(15);
        setMinWidth(350);
        setPrefWidth(450);
        setMaxWidth(550);
        setPrefHeight(300);

        // Panel styling via CSS
        getStyleClass().add("panel-background");

        buildHeader();
        buildFavoritesList();
        refreshFavorites();

        // Default theme application (MainApp also controls stylesheet ordering)
        applyLightTheme();
    }

    // -------------------- Theme Methods --------------------

    public void applyLightTheme() {
        ensureStylesheetOrder(THEME_LIGHT, THEME_DARK);
    }

    public void applyDarkTheme() {
        ensureStylesheetOrder(THEME_DARK, THEME_LIGHT);
    }

    private void ensureStylesheetOrder(String primary, String secondary) {
        Scene scene = getScene();
        if (scene == null) return;

        String primaryUrl = getClass().getResource(primary) != null ? getClass().getResource(primary).toExternalForm() : null;
        String secondaryUrl = getClass().getResource(secondary) != null ? getClass().getResource(secondary).toExternalForm() : null;

        if (primaryUrl == null || secondaryUrl == null) return;

        var stylesheets = scene.getStylesheets();
        stylesheets.remove(primaryUrl);
        stylesheets.remove(secondaryUrl);
        stylesheets.add(secondaryUrl);
        stylesheets.add(primaryUrl);
    }

    // -------------------- Public API --------------------

    /**
     * Set callback to be notified when a favorite city is selected.
     */
    public void setOnCitySelect(Consumer<String> callback) {
        this.onCitySelectCallback = callback;
    }

    /**
     * Refresh the favorites list display.
     */
    public void refreshFavorites() {
        favoritesList.getChildren().clear();

        List<String> favorites = favoritesService.getFavorites();
        if (favorites.isEmpty()) {
            showEmptyState();
        } else {
            showFavorites(favorites);
        }
    }

    // -------------------- UI Build --------------------

    private void buildHeader() {
        headerLabel = new Label("Favorite Cities");
        headerLabel.setAlignment(Pos.CENTER);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.getStyleClass().add("section-title");

        getChildren().add(headerLabel);
    }

    private void buildFavoritesList() {
        favoritesList = new VBox(10);
        favoritesList.setPadding(new Insets(10));
        favoritesList.getStyleClass().add("panel-content");

        scrollPane = new ScrollPane(favoritesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.getStyleClass().add("scroll-pane");

        getChildren().add(scrollPane);
    }

    // -------------------- Rendering --------------------

    private void showEmptyState() {
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(20));

        Label emptyLabel = new Label("No favorite cities yet");
        emptyLabel.getStyleClass().add("label-secondary");

        Label hintLabel = new Label("Search for a city and click the favorite button to add one.");
        hintLabel.getStyleClass().add("label-subtle");
        hintLabel.setWrapText(true);
        hintLabel.setTextAlignment(TextAlignment.CENTER);
        hintLabel.setMaxWidth(260);

        emptyState.getChildren().addAll(emptyLabel, hintLabel);
        favoritesList.getChildren().add(emptyState);
    }

    private void showFavorites(List<String> favorites) {
        for (String city : favorites) {
            favoritesList.getChildren().add(createCityItem(city));
        }
    }

    private HBox createCityItem(String cityName) {
        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);

        // Reuse a generic card style from CSS
        itemBox.getStyleClass().add("forecast-card");

        Label cityLabel = new Label(cityName);
        cityLabel.getStyleClass().add("label-primary");
        cityLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cityLabel, Priority.ALWAYS);

        Button viewButton = new Button("View");
        // Uses default .button styling from CSS

        viewButton.setOnAction(e -> handleCitySelect(cityName));

        Button removeButton = new Button("✕");
        // Make it clearly destructive using existing CSS class
        removeButton.getStyleClass().add("favorite-remove");
        removeButton.setMinWidth(36);
        removeButton.setPrefWidth(36);

        removeButton.setOnAction(e -> handleCityRemove(cityName));

        itemBox.getChildren().addAll(cityLabel, viewButton, removeButton);
        return itemBox;
    }

    // -------------------- Actions --------------------

    private void handleCitySelect(String cityName) {
        if (onCitySelectCallback != null) {
            onCitySelectCallback.accept(cityName);
        }
    }

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

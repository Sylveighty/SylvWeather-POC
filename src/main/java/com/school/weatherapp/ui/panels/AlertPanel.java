package com.school.weatherapp.ui.panels;

import com.school.weatherapp.data.models.Alert;
import com.school.weatherapp.data.services.AlertService;
import com.school.weatherapp.util.DateTimeUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * AlertPanel - UI panel displaying weather alerts.
 *
 * Notes (POC):
 * - Alert availability can vary by location and API plan/endpoints.
 * - The service may return simulated alerts when live alerts are unavailable.
 *
 * Styling is handled via theme.css / theme-dark.css.
 */
public class AlertPanel extends VBox {

    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    private final AlertService alertService;

    private Label titleLabel;
    private VBox alertsContainer;
    private VBox containerBox;

    private ProgressIndicator loadingIndicator;

    public AlertPanel() {
        this.alertService = new AlertService();

        setPadding(new Insets(20));
        setSpacing(15);

        getStyleClass().add("panel-background");

        buildTitle();
        buildAlertsContainer();

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

    // -------------------- UI Build --------------------

    private void buildTitle() {
        titleLabel = new Label("Weather Alerts");
        titleLabel.getStyleClass().add("section-title");
        getChildren().add(titleLabel);
    }

    private void buildAlertsContainer() {
        containerBox = new VBox(10);
        containerBox.setPadding(new Insets(20));
        containerBox.getStyleClass().add("panel-content");

        alertsContainer = new VBox(10);
        alertsContainer.setAlignment(Pos.TOP_LEFT);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);

        containerBox.getChildren().add(alertsContainer);
        getChildren().add(containerBox);

        showLoading();
    }

    // -------------------- Data Load --------------------

    public void loadAlerts(String cityName) {
        showLoading();

        alertService.getAlertsAsync(cityName)
            .thenAccept(alerts -> Platform.runLater(() -> {
                if (alerts == null || alerts.isEmpty()) {
                    showNoAlerts();
                } else {
                    showAlerts(alerts);
                }
            }));
    }

    // -------------------- Rendering --------------------

    private void showLoading() {
        Platform.runLater(() -> {
            alertsContainer.getChildren().clear();

            VBox loadingBox = new VBox(10);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(10));

            Label loadingLabel = new Label("Loading...");
            loadingLabel.getStyleClass().add("label-subtle");

            loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
            alertsContainer.getChildren().add(loadingBox);
        });
    }

    private void showNoAlerts() {
        alertsContainer.getChildren().clear();

        Label none = new Label("No active alerts.");
        none.getStyleClass().add("label-subtle");

        alertsContainer.getChildren().add(none);
    }

    private void showAlerts(List<Alert> alerts) {
        alertsContainer.getChildren().clear();

        for (Alert alert : alerts) {
            alertsContainer.getChildren().add(createAlertCard(alert));
        }
    }

    private VBox createAlertCard(Alert alert) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("forecast-card");

        // Severity styling via CSS classes
        String severity = alert.getSeverity() != null ? alert.getSeverity().toLowerCase() : "low";
        switch (severity) {
            case "high" -> card.getStyleClass().add("alert-high");
            case "medium" -> card.getStyleClass().add("alert-medium");
            default -> card.getStyleClass().add("alert-low");
        }

        Label title = new Label(alert.getTitle() != null ? alert.getTitle() : "Alert");
        title.getStyleClass().add("label-primary");

        Label time = new Label("Issued: " + DateTimeUtil.formatDateTime(alert.getTimestamp()));
        time.getStyleClass().add("label-subtle");

        Label desc = new Label(alert.getDescription() != null ? alert.getDescription() : "");
        desc.getStyleClass().add("label-secondary");
        desc.setWrapText(true);

        card.getChildren().addAll(title, time, desc);
        return card;
    }
}

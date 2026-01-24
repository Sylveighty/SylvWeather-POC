package com.school.weatherapp.ui.panels;

import com.school.weatherapp.data.models.Alert;
import com.school.weatherapp.data.services.AlertService;
import com.school.weatherapp.util.DateTimeUtil;
import com.school.weatherapp.util.ThemeUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * AlertPanel - UI panel displaying weather alerts.
 *
 * POC note:
 * - Alerts availability can vary by location and OpenWeather plan/endpoints.
 * - The service may return simulated alerts when live alerts are unavailable.
 *
 * This panel displays which city the alerts correspond to and whether data is live or simulated.
 */
public class AlertPanel extends VBox {

    private static final String THEME_LIGHT = "/theme.css";
    private static final String THEME_DARK = "/theme-dark.css";

    private final AlertService alertService;

    private Label titleLabel;
    private Label contextLabel;     // Shows city + source.
    private VBox alertsContainer;
    private VBox containerBox;

    private ProgressIndicator loadingIndicator;

    // Track the most recent request city.
    private String currentCity = "";

    public AlertPanel() {
        this.alertService = new AlertService();

        setPadding(new Insets(20));
        setSpacing(8);

        getStyleClass().add("panel-background");

        buildHeader();
        buildAlertsContainer();

        applyLightTheme();
    }

    // -------------------- Theme Methods --------------------

    public void applyLightTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_LIGHT, THEME_DARK);
    }

    public void applyDarkTheme() {
        ThemeUtil.ensureStylesheetOrder(getScene(), getClass(), THEME_DARK, THEME_LIGHT);
    }

    // -------------------- UI Build --------------------

    private void buildHeader() {
        titleLabel = new Label("Weather Alerts");
        titleLabel.getStyleClass().add("section-title");

        contextLabel = new Label("No city selected");
        contextLabel.getStyleClass().add("label-subtle");

        getChildren().addAll(titleLabel, contextLabel);
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
        this.currentCity = (cityName == null || cityName.isBlank()) ? "" : cityName.trim();
        contextLabel.setText("Loading alerts for: " + (currentCity.isEmpty() ? "(unknown city)" : currentCity));

        showLoading();

        alertService.getAlertsAsync(cityName)
            .thenAccept(alerts -> Platform.runLater(() -> {
                if (alerts == null || alerts.isEmpty()) {
                    contextLabel.setText("Alerts for: " + safeCity() + " (none available)");
                    showNoAlerts();
                    return;
                }

                // Heuristic: if the service returns exactly the known simulated alert title,
                // label it as simulated. This avoids changing AlertService yet.
                boolean simulated = looksSimulated(alerts);

                contextLabel.setText("Alerts for: " + safeCity() + (simulated ? " (simulated fallback)" : " (live)"));
                showAlerts(alerts);
            }));
    }

    private String safeCity() {
        return currentCity.isEmpty() ? "(unknown city)" : currentCity;
    }

    private boolean looksSimulated(List<Alert> alerts) {
        // The simulated alert title in the current service is "Moderate Wind Advisory".
        // If you change the simulated data later, update this heuristic accordingly.
        if (alerts.size() != 1) return false;
        Alert a = alerts.get(0);
        if (a == null || a.getTitle() == null) return false;
        return "Moderate Wind Advisory".equalsIgnoreCase(a.getTitle().trim());
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

        // Severity styling via CSS classes.
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

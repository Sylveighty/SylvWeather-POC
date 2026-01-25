package com.school.weatherapp.ui.panels;

import com.school.weatherapp.data.models.Alert;
import com.school.weatherapp.data.models.AlertHistoryEntry;
import com.school.weatherapp.data.services.AlertHistoryService;
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
    private final AlertHistoryService alertHistoryService;

    private Label titleLabel;
    private Label contextLabel;     // Shows city + source.
    private Label currentAlertsLabel;
    private Label historyLabel;
    private VBox alertsContainer;
    private VBox historyContainer;
    private VBox containerBox;

    private ProgressIndicator loadingIndicator;

    // Track the most recent request city.
    private String currentCity = "";

    public AlertPanel() {
        this.alertService = new AlertService();
        this.alertHistoryService = new AlertHistoryService();

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

        currentAlertsLabel = new Label("Current Alerts");
        currentAlertsLabel.getStyleClass().add("label-secondary");

        alertsContainer = new VBox(10);
        alertsContainer.setAlignment(Pos.TOP_LEFT);

        historyLabel = new Label("Alert History");
        historyLabel.getStyleClass().add("label-secondary");

        historyContainer = new VBox(10);
        historyContainer.setAlignment(Pos.TOP_LEFT);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);

        containerBox.getChildren().addAll(currentAlertsLabel, alertsContainer, historyLabel, historyContainer);
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
                    alertHistoryService.record(currentCity, alerts);
                    updateHistory(alertHistoryService.getHistory(currentCity));
                    return;
                }

                // Heuristic: if the service returns exactly the known simulated alert title,
                // label it as simulated. This avoids changing AlertService yet.
                boolean simulated = looksSimulated(alerts);

                contextLabel.setText("Alerts for: " + safeCity() + (simulated ? " (simulated fallback)" : " (live)"));
                showAlerts(alerts);
                alertHistoryService.record(currentCity, alerts);
                updateHistory(alertHistoryService.getHistory(currentCity));
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
            historyContainer.getChildren().clear();

            VBox loadingBox = new VBox(10);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPadding(new Insets(10));

            Label loadingLabel = new Label("Loading...");
            loadingLabel.getStyleClass().add("label-subtle");

            loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
            alertsContainer.getChildren().add(loadingBox);

            Label historyLoading = new Label("Loading alert history...");
            historyLoading.getStyleClass().add("label-subtle");
            historyContainer.getChildren().add(historyLoading);
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

    private void updateHistory(List<AlertHistoryEntry> entries) {
        historyContainer.getChildren().clear();

        if (entries == null || entries.size() <= 1) {
            Label none = new Label("No previous alerts.");
            none.getStyleClass().add("label-subtle");
            historyContainer.getChildren().add(none);
            return;
        }

        for (int i = 1; i < entries.size(); i++) {
            historyContainer.getChildren().add(createHistoryCard(entries.get(i)));
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

        Label severity = new Label("Severity: " + formatSeverity(alert.getSeverity()));
        severity.getStyleClass().add("label-secondary");

        Label timeWindow = new Label(formatEffectiveWindow(alert));
        timeWindow.getStyleClass().add("label-subtle");

        Label time = new Label("Issued: " + DateTimeUtil.formatDateTime(alert.getTimestamp()));
        time.getStyleClass().add("label-subtle");

        Label desc = new Label(alert.getDescription() != null ? alert.getDescription() : "");
        desc.getStyleClass().add("label-secondary");
        desc.setWrapText(true);

        card.getChildren().addAll(title, severity, timeWindow, time, desc);
        return card;
    }

    private VBox createHistoryCard(AlertHistoryEntry entry) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("forecast-card");

        Label fetchedAt = new Label("Fetched: " + DateTimeUtil.formatDateTime(entry.getFetchedAt()));
        fetchedAt.getStyleClass().add("label-subtle");
        card.getChildren().add(fetchedAt);

        List<Alert> alerts = entry.getAlerts();
        if (alerts.isEmpty()) {
            Label none = new Label("No alerts at this time.");
            none.getStyleClass().add("label-secondary");
            card.getChildren().add(none);
            return card;
        }

        for (Alert alert : alerts) {
            String summary = "• " + buildAlertSummary(alert);
            Label summaryLabel = new Label(summary);
            summaryLabel.getStyleClass().add("label-secondary");
            summaryLabel.setWrapText(true);
            card.getChildren().add(summaryLabel);
        }

        return card;
    }

    private String buildAlertSummary(Alert alert) {
        String title = alert.getTitle() != null ? alert.getTitle() : "Alert";
        return String.format("%s (Severity: %s, %s)", title, formatSeverity(alert.getSeverity()), formatEffectiveWindow(alert));
    }

    private String formatSeverity(String severity) {
        if (severity == null || severity.isBlank()) {
            return "Unknown";
        }
        String trimmed = severity.trim().toLowerCase();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }

    private String formatEffectiveWindow(Alert alert) {
        long start = alert.getEffectiveStart();
        long end = alert.getEffectiveEnd();

        if (start <= 0 && end <= 0) {
            return "Effective window unavailable";
        }

        String startText = start > 0 ? DateTimeUtil.formatDateTime(start) : "Unknown start";
        String endText = end > 0 ? DateTimeUtil.formatDateTime(end) : "Until further notice";
        return "Effective: " + startText + " - " + endText;
    }
}

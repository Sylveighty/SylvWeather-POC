package com.school.weatherapp.ui.panels;

import com.school.weatherapp.data.models.Alert;
import com.school.weatherapp.data.services.AlertService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * AlertPanel - UI panel for displaying weather alerts
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 3)
 */
public class AlertPanel extends VBox {
    
    private AlertService alertService;
    private Label titleLabel;
    private ScrollPane alertsScrollPane;
    private VBox alertsContainer;
    private ProgressIndicator loadingIndicator;
    
    public AlertPanel() {
        this.alertService = new AlertService();
        
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        this.setMaxWidth(1200);
        
        buildTitle();
        buildAlertsContainer();
        showNoAlerts();
    }
    
    private void buildTitle() {
        titleLabel = new Label("⚠ Weather Alerts");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #d32f2f;");
        this.getChildren().add(titleLabel);
    }
    
    private void buildAlertsContainer() {
        alertsContainer = new VBox(12);
        alertsContainer.setStyle("-fx-background-color: white; " +
                                 "-fx-background-radius: 8; " +
                                 "-fx-padding: 20;");
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        
        alertsScrollPane = new ScrollPane(alertsContainer);
        alertsScrollPane.setFitToWidth(true);
        alertsScrollPane.setPrefHeight(200);
        alertsScrollPane.setStyle("-fx-control-inner-background: white;");
        
        this.getChildren().add(alertsScrollPane);
    }
    
    private void showNoAlerts() {
        alertsContainer.getChildren().clear();
        Label noAlertsLabel = new Label("No active weather alerts");
        noAlertsLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
        noAlertsLabel.setAlignment(Pos.CENTER);
        VBox placeholderBox = new VBox(noAlertsLabel);
        placeholderBox.setAlignment(Pos.CENTER);
        placeholderBox.setPrefHeight(150);
        alertsContainer.getChildren().add(placeholderBox);
    }
    
    public void loadAlerts(String cityName) {
        Platform.runLater(() -> {
            alertsContainer.getChildren().clear();
            VBox loadingBox = new VBox(loadingIndicator);
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.setPrefHeight(100);
            alertsContainer.getChildren().add(loadingBox);
        });
        
        alertService.getAlertsAsync(cityName).thenAccept(alerts -> {
            Platform.runLater(() -> {
                alertsContainer.getChildren().clear();
                
                if (alerts != null && !alerts.isEmpty()) {
                    displayAlerts(alerts);
                } else {
                    showNoAlerts();
                }
            });
        });
    }
    
    private void displayAlerts(List<Alert> alerts) {
        for (Alert alert : alerts) {
            VBox card = createAlertCard(alert);
            alertsContainer.getChildren().add(card);
        }
    }
    
    private VBox createAlertCard(Alert alert) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setSpacing(5);
        
        String severity = alert.getSeverity() != null ? alert.getSeverity() : "low";
        String bgColor, borderColor, textColor;
        
        switch (severity.toLowerCase()) {
            case "high":
                bgColor = "#ffebee";
                borderColor = "#d32f2f";
                textColor = "#b71c1c";
                break;
            case "medium":
                bgColor = "#fff3e0";
                borderColor = "#f57c00";
                textColor = "#e65100";
                break;
            default:
                bgColor = "#e3f2fd";
                borderColor = "#1976d2";
                textColor = "#0d47a1";
        }
        
        card.setStyle("-fx-background-color: " + bgColor + "; " +
                     "-fx-border-color: " + borderColor + "; " +
                     "-fx-border-width: 2; " +
                     "-fx-border-radius: 6; " +
                     "-fx-background-radius: 6;");
        
        Label titleLabel = new Label(alert.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: " + textColor + ";");
        titleLabel.setWrapText(true);
        
        Label descLabel = new Label(alert.getDescription());
        descLabel.setFont(Font.font("System", 12));
        descLabel.setStyle("-fx-text-fill: " + textColor + ";");
        descLabel.setWrapText(true);
        
        Label severityLabel = new Label("Severity: " + severity.toUpperCase());
        severityLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        severityLabel.setStyle("-fx-text-fill: " + textColor + ";");
        
        card.getChildren().addAll(titleLabel, descLabel, severityLabel);
        
        return card;
    }
}

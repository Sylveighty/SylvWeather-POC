package com.school.weatherapp.ui.panels;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * AlertPanel - UI panel for displaying weather alerts
 * 
 * Displays severe weather warnings and alerts as highlighted cards.
 * Prepared for Phase 3 implementation.
 * 
 * Features (Phase 3):
 * - Fetch alerts from API
 * - Display by severity level
 * - Clickable for details
 * - Real-time updates every 10-15 minutes
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 2 - Skeleton)
 */
public class AlertPanel extends VBox {
    
    private Label titleLabel;
    private ScrollPane alertsScrollPane;
    private VBox alertsContainer;
    
    /**
     * Constructor - builds the UI panel
     */
    public AlertPanel() {
        // Panel styling
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        this.setMaxWidth(1200);
        
        // Build UI
        buildTitle();
        buildAlertsContainer();
        
        // Show placeholder
        showNoAlerts();
    }
    
    /**
     * Build title section
     */
    private void buildTitle() {
        titleLabel = new Label("⚠ Weather Alerts");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #d32f2f;");
        
        this.getChildren().add(titleLabel);
    }
    
    /**
     * Build container for alert cards
     */
    private void buildAlertsContainer() {
        alertsContainer = new VBox(12);
        alertsContainer.setStyle("-fx-background-color: white; " +
                                 "-fx-background-radius: 8; " +
                                 "-fx-padding: 20;");
        
        alertsScrollPane = new ScrollPane(alertsContainer);
        alertsScrollPane.setFitToWidth(true);
        alertsScrollPane.setPrefHeight(200);
        alertsScrollPane.setStyle("-fx-control-inner-background: white;");
        
        this.getChildren().add(alertsScrollPane);
    }
    
    /**
     * Show placeholder when no alerts
     */
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
    
    /**
     * Placeholder method for Phase 3 - load alerts from API
     * 
     * @param cityName Name of the city
     */
    public void loadAlerts(String cityName) {
        // Phase 3: Implement alert loading from API
        // This will fetch from weather alert API and display alerts
    }
}

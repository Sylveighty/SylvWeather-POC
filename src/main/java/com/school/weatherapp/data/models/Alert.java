package com.school.weatherapp.data.models;

/**
 * Alert - Data model for weather alerts
 * 
 * @author Weather App Team
 * @version 1.0 (Phase 3)
 */
public class Alert {
    
    // ==================== Alert Properties ====================
    private String id;
    private String type;           // "storm", "wind", "flood", etc.
    private String title;
    private String description;
    private String severity;       // "low", "medium", "high".
    private long timestamp;
    
    // ==================== Constructors ====================
    
    /**
     * Default constructor
     */
    public Alert() {
    }
    
    // ==================== Getters and Setters ====================
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

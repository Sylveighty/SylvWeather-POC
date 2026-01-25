package com.school.weatherapp.data.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AlertHistoryEntry - Snapshot of alerts retrieved for a location.
 */
public class AlertHistoryEntry {

    private final String city;
    private final long fetchedAt;
    private final List<Alert> alerts;

    public AlertHistoryEntry(String city, long fetchedAt, List<Alert> alerts) {
        this.city = city;
        this.fetchedAt = fetchedAt;
        this.alerts = alerts == null ? Collections.emptyList() : new ArrayList<>(alerts);
    }

    public String getCity() {
        return city;
    }

    public long getFetchedAt() {
        return fetchedAt;
    }

    public List<Alert> getAlerts() {
        return Collections.unmodifiableList(alerts);
    }
}

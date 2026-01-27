package com.school.weatherapp.data.services;

import com.school.weatherapp.data.models.Alert;
import com.school.weatherapp.data.models.AlertHistoryEntry;
import com.school.weatherapp.util.DateTimeUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AlertHistoryService - Stores recent alert snapshots per location.
 */
public class AlertHistoryService {

    private static final int MAX_HISTORY_ENTRIES = 5;

    private final Map<String, Deque<AlertHistoryEntry>> historyByCity = new HashMap<>();

    public void record(String city, List<Alert> alerts) {
        if (city == null || city.isBlank()) {
            return;
        }

        String normalizedCity = city.trim().toLowerCase();
        Deque<AlertHistoryEntry> entries = historyByCity.computeIfAbsent(normalizedCity, key -> new ArrayDeque<>());
        entries.addFirst(new AlertHistoryEntry(city.trim(), DateTimeUtil.getCurrentTimestamp(), alerts));

        while (entries.size() > MAX_HISTORY_ENTRIES) {
            entries.removeLast();
        }
    }

    public List<AlertHistoryEntry> getHistory(String city) {
        if (city == null || city.isBlank()) {
            return List.of();
        }

        Deque<AlertHistoryEntry> entries = historyByCity.get(city.trim().toLowerCase());
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(entries);
    }
}

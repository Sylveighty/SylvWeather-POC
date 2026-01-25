package com.school.weatherapp.features;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.school.weatherapp.config.AppConfig;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * UserPreferencesService - Persists lightweight user preferences across sessions.
 *
 * Stores:
 * - Theme preference (dark/light)
 * - Temperature unit preference (imperial/metric)
 * - Recent search history
 */
public class UserPreferencesService {

    private static final String PREF_NODE = "com.school.weatherapp.preferences";
    private static final String KEY_THEME = "theme.dark";
    private static final String KEY_UNIT = "temperature.unit";
    private static final String KEY_RECENT_SEARCHES = "recent.searches";

    private final Preferences preferences;
    private final Gson gson;
    private final Type listType;

    public UserPreferencesService() {
        this.preferences = Preferences.userRoot().node(PREF_NODE);
        this.gson = new Gson();
        this.listType = new TypeToken<List<String>>() {}.getType();
    }

    public boolean isDarkThemeEnabled() {
        return preferences.getBoolean(KEY_THEME, false);
    }

    public void setDarkThemeEnabled(boolean enabled) {
        preferences.putBoolean(KEY_THEME, enabled);
    }

    public String getTemperatureUnit() {
        String unit = preferences.get(KEY_UNIT, AppConfig.TEMPERATURE_UNIT);
        if (!"imperial".equalsIgnoreCase(unit) && !"metric".equalsIgnoreCase(unit)) {
            return AppConfig.TEMPERATURE_UNIT;
        }
        return unit.toLowerCase();
    }

    public void setTemperatureUnit(String unit) {
        if (unit == null) {
            return;
        }
        String normalized = unit.trim().toLowerCase();
        if (!"imperial".equals(normalized) && !"metric".equals(normalized)) {
            return;
        }
        preferences.put(KEY_UNIT, normalized);
    }

    public List<String> getRecentSearches() {
        return new ArrayList<>(loadRecentSearches());
    }

    public void addRecentSearch(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return;
        }
        String normalized = cityName.trim();
        List<String> searches = loadRecentSearches();
        searches.removeIf(entry -> entry.equalsIgnoreCase(normalized));
        searches.add(0, normalized);

        if (searches.size() > AppConfig.RECENT_SEARCH_LIMIT) {
            searches = new ArrayList<>(searches.subList(0, AppConfig.RECENT_SEARCH_LIMIT));
        }

        preferences.put(KEY_RECENT_SEARCHES, gson.toJson(searches));
    }

    private List<String> loadRecentSearches() {
        String json = preferences.get(KEY_RECENT_SEARCHES, "");
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<String> entries = gson.fromJson(json, listType);
            return entries == null ? new ArrayList<>() : new ArrayList<>(entries);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}

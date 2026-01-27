package com.school.weatherapp.features;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.school.weatherapp.config.AppConfig;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * UserPreferencesService - Persists lightweight user preferences across sessions.
 *
 * Stores:
 * - Theme preference (dark/light)
 * - Temperature unit preference (imperial/metric)
 * - Recent search history
 * - Favorites grouping preferences (country vs custom country groups, and ordering)
 */
public class UserPreferencesService {

    private static final String PREF_NODE = "com.school.weatherapp.preferences";
    private static final String KEY_THEME = "theme.dark";
    private static final String KEY_UNIT = "temperature.unit";
    private static final String KEY_RECENT_SEARCHES = "recent.searches";

    // Favorites grouping preferences
    private static final String KEY_FAV_GROUPING_MODE = "favorites.grouping.mode";      // "country" | "custom"
    private static final String KEY_FAV_GROUP_ORDER_MODE = "favorites.group.order";    // "alphabetical" | "manual"
    private static final String KEY_FAV_MANUAL_COUNTRY_ORDER = "favorites.country.order.manual"; // JSON List<String>
    private static final String KEY_FAV_CUSTOM_GROUPS = "favorites.custom.groups";     // JSON List<CustomCountryGroup>
    private static final String KEY_FAV_CUSTOM_GROUP_ORDER = "favorites.custom.groups.order"; // JSON List<String>

    public enum FavoritesGroupingMode {
        COUNTRY, CUSTOM
    }

    public enum FavoritesGroupOrderMode {
        ALPHABETICAL, MANUAL
    }

    /**
     * CustomCountryGroup
     * A named group containing one or more country codes (e.g., "SEA" -> ["PH","SG","MY"]).
     *
     * FavoritesPanel uses this to display favorites in user-defined groups.
     */
    public static final class CustomCountryGroup {
        private String name;
        private List<String> countryCodes;

        // For Gson
        public CustomCountryGroup() {
            this("", new ArrayList<>());
        }

        public CustomCountryGroup(String name, List<String> countryCodes) {
            this.name = name == null ? "" : name.trim();
            this.countryCodes = countryCodes == null ? new ArrayList<>() : new ArrayList<>(countryCodes);
        }

        public String getName() {
            return name;
        }

        public List<String> getCountryCodes() {
            return countryCodes == null ? Collections.emptyList() : Collections.unmodifiableList(countryCodes);
        }

        public boolean isValid() {
            return name != null && !name.trim().isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CustomCountryGroup)) return false;
            CustomCountryGroup that = (CustomCountryGroup) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    private final Preferences preferences;
    private final Gson gson;

    private final Type listOfStringType;
    private final Type listOfCustomGroupType;

    public UserPreferencesService() {
        this.preferences = Preferences.userRoot().node(PREF_NODE);
        this.gson = new Gson();
        this.listOfStringType = new TypeToken<List<String>>() {}.getType();
        this.listOfCustomGroupType = new TypeToken<List<CustomCountryGroup>>() {}.getType();
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
        String json = preferences.get(KEY_RECENT_SEARCHES, "[]");
        try {
            List<String> parsed = gson.fromJson(json, listOfStringType);
            return parsed == null ? new ArrayList<>() : new ArrayList<>(parsed);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public void setRecentSearches(List<String> searches) {
        List<String> safe = searches == null ? new ArrayList<>() : new ArrayList<>(searches);
        preferences.put(KEY_RECENT_SEARCHES, gson.toJson(safe, listOfStringType));
    }

    // -------------------- Favorites Grouping Preferences --------------------

    public FavoritesGroupingMode getFavoritesGroupingMode() {
        String raw = preferences.get(KEY_FAV_GROUPING_MODE, "country");
        if ("custom".equalsIgnoreCase(raw)) {
            return FavoritesGroupingMode.CUSTOM;
        }
        return FavoritesGroupingMode.COUNTRY;
    }

    public void setFavoritesGroupingMode(FavoritesGroupingMode mode) {
        if (mode == null) {
            return;
        }
        preferences.put(KEY_FAV_GROUPING_MODE, mode == FavoritesGroupingMode.CUSTOM ? "custom" : "country");
    }

    public FavoritesGroupOrderMode getFavoritesGroupOrderMode() {
        String raw = preferences.get(KEY_FAV_GROUP_ORDER_MODE, "alphabetical");
        if ("manual".equalsIgnoreCase(raw)) {
            return FavoritesGroupOrderMode.MANUAL;
        }
        return FavoritesGroupOrderMode.ALPHABETICAL;
    }

    public void setFavoritesGroupOrderMode(FavoritesGroupOrderMode mode) {
        if (mode == null) {
            return;
        }
        preferences.put(KEY_FAV_GROUP_ORDER_MODE, mode == FavoritesGroupOrderMode.MANUAL ? "manual" : "alphabetical");
    }

    public List<String> getManualCountryGroupOrder() {
        String json = preferences.get(KEY_FAV_MANUAL_COUNTRY_ORDER, "[]");
        try {
            List<String> parsed = gson.fromJson(json, listOfStringType);
            return parsed == null ? new ArrayList<>() : new ArrayList<>(parsed);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public void setManualCountryGroupOrder(List<String> order) {
        List<String> safe = order == null ? new ArrayList<>() : new ArrayList<>(order);
        preferences.put(KEY_FAV_MANUAL_COUNTRY_ORDER, gson.toJson(safe, listOfStringType));
    }

    public List<CustomCountryGroup> getCustomCountryGroups() {
        String json = preferences.get(KEY_FAV_CUSTOM_GROUPS, "[]");
        try {
            List<CustomCountryGroup> parsed = gson.fromJson(json, listOfCustomGroupType);
            if (parsed == null) {
                return new ArrayList<>();
            }
            List<CustomCountryGroup> result = new ArrayList<>();
            for (CustomCountryGroup g : parsed) {
                if (g != null && g.isValid()) {
                    result.add(g);
                }
            }
            return result;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public void setCustomCountryGroups(List<CustomCountryGroup> groups) {
        List<CustomCountryGroup> safe = new ArrayList<>();
        if (groups != null) {
            for (CustomCountryGroup g : groups) {
                if (g != null && g.isValid()) {
                    safe.add(g);
                }
            }
        }
        preferences.put(KEY_FAV_CUSTOM_GROUPS, gson.toJson(safe, listOfCustomGroupType));
    }

    public List<String> getCustomGroupNameOrder() {
        String json = preferences.get(KEY_FAV_CUSTOM_GROUP_ORDER, "[]");
        try {
            List<String> parsed = gson.fromJson(json, listOfStringType);
            return parsed == null ? new ArrayList<>() : new ArrayList<>(parsed);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public void setCustomGroupNameOrder(List<String> order) {
        List<String> safe = order == null ? new ArrayList<>() : new ArrayList<>(order);
        preferences.put(KEY_FAV_CUSTOM_GROUP_ORDER, gson.toJson(safe, listOfStringType));
    }
}

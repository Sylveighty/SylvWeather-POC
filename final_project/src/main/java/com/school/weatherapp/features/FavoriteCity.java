package com.school.weatherapp.features;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * FavoriteCity - Represents a favorite city with an optional country code.
 */
public final class FavoriteCity {
    private final String cityName;
    private final String countryCode;

    public FavoriteCity(String cityName, String countryCode) {
        this.cityName = normalizeCityName(cityName);
        this.countryCode = normalizeCountryCode(countryCode);
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String toStorageString() {
        return cityName + "|" + countryCode;
    }

    public String toDisplayString() {
        if (countryCode.isBlank()) {
            return cityName;
        }
        return cityName + ", " + countryCode;
    }

    public String toSearchQuery() {
        return toDisplayString();
    }

    public static Optional<FavoriteCity> fromStorageLine(String line) {
        if (line == null) {
            return Optional.empty();
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        String[] parts = trimmed.split("\\|", 2);
        String city = parts[0].trim();
        if (city.isEmpty()) {
            return Optional.empty();
        }
        String country = parts.length > 1 ? parts[1].trim() : "";
        return Optional.of(new FavoriteCity(city, country));
    }

    private static String normalizeCityName(String cityName) {
        return cityName == null ? "" : cityName.trim();
    }

    private static String normalizeCountryCode(String countryCode) {
        if (countryCode == null) {
            return "";
        }
        String trimmed = countryCode.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FavoriteCity)) {
            return false;
        }
        FavoriteCity that = (FavoriteCity) o;
        return Objects.equals(cityName, that.cityName)
            && Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityName, countryCode);
    }
}

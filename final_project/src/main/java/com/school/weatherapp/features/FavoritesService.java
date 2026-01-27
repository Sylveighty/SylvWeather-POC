package com.school.weatherapp.features;

import com.school.weatherapp.config.AppConfig;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * FavoritesService - Manages favorite cities for the Weather Application
 *
 * This service handles:
 * - Loading favorite cities from persistent storage
 * - Saving favorite cities to persistent storage
 * - Adding and removing cities from favorites
 * - Checking if a city is already a favorite
 *
 * @author Weather App Team
 * @version 1.0
 */
public class FavoritesService {

    private Set<FavoriteCity> favoriteCities;
    private final String favoritesFilePath;

    /**
     * Constructor - initializes the service and loads existing favorites
     */
    public FavoritesService() {
        this.favoritesFilePath = AppConfig.FAVORITES_FILE_PATH;
        this.favoriteCities = new LinkedHashSet<>(); // Preserve order.
        loadFavorites();
    }

    /**
     * Load favorite cities from the file
     */
    private void loadFavorites() {
        try {
            File file = new File(favoritesFilePath);
            if (file.exists()) {
                List<String> lines = Files.readAllLines(Paths.get(favoritesFilePath));
                for (String line : lines) {
                    FavoriteCity.fromStorageLine(line)
                        .ifPresent(favoriteCities::add);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading favorites: " + e.getMessage());
        }
    }

    /**
     * Save favorite cities to the file
     */
    private void saveFavorites() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(favoritesFilePath))) {
            for (FavoriteCity city : favoriteCities) {
                writer.write(city.toStorageString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving favorites: " + e.getMessage());
        }
    }

    /**
     * Add a city to favorites
     *
     * @param cityName Name of the city to add
     * @return true if added successfully, false if already exists
     */
    public boolean addFavorite(String cityName) {
        return addFavorite(cityName, "");
    }

    /**
     * Remove a city from favorites
     *
     * @param cityName Name of the city to remove
     * @return true if removed successfully, false if not found
     */
    public boolean removeFavorite(String cityName) {
        return removeFavorite(cityName, "");
    }

    /**
     * Check if a city is in favorites
     *
     * @param cityName Name of the city to check
     * @return true if the city is a favorite
     */
    public boolean isFavorite(String cityName) {
        return isFavorite(cityName, "");
    }

    /**
     * Add a city with a country code to favorites
     *
     * @param cityName Name of the city to add
     * @param countryCode Country code for the city
     * @return true if added successfully, false if already exists
     */
    public boolean addFavorite(String cityName, String countryCode) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }

        FavoriteCity favoriteCity = new FavoriteCity(cityName, countryCode);
        if (favoriteCities.add(favoriteCity)) {
            saveFavorites();
            return true;
        }
        return false;
    }

    /**
     * Remove a city with a country code from favorites
     *
     * @param cityName Name of the city to remove
     * @param countryCode Country code for the city
     * @return true if removed successfully, false if not found
     */
    public boolean removeFavorite(String cityName, String countryCode) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }

        FavoriteCity favoriteCity = new FavoriteCity(cityName, countryCode);
        if (favoriteCities.remove(favoriteCity)) {
            saveFavorites();
            return true;
        }
        return false;
    }

    /**
     * Check if a city with a country code is in favorites
     *
     * @param cityName Name of the city to check
     * @param countryCode Country code for the city
     * @return true if the city is a favorite
     */
    public boolean isFavorite(String cityName, String countryCode) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }
        return favoriteCities.contains(new FavoriteCity(cityName, countryCode));
    }

    /**
     * Get all favorite cities
     *
     * @return List of favorite city names
     */
    public List<String> getFavorites() {
        List<String> favorites = new ArrayList<>();
        for (FavoriteCity city : favoriteCities) {
            favorites.add(city.getCityName());
        }
        return favorites;
    }

    /**
     * Get all favorite city entries
     *
     * @return List of favorite cities with country codes
     */
    public List<FavoriteCity> getFavoriteEntries() {
        return new ArrayList<>(favoriteCities);
    }

    /**
     * Get the number of favorite cities
     *
     * @return Number of favorites
     */
    public int getFavoritesCount() {
        return favoriteCities.size();
    }

    /**
     * Clear all favorites
     */
    public void clearFavorites() {
        favoriteCities.clear();
        saveFavorites();
    }
}

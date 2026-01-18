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

    private Set<String> favoriteCities;
    private final String favoritesFilePath;

    /**
     * Constructor - initializes the service and loads existing favorites
     */
    public FavoritesService() {
        this.favoritesFilePath = AppConfig.FAVORITES_FILE_PATH;
        this.favoriteCities = new LinkedHashSet<>(); // Preserve order
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
                    String city = line.trim();
                    if (!city.isEmpty()) {
                        favoriteCities.add(city);
                    }
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
            for (String city : favoriteCities) {
                writer.write(city);
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
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }

        String normalizedCity = cityName.trim();
        if (favoriteCities.add(normalizedCity)) {
            saveFavorites();
            return true;
        }
        return false;
    }

    /**
     * Remove a city from favorites
     *
     * @param cityName Name of the city to remove
     * @return true if removed successfully, false if not found
     */
    public boolean removeFavorite(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }

        String normalizedCity = cityName.trim();
        if (favoriteCities.remove(normalizedCity)) {
            saveFavorites();
            return true;
        }
        return false;
    }

    /**
     * Check if a city is in favorites
     *
     * @param cityName Name of the city to check
     * @return true if the city is a favorite
     */
    public boolean isFavorite(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }
        return favoriteCities.contains(cityName.trim());
    }

    /**
     * Get all favorite cities
     *
     * @return List of favorite city names
     */
    public List<String> getFavorites() {
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

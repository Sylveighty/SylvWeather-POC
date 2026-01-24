package com.school.weatherapp.util;

import javafx.scene.Scene;

/**
 * ThemeUtil - Helper for consistent stylesheet ordering.
 */
public final class ThemeUtil {

    // ==================== Constructors ====================

    /**
     * Private constructor to prevent instantiation.
     */
    private ThemeUtil() {
    }

    // ==================== Stylesheet Helpers ====================

    /**
     * Ensures both stylesheets are present and ordered so the primary one wins.
     *
     * @param scene Scene to update
     * @param resourceClass Class used to resolve resources
     * @param primary Primary stylesheet resource path
     * @param secondary Secondary stylesheet resource path
     */
    public static void ensureStylesheetOrder(Scene scene, Class<?> resourceClass, String primary, String secondary) {
        if (scene == null) {
            return;
        }

        String primaryUrl = resourceClass.getResource(primary) != null
            ? resourceClass.getResource(primary).toExternalForm()
            : null;
        String secondaryUrl = resourceClass.getResource(secondary) != null
            ? resourceClass.getResource(secondary).toExternalForm()
            : null;

        if (primaryUrl == null || secondaryUrl == null) {
            return;
        }

        var stylesheets = scene.getStylesheets();
        stylesheets.remove(primaryUrl);
        stylesheets.remove(secondaryUrl);
        stylesheets.add(secondaryUrl);
        stylesheets.add(primaryUrl);
    }
}

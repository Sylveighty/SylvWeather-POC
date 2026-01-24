package com.school.weatherapp.util;

/**
 * WeatherEmojiUtil - Maps weather conditions to emoji icons.
 */
public final class WeatherEmojiUtil {

    // ==================== Constructors ====================

    /**
     * Private constructor to prevent instantiation.
     */
    private WeatherEmojiUtil() {
    }

    // ==================== Public Methods ====================

    /**
     * Return an emoji representing the provided weather condition.
     *
     * @param condition Weather condition (e.g., "Clear", "Clouds")
     * @return Emoji for the condition, or a default icon if unknown
     */
    public static String emojiForCondition(String condition) {
        if (condition == null) {
            return "◐";
        }
        return switch (condition.toLowerCase()) {
            case "clear" -> "☀";
            case "clouds" -> "☁";
            case "rain" -> "⛈";
            case "drizzle" -> "☔";
            case "thunderstorm" -> "⚡";
            case "snow" -> "❄";
            case "mist", "fog" -> "≈";
            default -> "◐";
        };
    }
}

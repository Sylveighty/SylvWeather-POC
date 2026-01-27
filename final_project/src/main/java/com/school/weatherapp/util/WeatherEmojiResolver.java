package com.school.weatherapp.util;

/**
 * WeatherEmojiResolver - Maps OpenWeather condition/icon codes to emoji.
 */
public final class WeatherEmojiResolver {

    private static final String DEFAULT_EMOJI = "❓";

    private WeatherEmojiResolver() {
    }

    /**
     * Resolve a weather emoji using OpenWeather icon codes when available.
     *
     * @param iconCode  OpenWeather icon code (e.g., 01d, 04n)
     * @param condition Weather condition (e.g., "Clear", "Clouds")
     * @return Emoji representing the condition or a fallback icon
     */
    public static String resolveEmoji(String iconCode, String condition) {
        String iconEmoji = emojiForIconCode(iconCode);
        if (iconEmoji != null) {
            return iconEmoji;
        }
        String conditionEmoji = emojiForCondition(condition);
        return conditionEmoji != null ? conditionEmoji : DEFAULT_EMOJI;
    }

    private static String emojiForIconCode(String iconCode) {
        if (iconCode == null || iconCode.isBlank()) {
            return null;
        }
        return switch (iconCode.toLowerCase()) {
            case "01d" -> "☀";
            case "01n" -> "🌙";
            case "02d" -> "⛅";
            case "02n" -> "☁";
            case "03d", "03n" -> "☁";
            case "04d", "04n" -> "☁";
            case "09d", "09n" -> "🌧";
            case "10d" -> "🌦";
            case "10n" -> "🌧";
            case "11d", "11n" -> "⛈";
            case "13d", "13n" -> "❄";
            case "50d", "50n" -> "🌫";
            default -> null;
        };
    }

    private static String emojiForCondition(String condition) {
        if (condition == null || condition.isBlank()) {
            return null;
        }
        return switch (condition.toLowerCase()) {
            case "clear" -> "☀";
            case "clouds" -> "☁";
            case "rain" -> "🌧";
            case "drizzle" -> "☔";
            case "thunderstorm" -> "⚡";
            case "snow" -> "❄";
            case "mist", "fog", "haze", "smoke", "dust", "sand", "ash" -> "🌫";
            default -> null;
        };
    }
}

package com.school.weatherapp.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * DateTimeUtil - Utility class for date and time formatting operations
 *
 * Provides methods for:
 * - Formatting Unix timestamps to readable strings
 * - Converting between different time formats
 * - Handling timezone conversions
 *
 * @author Weather App Team
 * @version 1.0 (Phase 5)
 */
public class DateTimeUtil {

    // ==================== DateTime Formatters ====================
    private static final DateTimeFormatter SHORT_DATE_TIME =
        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private static final DateTimeFormatter TIME_ONLY =
        DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter DATE_ONLY =
        DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private static final DateTimeFormatter DAY_OF_WEEK =
        DateTimeFormatter.ofPattern("EEEE");

    private static final DateTimeFormatter SHORT_DAY =
        DateTimeFormatter.ofPattern("EEE");

    // ==================== Constructors ====================
    
    /**
     * Private constructor to prevent instantiation.
     */
    private DateTimeUtil() {
        // Utility class.
    }

    // ==================== Public Methods ====================
    
    /**
     * Format Unix timestamp to readable date and time string
     *
     * @param timestamp Unix timestamp in seconds
     * @return Formatted string like "Jan 18, 2026 22:30"
     */
    public static String formatDateTime(long timestamp) {
        return formatTimestamp(timestamp, SHORT_DATE_TIME);
    }

    /**
     * Format Unix timestamp to time only string
     *
     * @param timestamp Unix timestamp in seconds
     * @return Formatted string like "22:30"
     */
    public static String formatTime(long timestamp) {
        return formatTimestamp(timestamp, TIME_ONLY);
    }

    /**
     * Format Unix timestamp to date only string
     *
     * @param timestamp Unix timestamp in seconds
     * @return Formatted string like "Jan 18, 2026"
     */
    public static String formatDate(long timestamp) {
        return formatTimestamp(timestamp, DATE_ONLY);
    }

    /**
     * Format Unix timestamp to day of week
     *
     * @param timestamp Unix timestamp in seconds
     * @return Formatted string like "Monday"
     */
    public static String formatDayOfWeek(long timestamp) {
        return formatTimestamp(timestamp, DAY_OF_WEEK);
    }

    /**
     * Format Unix timestamp to short day name
     *
     * @param timestamp Unix timestamp in seconds
     * @return Formatted string like "Mon"
     */
    public static String formatShortDay(long timestamp) {
        return formatTimestamp(timestamp, SHORT_DAY);
    }

    /**
     * Format Unix timestamp using custom formatter
     *
     * @param timestamp Unix timestamp in seconds
     * @param formatter DateTimeFormatter to use
     * @return Formatted string
     */
    public static String formatTimestamp(long timestamp, DateTimeFormatter formatter) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return formatter.format(dateTime);
    }

    /**
     * Get relative time description (e.g., "2 hours ago")
     *
     * @param timestamp Unix timestamp in seconds
     * @return Relative time string
     */
    public static String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis() / 1000;
        long diff = now - timestamp;

        if (diff < 60) {
            return "Just now";
        } else if (diff < 3600) {
            long minutes = diff / 60;
            return minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        } else if (diff < 86400) {
            long hours = diff / 3600;
            return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        } else {
            long days = diff / 86400;
            return days + " day" + (days != 1 ? "s" : "") + " ago";
        }
    }

    /**
     * Check if timestamp is today
     *
     * @param timestamp Unix timestamp in seconds
     * @return true if timestamp is today
     */
    public static boolean isToday(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();

        return dateTime.toLocalDate().equals(now.toLocalDate());
    }

    /**
     * Check if timestamp is tomorrow
     *
     * @param timestamp Unix timestamp in seconds
     * @return true if timestamp is tomorrow
     */
    public static boolean isTomorrow(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();

        return dateTime.toLocalDate().equals(now.toLocalDate().plusDays(1));
    }

    /**
     * Get current Unix timestamp
     *
     * @return Current Unix timestamp in seconds
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Get time of day classification (Morning, Noon, Afternoon, Evening, Night, Midnight)
     *
     * @param timestamp Unix timestamp in seconds
     * @return Time of day string
     */
    public static String getTimeOfDay(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        int hour = dateTime.getHour();

        if (hour >= 5 && hour < 10) {
            return "Morning";
        } else if (hour >= 10 && hour < 12) {
            return "Late Morning";
        } else if (hour >= 12 && hour < 14) {
            return "Noon";
        } else if (hour >= 14 && hour < 17) {
            return "Afternoon";
        } else if (hour >= 17 && hour < 20) {
            return "Evening";
        } else if (hour >= 20 && hour < 24) {
            return "Night";
        } else {
            return "Midnight";
        }
    }

    /**
     * Get time of day with emoji representation
     *
     * @param timestamp Unix timestamp in seconds
     * @return Time of day with emoji
     */
    public static String getTimeOfDayWithEmoji(long timestamp) {
        String timeOfDay = getTimeOfDay(timestamp);
        String emoji = switch (timeOfDay) {
            case "Morning" -> "🌅";
            case "Late Morning" -> "☀️";
            case "Noon" -> "☀️";
            case "Afternoon" -> "🌤️";
            case "Evening" -> "🌆";
            case "Night" -> "🌙";
            case "Midnight" -> "🌙";
            default -> "⏰";
        };
        return emoji + " " + timeOfDay;
    }

    /**
     * Get formatted time with time of day
     *
     * @param timestamp Unix timestamp in seconds
     * @return Formatted string like "22:30 (Night)"
     */
    public static String formatTimeWithOfDay(long timestamp) {
        String time = formatTime(timestamp);
        String timeOfDay = getTimeOfDay(timestamp);
        return time + " (" + timeOfDay + ")";
    }

    /**
     * Get full date and time with time of day
     *
     * @param timestamp Unix timestamp in seconds
     * @return Formatted string like "Jan 18, 2026 22:30 (Night)"
     */
    public static String formatDateTimeWithOfDay(long timestamp) {
        String dateTime = formatDateTime(timestamp);
        String timeOfDay = getTimeOfDay(timestamp);
        return dateTime + " (" + timeOfDay + ")";
    }
}

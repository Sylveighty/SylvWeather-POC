package com.school.weatherapp.util;

/**
 * TemperatureUtil - Utility class for temperature conversions and formatting
 *
 * Provides methods for:
 * - Converting between Celsius and Fahrenheit
 * - Formatting temperature values with units
 * - Temperature-related calculations
 *
 * @author Weather App Team
 * @version 1.0 (Phase 5)
 */
public class TemperatureUtil {

    // Conversion constants
    private static final double CELSIUS_TO_FAHRENHEIT_FACTOR = 9.0 / 5.0;
    private static final double FAHRENHEIT_TO_CELSIUS_FACTOR = 5.0 / 9.0;
    private static final double FAHRENHEIT_FREEZING_POINT = 32.0;

    /**
     * Temperature units enumeration
     */
    public enum Unit {
        CELSIUS("°C"),
        FAHRENHEIT("°F"),
        KELVIN("K");

        private final String symbol;

        Unit(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    private TemperatureUtil() {
        // Utility class
    }

    /**
     * Convert Celsius to Fahrenheit
     *
     * @param celsius Temperature in Celsius
     * @return Temperature in Fahrenheit
     */
    public static double celsiusToFahrenheit(double celsius) {
        return (celsius * CELSIUS_TO_FAHRENHEIT_FACTOR) + FAHRENHEIT_FREEZING_POINT;
    }

    /**
     * Convert Fahrenheit to Celsius
     *
     * @param fahrenheit Temperature in Fahrenheit
     * @return Temperature in Celsius
     */
    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - FAHRENHEIT_FREEZING_POINT) * FAHRENHEIT_TO_CELSIUS_FACTOR;
    }

    /**
     * Convert Celsius to Kelvin
     *
     * @param celsius Temperature in Celsius
     * @return Temperature in Kelvin
     */
    public static double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    /**
     * Convert Kelvin to Celsius
     *
     * @param kelvin Temperature in Kelvin
     * @return Temperature in Celsius
     */
    public static double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }

    /**
     * Convert Fahrenheit to Kelvin
     *
     * @param fahrenheit Temperature in Fahrenheit
     * @return Temperature in Kelvin
     */
    public static double fahrenheitToKelvin(double fahrenheit) {
        return celsiusToKelvin(fahrenheitToCelsius(fahrenheit));
    }

    /**
     * Convert Kelvin to Fahrenheit
     *
     * @param kelvin Temperature in Kelvin
     * @return Temperature in Fahrenheit
     */
    public static double kelvinToFahrenheit(double kelvin) {
        return celsiusToFahrenheit(kelvinToCelsius(kelvin));
    }

    /**
     * Format temperature with unit symbol
     *
     * @param temperature Temperature value
     * @param unit Temperature unit
     * @return Formatted string like "25°C" or "77°F"
     */
    public static String formatTemperature(double temperature, Unit unit) {
        return formatTemperature(temperature, unit, 0);
    }

    /**
     * Format temperature with unit symbol and decimal places
     *
     * @param temperature Temperature value
     * @param unit Temperature unit
     * @param decimalPlaces Number of decimal places (0-2)
     * @return Formatted string like "25°C" or "77.5°F"
     */
    public static String formatTemperature(double temperature, Unit unit, int decimalPlaces) {
        String formatString = switch (decimalPlaces) {
            case 0 -> "%.0f%s";
            case 1 -> "%.1f%s";
            case 2 -> "%.2f%s";
            default -> "%.0f%s";
        };

        return String.format(formatString, temperature, unit.getSymbol());
    }

    /**
     * Convert temperature between units and format
     *
     * @param temperature Input temperature value
     * @param fromUnit Input unit
     * @param toUnit Output unit
     * @return Formatted string in target unit
     */
    public static String convertAndFormat(double temperature, Unit fromUnit, Unit toUnit) {
        double converted = convertTemperature(temperature, fromUnit, toUnit);
        return formatTemperature(converted, toUnit);
    }

    /**
     * Convert temperature between any two units
     *
     * @param temperature Input temperature value
     * @param fromUnit Input unit
     * @param toUnit Output unit
     * @return Converted temperature value
     */
    public static double convertTemperature(double temperature, Unit fromUnit, Unit toUnit) {
        if (fromUnit == toUnit) {
            return temperature;
        }

        // Convert to Celsius first, then to target unit
        double celsius = switch (fromUnit) {
            case CELSIUS -> temperature;
            case FAHRENHEIT -> fahrenheitToCelsius(temperature);
            case KELVIN -> kelvinToCelsius(temperature);
        };

        return switch (toUnit) {
            case CELSIUS -> celsius;
            case FAHRENHEIT -> celsiusToFahrenheit(celsius);
            case KELVIN -> celsiusToKelvin(celsius);
        };
    }

    /**
     * Get temperature description based on value in Celsius
     *
     * @param celsius Temperature in Celsius
     * @return Description like "Hot", "Warm", "Cool", etc.
     */
    public static String getTemperatureDescription(double celsius) {
        if (celsius >= 30) {
            return "Hot";
        } else if (celsius >= 20) {
            return "Warm";
        } else if (celsius >= 10) {
            return "Mild";
        } else if (celsius >= 0) {
            return "Cool";
        } else if (celsius >= -10) {
            return "Cold";
        } else {
            return "Freezing";
        }
    }

    /**
     * Calculate wind chill temperature
     *
     * @param temperature Air temperature in Celsius
     * @param windSpeed Wind speed in m/s
     * @return Wind chill temperature in Celsius
     */
    public static double calculateWindChill(double temperature, double windSpeed) {
        // Convert wind speed to km/h for the formula
        double windSpeedKmh = windSpeed * 3.6;

        if (temperature > 10 || windSpeedKmh < 4.8) {
            return temperature; // Wind chill not applicable
        }

        // Wind chill formula: T_wc = 13.12 + 0.6215*T - 11.37*V^0.16 + 0.3965*T*V^0.16
        // Where T is temperature in Celsius, V is wind speed in km/h
        double windSpeedPower = Math.pow(windSpeedKmh, 0.16);
        return 13.12 + (0.6215 * temperature) - (11.37 * windSpeedPower) +
               (0.3965 * temperature * windSpeedPower);
    }

    /**
     * Calculate heat index (feels like temperature)
     *
     * @param temperature Air temperature in Celsius
     * @param humidity Relative humidity as percentage (0-100)
     * @return Heat index in Celsius
     */
    public static double calculateHeatIndex(double temperature, double humidity) {
        if (temperature < 27) {
            return temperature; // Heat index not applicable for cool temperatures
        }

        // Convert to Fahrenheit for the formula
        double tempF = celsiusToFahrenheit(temperature);

        // Heat index formula (simplified)
        double heatIndexF = -42.379 + (2.04901523 * tempF) + (10.14333127 * humidity) -
                           (0.22475541 * tempF * humidity) - (0.00683783 * tempF * tempF) -
                           (0.05481717 * humidity * humidity) + (0.00122874 * tempF * tempF * humidity) +
                           (0.00085282 * tempF * humidity * humidity) -
                           (0.00000199 * tempF * tempF * humidity * humidity);

        return fahrenheitToCelsius(heatIndexF);
    }
}

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WeatherApp {
    private static final String API_KEY = "f55978d8ae2181360e45c253d1e13d60";
    private static final String CURRENT_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String FAVORITES_FILE = "favorites.txt";
    private static final String CACHE_FILE = "weather_cache.txt";
    
    private static boolean useFahrenheit = false;
    private static Set<String> favorites = new HashSet<>();
    private static Map<String, CachedWeather> cache = new HashMap<>();
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        loadFavorites();
        loadCache();
        
        while (true) {
            displayMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    searchWeather(scanner);
                    break;
                case "2":
                    viewFavorites(scanner);
                    break;
                case "3":
                    toggleUnits();
                    break;
                case "4":
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
    
    private static void displayMenu() {
        System.out.println("\n=== Weather Tracker ===");
        System.out.println("1. Search weather by city");
        System.out.println("2. View favorites");
        System.out.println("3. Toggle units (" + (useFahrenheit ? "°F" : "°C") + ")");
        System.out.println("4. Exit");
    }
    
    private static void searchWeather(Scanner scanner) {
        System.out.print("\nEnter city name: ");
        String city = scanner.nextLine().trim();
        
        if (city.isEmpty()) {
            System.out.println("City name cannot be empty.");
            return;
        }
        
        fetchAndDisplayWeather(city, scanner);
    }
    
    private static void fetchAndDisplayWeather(String city, Scanner scanner) {
        int retries = 3;
        String weatherData = null;
        
        while (retries > 0) {
            try {
                weatherData = getWeatherData(city);
                break;
            } catch (Exception e) {
                retries--;
                if (retries > 0) {
                    System.out.println("Connection error. Retrying... (" + retries + " attempts left)");
                } else {
                    System.out.println("Failed to fetch weather data: " + e.getMessage());
                    
                    // Try to load from cache
                    CachedWeather cached = cache.get(city.toLowerCase());
                    if (cached != null && cached.isValid()) {
                        System.out.println("\n[Showing cached data from " + cached.getTimestamp() + "]");
                        displayWeather(cached.data, city);
                        return;
                    } else {
                        System.out.println("No cached data available.");
                        return;
                    }
                }
            }
        }
        
        if (weatherData != null) {
            cacheWeatherData(city, weatherData);
            displayWeather(weatherData, city);
            displayWeatherMenu(scanner, city, weatherData);
        }
    }
    
    private static void displayWeatherMenu(Scanner scanner, String city, String weatherData) {
        while (true) {
            System.out.println("\n--- Options ---");
            System.out.println("1. View 7-day forecast");
            System.out.println("2. Check weather alerts");
            System.out.println("3. " + (favorites.contains(city.toLowerCase()) ? "Remove from" : "Add to") + " favorites");
            System.out.println("4. Back to main menu");
            System.out.print("Choose: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    displayForecast(city);
                    break;
                case "2":
                    checkAlerts(weatherData, city);
                    break;
                case "3":
                    toggleFavorite(city);
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    
    private static String getWeatherData(String city) throws Exception {
        String encodedCity = java.net.URLEncoder.encode(city, "UTF-8");
        String urlString = CURRENT_WEATHER_URL + "?q=" + encodedCity + "&appid=" + API_KEY + "&units=metric";
        return makeAPICall(urlString);
    }
    
    private static String getForecastData(String city) throws Exception {
        String encodedCity = java.net.URLEncoder.encode(city, "UTF-8");
        String urlString = FORECAST_URL + "?q=" + encodedCity + "&appid=" + API_KEY + "&units=metric";
        return makeAPICall(urlString);
    }
    
    private static String makeAPICall(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("API error (Code: " + responseCode + ")");
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        return response.toString();
    }
    
    private static void displayWeather(String jsonData, String city) {
        try {
            String tempStr = extractValue(jsonData, "\"temp\":");
            String feelsLikeStr = extractValue(jsonData, "\"feels_like\":");
            double temp = tempStr.equals("N/A") ? 0 : Double.parseDouble(tempStr);
            double feelsLike = feelsLikeStr.equals("N/A") ? 0 : Double.parseDouble(feelsLikeStr);
            String humidity = extractValue(jsonData, "\"humidity\":");
            String description = extractValue(jsonData, "\"description\":\"");
            String windSpeed = extractValue(jsonData, "\"speed\":");
            String pressure = extractValue(jsonData, "\"pressure\":");
            String visibility = extractValue(jsonData, "\"visibility\":");
            String sunrise = extractValue(jsonData, "\"sunrise\":");
            String sunset = extractValue(jsonData, "\"sunset\":");
            
            System.out.println("\n=== Weather in " + city + " ===");
            System.out.println("Temperature: " + formatTemp(temp));
            System.out.println("Feels like: " + formatTemp(feelsLike));
            System.out.println("Conditions: " + capitalize(description));
            System.out.println("Humidity: " + humidity + "%");
            System.out.println("Wind Speed: " + windSpeed + " m/s");
            System.out.println("Pressure: " + pressure + " hPa");
            
            if (!visibility.equals("N/A")) {
                try {
                    double visKm = Double.parseDouble(visibility) / 1000;
                    System.out.println("Visibility: " + String.format("%.1f", visKm) + " km");
                } catch (NumberFormatException e) {
                    // Skip visibility if parsing fails
                }
            }
            
            if (!sunrise.equals("N/A") && !sunset.equals("N/A")) {
                try {
                    System.out.println("Sunrise: " + formatTime(Long.parseLong(sunrise)));
                    System.out.println("Sunset: " + formatTime(Long.parseLong(sunset)));
                } catch (NumberFormatException e) {
                    // Skip sun times if parsing fails
                }
            }
        } catch (Exception e) {
            System.out.println("Error displaying weather data: " + e.getMessage());
        }
    }
    
    private static void displayForecast(String city) {
        try {
            String forecastData = getForecastData(city);
            System.out.println("\n=== 7-Day Forecast for " + city + " ===");
            
            Map<String, List<ForecastEntry>> dailyForecasts = new TreeMap<>();
            
            // Parse forecast entries
            String listSection = forecastData.substring(forecastData.indexOf("\"list\":[") + 8);
            String[] entries = listSection.split("\\},\\{");
            
            for (String entry : entries) {
                if (entry.contains("\"dt\":")) {
                    String dt = extractValue(entry, "\"dt\":");
                    String temp = extractValue(entry, "\"temp\":");
                    String desc = extractValue(entry, "\"description\":\"");
                    
                    if (!dt.equals("N/A") && !temp.equals("N/A")) {
                        try {
                            long timestamp = Long.parseLong(dt);
                            double temperature = Double.parseDouble(temp);
                            
                            LocalDate date = Instant.ofEpochSecond(timestamp)
                                .atZone(ZoneId.systemDefault()).toLocalDate();
                            String dateKey = date.toString();
                            
                            dailyForecasts.putIfAbsent(dateKey, new ArrayList<>());
                            dailyForecasts.get(dateKey).add(
                                new ForecastEntry(timestamp, temperature, desc)
                            );
                        } catch (NumberFormatException e) {
                            // Skip this entry if parsing fails
                            continue;
                        }
                    }
                }
            }
            
            // Display daily summaries
            int count = 0;
            for (Map.Entry<String, List<ForecastEntry>> dayEntry : dailyForecasts.entrySet()) {
                if (count++ >= 7) break;
                
                List<ForecastEntry> forecasts = dayEntry.getValue();
                if (forecasts.isEmpty()) continue;
                
                double avgTemp = forecasts.stream()
                    .mapToDouble(f -> f.temp).average().orElse(0);
                double maxTemp = forecasts.stream()
                    .mapToDouble(f -> f.temp).max().orElse(0);
                double minTemp = forecasts.stream()
                    .mapToDouble(f -> f.temp).min().orElse(0);
                
                String commonDesc = forecasts.get(forecasts.size() / 2).description;
                
                LocalDate date = LocalDate.parse(dayEntry.getKey());
                System.out.printf("%s: %s | High: %s Low: %s Avg: %s%n",
                    date.format(DateTimeFormatter.ofPattern("EEE, MMM dd")),
                    capitalize(commonDesc),
                    formatTemp(maxTemp),
                    formatTemp(minTemp),
                    formatTemp(avgTemp));
            }
            
            if (dailyForecasts.isEmpty()) {
                System.out.println("No forecast data available.");
            }
        } catch (Exception e) {
            System.out.println("Error fetching forecast: " + e.getMessage());
        }
    }
    
    private static void checkAlerts(String weatherData, String city) {
        try {
            String tempStr = extractValue(weatherData, "\"temp\":");
            String windSpeedStr = extractValue(weatherData, "\"speed\":");
            String mainWeather = extractValue(weatherData, "\"main\":\"");
            
            double temp = tempStr.equals("N/A") ? 0 : Double.parseDouble(tempStr);
            double windSpeed = windSpeedStr.equals("N/A") ? 0 : Double.parseDouble(windSpeedStr);
            
            List<String> alerts = new ArrayList<>();
            
            if (temp > 35) {
                alerts.add("⚠️ HEAT ALERT: Extreme temperature detected");
            } else if (temp < -10) {
                alerts.add("❄️ COLD ALERT: Freezing conditions");
            }
            
            if (windSpeed > 15) {
                alerts.add("💨 WIND ALERT: High wind speeds");
            }
            
            if (mainWeather.toLowerCase().contains("thunderstorm")) {
                alerts.add("⛈️ STORM ALERT: Thunderstorm conditions");
            } else if (mainWeather.toLowerCase().contains("snow")) {
                alerts.add("🌨️ SNOW ALERT: Snow conditions");
            }
            
            System.out.println("\n=== Weather Alerts for " + city + " ===");
            if (alerts.isEmpty()) {
                System.out.println("✓ No weather alerts at this time.");
            } else {
                for (String alert : alerts) {
                    System.out.println(alert);
                }
            }
        } catch (Exception e) {
            System.out.println("Error checking alerts: " + e.getMessage());
        }
    }
    
    private static void toggleFavorite(String city) {
        String cityLower = city.toLowerCase();
        if (favorites.contains(cityLower)) {
            favorites.remove(cityLower);
            System.out.println("Removed from favorites.");
        } else {
            favorites.add(cityLower);
            System.out.println("Added to favorites.");
        }
        saveFavorites();
    }
    
    private static void viewFavorites(Scanner scanner) {
        if (favorites.isEmpty()) {
            System.out.println("\nNo favorite locations saved.");
            return;
        }
        
        System.out.println("\n=== Favorite Locations ===");
        List<String> favList = new ArrayList<>(favorites);
        for (int i = 0; i < favList.size(); i++) {
            System.out.println((i + 1) + ". " + capitalize(favList.get(i)));
        }
        
        System.out.print("\nSelect a location (0 to cancel): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= favList.size()) {
                fetchAndDisplayWeather(favList.get(choice - 1), scanner);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }
    
    private static void toggleUnits() {
        useFahrenheit = !useFahrenheit;
        System.out.println("Units changed to " + (useFahrenheit ? "Fahrenheit" : "Celsius"));
    }
    
    private static String formatTemp(double celsius) {
        if (useFahrenheit) {
            double fahrenheit = celsius * 9/5 + 32;
            return String.format("%.1f°F", fahrenheit);
        }
        return String.format("%.1f°C", celsius);
    }
    
    private static String formatTime(long timestamp) {
        return Instant.ofEpochSecond(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    private static String extractValue(String json, String key) {
        int startIndex = json.indexOf(key);
        if (startIndex == -1) return "N/A";
        
        startIndex += key.length();
        if (key.contains("\"description") || key.contains("\"main")) {
            int endIndex = json.indexOf("\"", startIndex);
            return endIndex != -1 ? json.substring(startIndex, endIndex) : "N/A";
        } else {
            // For numeric values, find the end (comma or closing brace)
            int commaIndex = json.indexOf(",", startIndex);
            int braceIndex = json.indexOf("}", startIndex);
            
            int endIndex;
            if (commaIndex == -1) {
                endIndex = braceIndex;
            } else if (braceIndex == -1) {
                endIndex = commaIndex;
            } else {
                endIndex = Math.min(commaIndex, braceIndex);
            }
            
            if (endIndex == -1) return "N/A";
            
            String value = json.substring(startIndex, endIndex).trim();
            return value.replace("\"", "");
        }
    }
    
    private static void loadFavorites() {
        try {
            Path path = Paths.get(FAVORITES_FILE);
            if (Files.exists(path)) {
                favorites = new HashSet<>(Files.readAllLines(path));
            }
        } catch (IOException e) {
            // Ignore, file might not exist yet
        }
    }
    
    private static void saveFavorites() {
        try {
            Files.write(Paths.get(FAVORITES_FILE), favorites);
        } catch (IOException e) {
            System.out.println("Error saving favorites: " + e.getMessage());
        }
    }
    
    private static void cacheWeatherData(String city, String data) {
        cache.put(city.toLowerCase(), new CachedWeather(data));
        saveCache();
    }
    
    private static void loadCache() {
        try {
            Path path = Paths.get(CACHE_FILE);
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                for (int i = 0; i < lines.size() - 1; i += 2) {
                    String city = lines.get(i);
                    String data = lines.get(i + 1);
                    cache.put(city, new CachedWeather(data));
                }
            }
        } catch (IOException e) {
            // Ignore
        }
    }
    
    private static void saveCache() {
        try {
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, CachedWeather> entry : cache.entrySet()) {
                lines.add(entry.getKey());
                lines.add(entry.getValue().data);
            }
            Files.write(Paths.get(CACHE_FILE), lines);
        } catch (IOException e) {
            // Ignore
        }
    }
    
    static class CachedWeather {
        String data;
        long timestamp;
        
        CachedWeather(String data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isValid() {
            long ageMinutes = (System.currentTimeMillis() - timestamp) / 60000;
            return ageMinutes < 30; // Cache valid for 30 minutes
        }
        
        String getTimestamp() {
            return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("hh:mm a"));
        }
    }
    
    static class ForecastEntry {
        long timestamp;
        double temp;
        String description;
        
        ForecastEntry(long timestamp, double temp, String description) {
            this.timestamp = timestamp;
            this.temp = temp;
            this.description = description;
        }
    }
}
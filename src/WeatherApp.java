import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherApp {
    // Replace with your actual API key
    private static final String API_KEY = "f55978d8ae2181360e45c253d1e13d60";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Weather Tracker ===");
        System.out.print("Enter city name: ");
        String city = scanner.nextLine();
        
        try {
            String weatherData = getWeatherData(city);
            displayWeather(weatherData, city);
        } catch (Exception e) {
            System.out.println("Error fetching weather data: " + e.getMessage());
        }
        
        scanner.close();
    }
    
    private static String getWeatherData(String city) throws Exception {
        String urlString = API_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("City not found or API error (Code: " + responseCode + ")");
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
        // Simple JSON parsing (without external libraries)
        String temp = extractValue(jsonData, "\"temp\":");
        String feelsLike = extractValue(jsonData, "\"feels_like\":");
        String humidity = extractValue(jsonData, "\"humidity\":");
        String description = extractValue(jsonData, "\"description\":\"");
        String windSpeed = extractValue(jsonData, "\"speed\":");
        
        System.out.println("\n=== Weather in " + city + " ===");
        System.out.println("Temperature: " + temp + "°C");
        System.out.println("Feels like: " + feelsLike + "°C");
        System.out.println("Conditions: " + description);
        System.out.println("Humidity: " + humidity + "%");
        System.out.println("Wind Speed: " + windSpeed + " m/s");
    }
    
    private static String extractValue(String json, String key) {
        int startIndex = json.indexOf(key);
        if (startIndex == -1) return "N/A";
        
        startIndex += key.length();
        if (key.contains("\"description")) {
            int endIndex = json.indexOf("\"", startIndex);
            return json.substring(startIndex, endIndex);
        } else {
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
            String value = json.substring(startIndex, endIndex).trim();
            // Remove quotes if present
            return value.replace("\"", "");
        }
    }
}
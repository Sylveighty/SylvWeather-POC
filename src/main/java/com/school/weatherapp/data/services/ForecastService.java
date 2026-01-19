package com.school.weatherapp.data.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.data.models.Forecast;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ForecastService - Fetches and transforms OpenWeather 5-day / 3-hour forecast data.
 *
 * Responsibilities:
 * - Call the /forecast endpoint
 * - Parse 3-hour interval entries into Forecast objects
 * - Provide:
 *   - hourly subset (next ~24 hours)
 *   - daily summaries (grouped by date with min/max temperatures)
 *
 * This is a POC: error handling is intentionally lightweight.
 */
public class ForecastService {

    private final HttpClient httpClient;

    public ForecastService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<List<Forecast>> getForecastAsync(String cityName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getForecast(cityName);
            } catch (Exception e) {
                System.err.println("Error fetching forecast: " + e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public List<Forecast> getForecast(String cityName) throws Exception {
        String apiUrl = buildForecastApiUrl(cityName);
        HttpResponse<String> response = sendHttpRequest(apiUrl);

        validateApiResponse(response);

        String units = "imperial".equalsIgnoreCase(AppConfig.TEMPERATURE_UNIT) ? "imperial" : "metric";
        return parseForecastResponse(response.body(), units);
    }

    /**
     * Hourly forecast view: returns the next 8 entries (3-hour steps ~ 24 hours).
     */
    public CompletableFuture<List<Forecast>> getHourlyForecastAsync(String cityName) {
        return getForecastAsync(cityName).thenApply(forecasts -> {
            if (forecasts.isEmpty()) return forecasts;
            int end = Math.min(8, forecasts.size());
            return new ArrayList<>(forecasts.subList(0, end));
        });
    }

    /**
     * Daily forecast view: groups by calendar date and produces daily min/max summaries.
     */
    public CompletableFuture<List<Forecast>> getDailyForecastAsync(String cityName) {
        return getForecastAsync(cityName).thenApply(this::groupForecastsByDate);
    }

    private String buildForecastApiUrl(String cityName) {
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String units = "imperial".equalsIgnoreCase(AppConfig.TEMPERATURE_UNIT) ? "imperial" : "metric";

        return String.format("%s/forecast?q=%s&units=%s&appid=%s",
            AppConfig.WEATHER_API_BASE_URL,
            encodedCity,
            units,
            AppConfig.WEATHER_API_KEY
        );
    }

    private HttpResponse<String> sendHttpRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void validateApiResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() != 200) {
            throw new Exception("API returned status code: " + response.statusCode());
        }
    }

    private List<Forecast> parseForecastResponse(String jsonResponse, String units) {
        List<Forecast> forecasts = new ArrayList<>();

        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray list = json.getAsJsonArray("list");
        if (list == null) return forecasts;

        for (int i = 0; i < list.size(); i++) {
            JsonObject item = list.get(i).getAsJsonObject();
            forecasts.add(createForecastFromJson(item, units));
        }

        // Ensure chronological order
        forecasts.sort(Comparator.comparingLong(Forecast::getTimestamp));
        return forecasts;
    }

    private Forecast createForecastFromJson(JsonObject item, String units) {
        Forecast forecast = new Forecast();

        parseTimestampData(item, forecast);
        parseMainForecastData(item, forecast, units);
        parseForecastCondition(item, forecast);
        parseForecastWind(item, forecast);
        parseForecastPrecipitation(item, forecast);

        return forecast;
    }

    private void parseTimestampData(JsonObject item, Forecast forecast) {
        long timestamp = item.get("dt").getAsLong();
        forecast.setTimestamp(timestamp);

        Instant instant = Instant.ofEpochSecond(timestamp);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE").withZone(ZoneId.systemDefault());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());

        forecast.setDayOfWeek(dayFormatter.format(instant));
        forecast.setTimeLabel(timeFormatter.format(instant));
    }

    private void parseMainForecastData(JsonObject item, Forecast forecast, String units) {
        JsonObject main = item.getAsJsonObject("main");
        forecast.setTemperature(main.get("temp").getAsDouble());
        forecast.setFeelsLike(main.get("feels_like").getAsDouble());
        forecast.setTempMin(main.get("temp_min").getAsDouble());
        forecast.setTempMax(main.get("temp_max").getAsDouble());
        forecast.setHumidity(main.get("humidity").getAsInt());
        forecast.setTemperatureUnit(units);
    }

    private void parseForecastCondition(JsonObject item, Forecast forecast) {
        JsonObject weather = item.getAsJsonArray("weather").get(0).getAsJsonObject();
        forecast.setCondition(weather.get("main").getAsString());
        forecast.setDescription(weather.get("description").getAsString());
        // Note: current code stores "id" as iconCode; kept as-is for compatibility with UI.
        forecast.setIconCode(weather.get("id").getAsString());
    }

    private void parseForecastWind(JsonObject item, Forecast forecast) {
        JsonObject wind = item.getAsJsonObject("wind");
        forecast.setWindSpeed(wind.get("speed").getAsDouble());
    }

    private void parseForecastPrecipitation(JsonObject item, Forecast forecast) {
        if (item.has("pop")) {
            int precipPercent = (int) (item.get("pop").getAsDouble() * 100);
            forecast.setPrecipitation(precipPercent);
        }
    }

    /**
     * Groups 3-hour entries by calendar date (LocalDate) to create stable daily summaries.
     * This avoids unpredictable ordering and avoids relying on day-of-week strings.
     */
    private List<Forecast> groupForecastsByDate(List<Forecast> forecasts) {
        Map<LocalDate, List<Forecast>> byDate = new LinkedHashMap<>();

        ZoneId zone = ZoneId.systemDefault();
        for (Forecast f : forecasts) {
            LocalDate date = Instant.ofEpochSecond(f.getTimestamp()).atZone(zone).toLocalDate();
            byDate.computeIfAbsent(date, k -> new ArrayList<>()).add(f);
        }

        List<Map.Entry<LocalDate, List<Forecast>>> entries = new ArrayList<>(byDate.entrySet());
        entries.sort(Map.Entry.comparingByKey());

        List<Forecast> dailyForecasts = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Forecast>> entry : entries) {
            List<Forecast> dayForecasts = entry.getValue();
            if (dayForecasts.isEmpty()) continue;

            Forecast daily = createDailyForecast(entry.getKey(), dayForecasts);
            dailyForecasts.add(daily);

            if (dailyForecasts.size() >= 7) break;
        }

        return dailyForecasts;
    }

    private Forecast createDailyForecast(LocalDate date, List<Forecast> dayForecasts) {
        // Day forecasts should be chronological
        dayForecasts.sort(Comparator.comparingLong(Forecast::getTimestamp));

        Forecast first = dayForecasts.get(0);
        Forecast daily = new Forecast();

        // Labels
        DateTimeFormatter dayLabel = DateTimeFormatter.ofPattern("EEE");
        daily.setDayOfWeek(dayLabel.format(date));
        daily.setTimeLabel(dayLabel.format(date));

        // Baseline properties
        daily.setTimestamp(first.getTimestamp());
        daily.setTemperatureUnit(first.getTemperatureUnit());

        // Min/max range
        double minTemp = dayForecasts.stream().mapToDouble(Forecast::getTempMin).min().orElse(first.getTempMin());
        double maxTemp = dayForecasts.stream().mapToDouble(Forecast::getTempMax).max().orElse(first.getTempMax());

        daily.setTempMin(minTemp);
        daily.setTempMax(maxTemp);
        daily.setTemperature((minTemp + maxTemp) / 2);

        // Pick a representative condition near the middle of the day list
        Forecast representative = dayForecasts.get(dayForecasts.size() / 2);
        daily.setCondition(representative.getCondition());
        daily.setDescription(representative.getDescription());
        daily.setIconCode(representative.getIconCode());

        // Average humidity and wind
        double avgHumidity = dayForecasts.stream().mapToInt(Forecast::getHumidity).average().orElse(first.getHumidity());
        daily.setHumidity((int) avgHumidity);

        double avgWind = dayForecasts.stream().mapToDouble(Forecast::getWindSpeed).average().orElse(first.getWindSpeed());
        daily.setWindSpeed(avgWind);

        return daily;
    }
}

package com.school.weatherapp.data.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.school.weatherapp.config.AppConfig;
import com.school.weatherapp.data.cache.CacheService;
import com.school.weatherapp.data.models.Forecast;
import com.school.weatherapp.features.UserPreferencesService;

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
    private final CacheService cacheService;
    private final UserPreferencesService preferencesService;

    public ForecastService() {
        this.httpClient = HttpClient.newHttpClient();
        this.cacheService = new CacheService();
        this.preferencesService = new UserPreferencesService();
    }

    public CompletableFuture<List<Forecast>> getForecastAsync(String cityName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getForecast(cityName);
            } catch (Exception e) {
                System.err.println("Error fetching forecast: " + e.getMessage());
                return cacheService.loadForecast();
            }
        });
    }

    public List<Forecast> getForecast(String cityName) throws Exception {
        try {
            String apiUrl = buildForecastApiUrl(cityName);
            HttpResponse<String> response = sendHttpRequest(apiUrl);

            validateApiResponse(response);

            String units = resolveUnits();
            List<Forecast> forecasts = parseForecastResponse(response.body(), units);
            markCached(forecasts, false);
            cacheService.saveForecast(cityName, forecasts);
            return forecasts;
        } catch (Exception ex) {
            List<Forecast> cached = cacheService.loadForecast();
            if (!cached.isEmpty()) {
                return cached;
            }
            throw ex;
        }
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
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Forecast> daily = getDailyForecast(cityName);
                if (!daily.isEmpty()) {
                    markCached(daily, false);
                    cacheService.saveDailyForecast(cityName, daily);
                    return daily;
                }
                List<Forecast> grouped = groupForecastsByDate(getForecast(cityName));
                if (!grouped.isEmpty()) {
                    cacheService.saveDailyForecast(cityName, grouped);
                }
                return grouped;
            } catch (Exception e) {
                System.err.println("Error fetching daily forecast: " + e.getMessage());
                List<Forecast> cachedDaily = cacheService.loadDailyForecast();
                if (!cachedDaily.isEmpty()) {
                    return cachedDaily;
                }
                List<Forecast> cachedHourly = cacheService.loadForecast();
                if (!cachedHourly.isEmpty()) {
                    return groupForecastsByDate(cachedHourly);
                }
                return new ArrayList<>();
            }
        });
    }

    private String buildForecastApiUrl(String cityName) {
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String units = resolveUnits();

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

        // Ensure chronological order.
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

        if (item.has("rain")) {
            JsonObject rain = item.getAsJsonObject("rain");
            if (rain.has("3h")) {
                forecast.setRainAmount(rain.get("3h").getAsDouble());
            }
        }

        if (item.has("snow")) {
            JsonObject snow = item.getAsJsonObject("snow");
            if (snow.has("3h")) {
                forecast.setSnowAmount(snow.get("3h").getAsDouble());
            }
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

            if (dailyForecasts.size() >= 10) break;
        }

        return dailyForecasts;
    }

    private Forecast createDailyForecast(LocalDate date, List<Forecast> dayForecasts) {
        // Day forecasts should be chronological.
        dayForecasts.sort(Comparator.comparingLong(Forecast::getTimestamp));

        Forecast first = dayForecasts.get(0);
        Forecast daily = new Forecast();

        // Labels.
        DateTimeFormatter dayLabel = DateTimeFormatter.ofPattern("EEE");
        daily.setDayOfWeek(dayLabel.format(date));
        daily.setTimeLabel(dayLabel.format(date));

        // Baseline properties.
        daily.setTimestamp(first.getTimestamp());
        daily.setTemperatureUnit(first.getTemperatureUnit());

        // Min/max range.
        double minTemp = dayForecasts.stream().mapToDouble(Forecast::getTempMin).min().orElse(first.getTempMin());
        double maxTemp = dayForecasts.stream().mapToDouble(Forecast::getTempMax).max().orElse(first.getTempMax());

        daily.setTempMin(minTemp);
        daily.setTempMax(maxTemp);
        daily.setTemperature((minTemp + maxTemp) / 2);

        // Pick a representative condition near the middle of the day list.
        Forecast representative = dayForecasts.get(dayForecasts.size() / 2);
        daily.setCondition(representative.getCondition());
        daily.setDescription(representative.getDescription());
        daily.setIconCode(representative.getIconCode());

        // Average humidity and wind.
        double avgHumidity = dayForecasts.stream().mapToInt(Forecast::getHumidity).average().orElse(first.getHumidity());
        daily.setHumidity((int) avgHumidity);

        double avgWind = dayForecasts.stream().mapToDouble(Forecast::getWindSpeed).average().orElse(first.getWindSpeed());
        daily.setWindSpeed(avgWind);

        int maxPop = dayForecasts.stream().mapToInt(Forecast::getPrecipitation).max().orElse(0);
        daily.setPrecipitation(maxPop);

        double totalRain = dayForecasts.stream().mapToDouble(Forecast::getRainAmount).sum();
        daily.setRainAmount(totalRain);

        double totalSnow = dayForecasts.stream().mapToDouble(Forecast::getSnowAmount).sum();
        daily.setSnowAmount(totalSnow);

        boolean cached = dayForecasts.stream().anyMatch(Forecast::isCached);
        daily.setCached(cached);

        return daily;
    }

    private List<Forecast> getDailyForecast(String cityName) throws Exception {
        String units = resolveUnits();
        double[] coords = fetchCoordinates(cityName);
        if (coords == null) {
            return new ArrayList<>();
        }

        String apiUrl = buildDailyForecastApiUrl(coords[0], coords[1], units);
        HttpResponse<String> response = sendHttpRequest(apiUrl);
        validateApiResponse(response);

        return parseDailyForecastResponse(response.body(), units);
    }

    private double[] fetchCoordinates(String cityName) throws Exception {
        String encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
        String units = resolveUnits();

        String url = String.format("%s/weather?q=%s&units=%s&appid=%s",
            AppConfig.WEATHER_API_BASE_URL,
            encodedCity,
            units,
            AppConfig.WEATHER_API_KEY
        );

        HttpResponse<String> response = sendHttpRequest(url);
        validateApiResponse(response);

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!json.has("coord")) {
            return null;
        }

        JsonObject coord = json.getAsJsonObject("coord");
        if (!coord.has("lat") || !coord.has("lon")) {
            return null;
        }

        return new double[]{coord.get("lat").getAsDouble(), coord.get("lon").getAsDouble()};
    }

    private String buildDailyForecastApiUrl(double lat, double lon, String units) {
        return String.format("%s/onecall?lat=%s&lon=%s&units=%s&exclude=minutely,hourly,alerts&appid=%s",
            AppConfig.WEATHER_API_BASE_URL,
            lat,
            lon,
            units,
            AppConfig.WEATHER_API_KEY
        );
    }

    private List<Forecast> parseDailyForecastResponse(String jsonResponse, String units) {
        List<Forecast> forecasts = new ArrayList<>();

        JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray dailyArray = json.getAsJsonArray("daily");
        if (dailyArray == null) {
            return forecasts;
        }

        int limit = Math.min(10, dailyArray.size());
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE").withZone(ZoneId.systemDefault());

        for (int i = 0; i < limit; i++) {
            JsonObject item = dailyArray.get(i).getAsJsonObject();
            Forecast forecast = new Forecast();

            if (item.has("dt")) {
                long timestamp = item.get("dt").getAsLong();
                forecast.setTimestamp(timestamp);
                String dayLabel = dayFormatter.format(Instant.ofEpochSecond(timestamp));
                forecast.setDayOfWeek(dayLabel);
                forecast.setTimeLabel(dayLabel);
            }

            if (item.has("temp")) {
                JsonObject temp = item.getAsJsonObject("temp");
                if (temp.has("min")) {
                    forecast.setTempMin(temp.get("min").getAsDouble());
                }
                if (temp.has("max")) {
                    forecast.setTempMax(temp.get("max").getAsDouble());
                }
                if (temp.has("day")) {
                    forecast.setTemperature(temp.get("day").getAsDouble());
                } else {
                    forecast.setTemperature((forecast.getTempMin() + forecast.getTempMax()) / 2);
                }
            }

            if (item.has("humidity")) {
                forecast.setHumidity(item.get("humidity").getAsInt());
            }

            if (item.has("wind_speed")) {
                forecast.setWindSpeed(item.get("wind_speed").getAsDouble());
            }

            if (item.has("pop")) {
                int precipPercent = (int) (item.get("pop").getAsDouble() * 100);
                forecast.setPrecipitation(precipPercent);
            }

            if (item.has("rain")) {
                forecast.setRainAmount(item.get("rain").getAsDouble());
            }

            if (item.has("snow")) {
                forecast.setSnowAmount(item.get("snow").getAsDouble());
            }

            if (item.has("weather") && item.getAsJsonArray("weather").size() > 0) {
                JsonObject weather = item.getAsJsonArray("weather").get(0).getAsJsonObject();
                forecast.setCondition(weather.get("main").getAsString());
                forecast.setDescription(weather.get("description").getAsString());
                forecast.setIconCode(weather.get("id").getAsString());
            }

            forecast.setTemperatureUnit(units);
            forecasts.add(forecast);
        }

        return forecasts;
    }

    private String resolveUnits() {
        return "imperial".equalsIgnoreCase(preferencesService.getTemperatureUnit()) ? "imperial" : "metric";
    }

    private void markCached(List<Forecast> forecasts, boolean cached) {
        if (forecasts == null) {
            return;
        }
        forecasts.forEach(forecast -> forecast.setCached(cached));
    }
}

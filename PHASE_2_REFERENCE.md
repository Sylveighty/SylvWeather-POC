# 📚 Phase 2 Quick Reference Guide

## Project Structure (After Phase 2)

```
SylvWeather/
├── src/main/java/com/school/weatherapp/
│   ├── app/
│   │   └── MainApp.java                    # Entry point + layout
│   ├── config/
│   │   └── AppConfig.java                  # Configuration (API keys, units)
│   ├── data/
│   │   ├── models/
│   │   │   ├── Forecast.java               # Daily/hourly forecast data
│   │   │   └── Weather.java                # Current weather data
│   │   └── services/
│   │       ├── WeatherService.java         # Current weather API
│   │       └── ForecastService.java        # Forecast API + grouping
│   ├── features/                           # Ready for Phase 3+
│   └── ui/
│       └── panels/
│           ├── CurrentWeatherPanel.java    # Phase 1
│           ├── HourlyForecastPanel.java    # Phase 2 ✅
│           ├── DailyForecastPanel.java     # Phase 2 ✅
│           └── AlertPanel.java             # Phase 2 skeleton ✅
├── src/main/resources/
│   └── theme.css                           # Light theme stylesheet ✅
├── build.gradle                            # Dependencies + build config
└── PHASE_2_COMPLETE.md                     # Detailed completion report

```

---

## Running the Application

### Option 1: Using Gradle
```bash
cd SylvWeather
gradle runApp
```

### Option 2: Using IDE
- Open `MainApp.java` → Right-click → Run

### Option 3: Building JAR
```bash
gradle build
java -jar build/libs/SylvWeather-1.0-SNAPSHOT.jar
```

---

## Key Classes & Methods (Phase 2)

### DailyForecastPanel
```java
public void loadDailyForecast(String cityName)
private void displayForecasts(List<Forecast> forecasts)
private VBox createForecastCard(Forecast forecast)
private String getWeatherEmoji(String condition)
```

### HourlyForecastPanel
```java
public void loadHourlyForecast(String cityName)
private void displayForecasts(List<Forecast> forecasts)
private VBox createForecastCard(Forecast forecast)
private String getWeatherEmoji(String condition)
```

### ForecastService
```java
public CompletableFuture<List<Forecast>> getHourlyForecastAsync(String cityName)
public CompletableFuture<List<Forecast>> getDailyForecastAsync(String cityName)
private List<Forecast> groupForecastsByDay(List<Forecast> forecasts)
private List<Forecast> parseForecastResponse(String jsonResponse)
```

### AlertPanel (Skeleton)
```java
public void loadAlerts(String cityName)           // For Phase 3
private void showNoAlerts()                       // Current behavior
```

### MainApp
```java
public void start(Stage primaryStage)             // Updated with CSS + AlertPanel
private void loadForecasts(String cityName)       // Updated with alertPanel
private void setupCityChangeListener()            // Already implemented
```

---

## CSS Classes Available

### For Cards
```css
.forecast-card          /* Base card styling */
.forecast-card:hover    /* Hover state with shadow */
```

### For Alerts (Phase 3)
```css
.alert-high             /* High severity - red */
.alert-medium           /* Medium severity - orange */
.alert-low              /* Low severity - blue */
```

### For Text
```css
.title-text             /* 22px bold titles */
.section-title          /* 18px bold sections */
.label-primary          /* 14px main labels */
.label-secondary        /* 12px secondary labels */
```

### For Temperatures
```css
.temp-hot               /* #d32f2f red */
.temp-cold              /* #1976d2 blue */
.temp-neutral           /* #f57c00 orange */
```

---

## Configuration (AppConfig.java)

```java
public static final String WEATHER_API_KEY = "YOUR_API_KEY_HERE";
public static final String WEATHER_API_BASE_URL = "https://api.openweathermap.org/data/2.5";
public static final String DEFAULT_CITY = "New York";
public static final String TEMPERATURE_UNIT = "imperial"; // or "metric"
```

---

## Data Flow Diagram

```
User searches city
        ↓
CurrentWeatherPanel → WeatherService → OpenWeatherMap API
        ↓ (city change event)
MainApp.setupCityChangeListener()
        ↓
loadForecasts(cityName)
        ↓
┌───────────────┬──────────────────┬──────────────┐
↓               ↓                  ↓              ↓
HourlyPanel     DailyPanel         AlertPanel     (more panels)
    ↓              ↓                   ↓
ForecastService   ForecastService   AlertService
    ↓              ↓                   ↓
OpenWeatherMap 5-day/3hr Forecast     Weather Alerts API
    ↓              ↓                   ↓
Display 24hrs     Group by day       Display alerts
```

---

## Phase 2 Enhancements

### UI Improvements
- ✅ Hover effects with smooth transitions
- ✅ Shadow effects on card hover
- ✅ Color-coded temperatures (hot=red, cold=blue)
- ✅ Weather emoji representations
- ✅ Responsive card layouts
- ✅ Loading indicators

### Code Quality
- ✅ Comprehensive Javadoc comments
- ✅ Error handling for API failures
- ✅ Async/non-blocking operations
- ✅ Proper separation of concerns
- ✅ Reusable service architecture

### Styling
- ✅ CSS theme support (theme.css)
- ✅ Consistent color scheme
- ✅ Typography hierarchy
- ✅ Component styling framework
- ✅ Ready for light/dark theme toggle (Phase 4)

---

## Common Customizations

### Change default city
```java
// In AppConfig.java
public static final String DEFAULT_CITY = "London";
```

### Change temperature unit
```java
// In AppConfig.java
public static final String TEMPERATURE_UNIT = "metric"; // Change to °C
```

### Adjust card width
```java
// In DailyForecastPanel.java, line ~110
card.setPrefWidth(130);  // Increase from 110
```

### Modify hover shadow
```java
// In DailyForecastPanel.java, createForecastCard() method
"-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 12, 0, 0, 3);"
// Adjust: rgba values, radius, offsets
```

---

## Debugging Tips

### Check API errors
```java
// In ForecastService.java
System.err.println("Error fetching forecast: " + e.getMessage());
// Look in console for error details
```

### Verify data loading
```java
// In DailyForecastPanel.java
System.out.println("Forecasts loaded: " + forecasts.size());
```

### Monitor city changes
```java
// In MainApp.java
currentWeatherPanel.setOnCityChange(cityName -> {
    System.out.println("City changed to: " + cityName);
    loadForecasts(cityName);
});
```

---

## Phase 3 Preparation

AlertPanel is ready for Phase 3 implementation. You'll need to:

1. **Create Alert model class**
   ```java
   public class Alert {
       private String id, type, title, description;
       private String severity; // "low", "medium", "high"
       private long timestamp;
       // getters/setters...
   }
   ```

2. **Create AlertService**
   ```java
   public CompletableFuture<List<Alert>> getAlertsAsync(String cityName)
   ```

3. **Implement AlertPanel.loadAlerts()**
   - Fetch alerts from API
   - Display alert cards with severity styling
   - Add click handlers for detail modals

4. **Create AlertCard component**
   - Title, description, severity badge
   - Clickable for details
   - Dismissable

---

## Build & Testing

### Full rebuild
```bash
gradle clean build
```

### Compile only (faster)
```bash
gradle compileJava
```

### Run tests (when added)
```bash
gradle test
```

### Generate documentation
```bash
gradle javadoc
# Output: build/docs/javadoc/
```

---

## Support Notes for Teachers

- All code is commented with clear Javadoc
- Package structure follows industry standards
- Async operations prevent UI freezing
- Error handling prevents crashes
- CSS styling is easily customizable
- Service layer allows API swapping (for testing with mock data)

**Total Code:**  ~800 lines (UI + services, excluding Phase 1)
**Complexity:** High school advanced level
**Time to implement:** ~3-4 hours guided

---

**Phase 2: ✅ COMPLETE**
**Next: Phase 3 - Real-Time Alerts**

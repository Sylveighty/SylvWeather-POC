# 🚀 Phase 2 Quick Start Guide

## Running the Application

### Method 1: Using Gradle (Recommended)
```bash
cd SylvWeather
gradle runApp
```

### Method 2: Using IDE
1. Open `MainApp.java` in VS Code
2. Right-click → "Run Java"
3. Or press `F5` to debug

### Method 3: Building JAR
```bash
gradle build
java -jar build/libs/SylvWeather-1.0-SNAPSHOT.jar
```

---

## What You'll See

### On Startup
```
✅ Application launches
✅ Window opens: "Weather Dashboard" (1400×900px)
✅ Panels load with data from OpenWeatherMap API
```

### Layout Structure (Top to Bottom)
```
┌─────────────────────────────────────────────────┐
│  Search Bar: [Enter city] [Search]              │ ← Search for cities
├─────────────────────────────────────────────────┤
│  Current Weather                                │ ← New York (or default city)
│  72°F, Clear Sky                                │
├─────────────────────────────────────────────────┤
│  ⚠️ Weather Alerts                              │ ← Placeholder (Phase 3)
│  No active weather alerts                       │
├─────────────────────────────────────────────────┤
│  Hourly Forecast (next 24 hours)                │ ← Phase 2 Feature
│  [2PM] [3PM] [4PM] [5PM] [6PM] ...             │
│   72°F  70°F  68°F  66°F  64°F                  │
├─────────────────────────────────────────────────┤
│  7-Day Forecast                                 │ ← Phase 2 Feature
│  [Fri]  [Sat]  [Sun]  [Mon]  [Tue]  ...        │
│  75/62  72/59  60/50  58/48  55/45              │
└─────────────────────────────────────────────────┘
```

---

## Interactive Features to Try

### 1. Search for Different Cities
```
1. Type city name in search box: "London"
2. Click [Search] or press Enter
3. Watch panels update automatically
```

### 2. Hover Over Forecast Cards
```
1. Hover over any hourly or daily forecast card
2. Card background lightens
3. Drop shadow appears
4. Smooth transition (no flickering)
```

### 3. View Temperatures
```
Daily Forecast:
- Red temperature = High temp (hot)
- Blue temperature = Low temp (cold)

Example: 75/62 = 75°F high (red), 62°F low (blue)
```

### 4. Check Hourly Precipitation
```
Hourly cards show:
- Time (e.g., "2:00 PM")
- Weather emoji (☀️ ☁️ 🌧️ etc.)
- Temperature (e.g., "72°F")
- Precipitation chance if > 0% (e.g., "20%")
```

---

## Understanding the Data

### Current Weather (Phase 1)
```
Shows:
- Location: City, State, Country
- Current temperature
- Condition (Clear, Clouds, Rain, etc.)
- Details: Feels like, Humidity, Wind, Pressure
- Last updated timestamp
```

### Hourly Forecast (Phase 2 - NEW)
```
Shows next 24 hours in 3-hour intervals:
- Time label (e.g., "2:00 PM")
- Weather condition as emoji
- Exact temperature
- Precipitation % if available
- 8 cards visible (24 hours ÷ 3 = 8)
```

### 7-Day Forecast (Phase 2 - NEW)
```
Shows 7-day outlook:
- Day label (e.g., "Monday")
- Weather condition as emoji
- High temperature (red)
- Low temperature (blue)
- Weather condition text
- 7 cards visible (one per day)
```

### Alerts (Phase 2 - Skeleton, Phase 3 - Implementation)
```
Currently:
✅ Panel exists and displays
✅ Shows "No active weather alerts"
❌ Not fetching real alerts yet (Phase 3)

After Phase 3:
✅ Will fetch from weather alert API
✅ Display severity levels (high/medium/low)
✅ Show alert details on click
```

---

## Customization

### Change Default City
**File:** `src/main/java/com/school/weatherapp/config/AppConfig.java`
```java
public static final String DEFAULT_CITY = "Los Angeles"; // Change here
```

### Change Temperature Unit
**File:** `src/main/java/com/school/weatherapp/config/AppConfig.java`
```java
public static final String TEMPERATURE_UNIT = "metric"; // metric = °C, imperial = °F
```

### Add Your API Key
**File:** `src/main/java/com/school/weatherapp/config/AppConfig.java`
```java
public static final String WEATHER_API_KEY = "YOUR_ACTUAL_KEY"; // From OpenWeatherMap
```

### Adjust Card Sizes
**File:** `src/main/java/com/school/weatherapp/ui/panels/DailyForecastPanel.java` ~line 110
```java
card.setPrefWidth(130); // Increase from 110 for larger cards
```

### Modify Hover Effects
**File:** `src/main/resources/theme.css` or inline in panels
```css
.forecast-card:hover {
    -fx-background-color: #e8e8e8;  /* Adjust color */
    -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 12, 0, 0, 3); /* Adjust shadow */
}
```

---

## Troubleshooting

### Application won't start
```
❌ Error: "No API key" or "Connection refused"
✅ Solution: Check AppConfig.java has valid OpenWeatherMap API key

❌ Error: "Cannot find theme.css"
✅ Solution: Ensure src/main/resources/theme.css exists

❌ Error: Port already in use
✅ Solution: Close other instances of the app
```

### Panels show "Could not load forecast data"
```
Possible causes:
1. No internet connection
2. Invalid API key
3. API rate limit reached
4. OpenWeatherMap API down

Solutions:
1. Check internet connection
2. Verify API key in AppConfig
3. Wait a few minutes
4. Check OpenWeatherMap status page
```

### Hover effects not showing
```
❌ Problem: Cards don't highlight on hover
✅ Solutions:
1. Check CSS loaded: Look for "Exception" in console
2. Verify theme.css path in MainApp
3. Rebuild: gradle clean build
4. Restart application
```

### City search not working
```
❌ Problem: Search field doesn't respond
✅ Solutions:
1. Click in search field first
2. Type city name
3. Press Enter or click [Search] button
4. Check console for errors
```

---

## Console Output Meanings

### ✅ Expected On Startup
```
Weather App launched successfully!
Phase 2: Forecast Panels loaded
```

### ✅ Expected When Loading Data
```
No console output = Normal (async loading)
Look for panels to populate ~1 second after search
```

### ❌ Errors To Watch For
```
"Error fetching forecast: HTTP 401"
→ Invalid API key in AppConfig

"Error fetching forecast: Unexpected character"
→ JSON parsing error (API response format changed)

"NullPointerException" 
→ API returned null data (check API status)
```

---

## Phase 2 Feature Highlights

### ✨ What's New
1. **Hourly Forecast** - Next 24 hours, 3-hour intervals
2. **Daily Forecast** - 7-day outlook with high/low temps
3. **Hover Effects** - Smooth animations on all cards
4. **CSS Theme** - Professional styling with light theme
5. **Alert Panel** - Ready for real-time alerts (Phase 3)
6. **Color Coding** - Red=hot, Blue=cold temperatures

### 🎨 Design Features
- Clean card-based layout
- Responsive to window size
- High contrast, accessible colors
- Professional typography
- Smooth animations (no UI lag)
- Proper spacing and padding

### ⚙️ Technical Features
- Async API calls (no blocking)
- Error handling and recovery
- Loading indicators
- Modular architecture
- Service-based design
- CSS theme support

---

## Next: Phase 3 Preview

**What Phase 3 Will Add:**

1. **Real Weather Alerts**
   - Fetch from OpenWeatherMap alerts API
   - Display alerts with severity levels
   - Show alert details on click

2. **Alert Styling**
   - Red for high severity (dangerous)
   - Orange for medium severity (warning)
   - Blue for low severity (information)

3. **Real-Time Updates**
   - Poll alerts every 10-15 minutes
   - Display animation when new alert arrives
   - Dismiss/acknowledge alerts

4. **User Interaction**
   - Click alert for full details
   - Dismiss alert
   - Visual notification badge

**Estimated Phase 3 Time:** 1-2 hours guided

---

## Tips & Tricks

### Performance
- ✅ Application loads quickly
- ✅ Smooth scrolling
- ✅ No lag on hover effects
- ✅ If slow, check internet connection

### UI Best Practices
- 📱 Resize window to test responsiveness
- 🔄 Try searching different cities
- 🖱️ Hover on every card to test effects
- 👁️ Observe color coding (red=hot, blue=cold)

### Data Understanding
- 🌡️ Temperatures are in your configured unit (°F or °C)
- 🌧️ Precipitation % shows chance of rain
- 💨 Wind speed in mph (imperial) or m/s (metric)
- ⏰ All times in your local timezone

### Development
- 📝 Check console for debug messages
- 🔍 Use IDE's debugger if stuck
- 📚 Refer to PHASE_2_REFERENCE.md
- 🆘 Check WHAT_WAS_IMPLEMENTED.md for details

---

## File Locations (Quick Reference)

```
Config:        src/main/java/.../config/AppConfig.java
Current Panel: src/main/java/.../ui/panels/CurrentWeatherPanel.java
Hourly Panel:  src/main/java/.../ui/panels/HourlyForecastPanel.java (NEW)
Daily Panel:   src/main/java/.../ui/panels/DailyForecastPanel.java (NEW)
Alert Panel:   src/main/java/.../ui/panels/AlertPanel.java (NEW)
Services:      src/main/java/.../data/services/*.java
Models:        src/main/java/.../data/models/*.java
Theme:         src/main/resources/theme.css (NEW)
Main:          src/main/java/.../app/MainApp.java (UPDATED)
```

---

## Quick Documentation Index

| Document | Purpose |
|----------|---------|
| PHASE_2_REFERENCE.md | Quick lookup - methods, features, classes |
| PHASE_2_COMPLETE.md | Detailed report - what was completed |
| WHAT_WAS_IMPLEMENTED.md | Implementation details - what changed |
| UI_LAYOUT_REFERENCE.md | Visual guide - UI structure and design |
| PHASE_2_CHECKLIST.md | Verification - what's done and ready |
| IMPLEMENTATION_SUMMARY.md | Statistics - files, lines, changes |

---

## Support Command Summary

```bash
# Build and run
gradle runApp

# Just build (no run)
gradle build

# Clean and rebuild
gradle clean build

# Show errors with details
gradle --stacktrace build

# Generate documentation
gradle javadoc

# View dependencies
gradle dependencies
```

---

## You're All Set! 🎉

**Phase 2 is complete and working.** 

- ✅ Run the app with `gradle runApp`
- ✅ Try searching different cities
- ✅ Hover over forecast cards to see effects
- ✅ Check hourly and daily forecasts
- ✅ Review documentation as needed

**Next:** Phase 3 - Real-Time Alerts

Questions? Check the documentation files first! 📚

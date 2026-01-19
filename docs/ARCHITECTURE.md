
---

# `/docs/ARCHITECTURE.md` (ready to paste)

```markdown
# Architecture

## Overview

SylvWeather-POC is a JavaFX desktop application structured as a proof-of-concept. The architecture is organized to keep user interface code separate from API access and data models. This makes the project easier to explain in an academic setting and easier to extend in future iterations.

---

## Package Structure (Actual)

### `com.school.weatherapp.app`
- **`MainApp`**
  - JavaFX entry point (`Application`)
  - Builds the stage, scene, and top-level layout
  - Instantiates UI panels and connects them through callbacks

### `com.school.weatherapp.config`
- **`AppConfig`**
  - Centralizes constants such as:
    - OpenWeather base URL
    - default city
    - temperature unit preference
    - favorites file path
  - (Current limitation: API key is stored here in-source.)

### `com.school.weatherapp.data.models`
- **`Weather`**: current conditions data used by the current weather UI
- **`Forecast`**: forecast entries used by hourly and daily forecast panels
- **`Alert`**: alert objects displayed in the alert panel

### `com.school.weatherapp.data.services`
- **`WeatherService`**
  - Calls OpenWeather current weather endpoint
  - Parses JSON using Gson into `Weather`

- **`ForecastService`**
  - Calls OpenWeather 5-day/3-hour forecast endpoint
  - Produces:
    - hourly list (next ~24 hours)
    - daily summaries (grouped by day)

- **`AlertService`**
  - Attempts to retrieve alert data using city geocoding + a follow-up request
  - If unavailable/failing, provides simulated alerts as fallback for demo purposes

### `com.school.weatherapp.features`
- **`FavoritesService`**
  - Stores favorite cities in a local text file (`favorites.txt`)
  - Supports add/remove/list operations
  - Used by current weather + favorites UI panels

### `com.school.weatherapp.ui.panels`
Modular UI components:
- `CurrentWeatherPanel`
- `FavoritesPanel`
- `HourlyForecastPanel`
- `DailyForecastPanel`
- `AlertPanel`

Each panel is responsible for:
- constructing its own UI layout
- calling the relevant service
- rendering data models into JavaFX controls
- exposing callbacks/events where cross-panel communication is needed

### `com.school.weatherapp.util`
- `DateTimeUtil`
- `TemperatureUtil`

Utilities support formatting and conversions used by the UI.

---

## Data Flow

A typical request cycle looks like this:

1. **User action**
   - The user searches a city in `CurrentWeatherPanel` or selects one in `FavoritesPanel`.

2. **Current weather retrieval**
   - `CurrentWeatherPanel` calls `WeatherService.getCurrentWeatherAsync(city)`.

3. **Forecast + alert retrieval**
   - `MainApp` listens for city changes and triggers:
     - `HourlyForecastPanel.loadHourlyForecast(city)`
     - `DailyForecastPanel.loadDailyForecast(city)`
     - `AlertPanel.loadAlerts(city)`

4. **Parsing**
   - Each service parses JSON into model objects:
     - `Weather`, `Forecast`, `Alert`

5. **UI update**
   - Panels update JavaFX controls with formatted text, timestamps, and values.

---

## JavaFX Lifecycle Responsibilities

- `MainApp.start(Stage)` is responsible for:
  - creating the `Scene` and attaching it to the `Stage`
  - creating the top bar (theme toggle + unit toggle)
  - constructing and placing panels into the layout
  - wiring panel callbacks so city changes refresh forecasts and favorites
  - triggering initial loading using `AppConfig.DEFAULT_CITY`

Panels handle their own rendering and service calls, keeping the entry point focused on composition and wiring.

---

## Scope and Intent

This architecture intentionally avoids production-level concerns such as:
- dependency injection frameworks
- persistent databases
- caching layers
- complex background scheduling

The priority is a clear and teachable structure for JavaFX + REST API integration.

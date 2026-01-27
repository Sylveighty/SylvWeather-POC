# Architecture

## Overview

SylvWeather-POC is a JavaFX desktop proof-of-concept (POC) organized around clear separation of concerns: UI panels render data, services retrieve and parse API responses, and models represent the data passed to the UI. The architecture is intentionally simple to support learning and academic presentation.

## Package Structure (Actual)

### `com.school.weatherapp.app`
- **`MainApp`**
  - JavaFX entry point (`Application`)
  - Creates the scene/layout
  - Initializes UI panels and wires cross-panel interactions

### `com.school.weatherapp.config`
- **`AppConfig`**
  - Central configuration constants (API base URL, default city, favorites file path, etc.)

### `com.school.weatherapp.data.models`
- **`Weather`**
  - Current conditions data displayed by the current weather panel
- **`Forecast`**
  - Forecast entries (3-hour) and daily summary values used by forecast panels
- **`Alert`**
  - Alert objects displayed in the alerts panel

### `com.school.weatherapp.data.services`
- **`WeatherService`**
  - Calls OpenWeather current weather endpoint
  - Parses JSON via Gson into `Weather`
- **`ForecastService`**
  - Calls OpenWeather 5-day / 3-hour forecast endpoint
  - Produces hourly subset and daily summaries by grouping entries by day
- **`AlertService`**
  - Attempts geocoding lookup and an alerts request

### `com.school.weatherapp.features`
- **`FavoritesService`**
  - Loads/saves favorite cities to a local text file (`favorites.txt`)

### `com.school.weatherapp.ui.panels`
- `CurrentWeatherPanel`
- `FavoritesPanel`
- `HourlyForecastPanel`
- `DailyForecastPanel`
- `AlertPanel`

Panels are responsible for:
- building their own UI layout
- requesting data via services
- rendering model data into JavaFX controls
- exposing callbacks for cross-panel communication (e.g., city selection)

### `com.school.weatherapp.util`
- `DateTimeUtil` (timestamp formatting)
- `TemperatureUtil` (temperature conversion/formatting)

## Data Flow

Typical flow when a user searches/selects a city:

1. User enters/selects a city in the UI:
   - `CurrentWeatherPanel` search action OR `FavoritesPanel` selection

2. Current weather request:
   - UI triggers `WeatherService.getCurrentWeatherAsync(city)`
   - Response JSON is parsed into `Weather`
   - UI updates labels and details grid

3. Forecast + alerts refresh:
   - `MainApp` listens for city changes and triggers:
     - `HourlyForecastPanel.loadHourlyForecast(city)`
     - `DailyForecastPanel.loadDailyForecast(city)`
     - `AlertPanel.loadAlerts(city)`

4. Forecast parsing + transformation:
   - `ForecastService` fetches `/forecast` and parses entries into `Forecast`
   - Hourly panel displays a subset of the next forecast entries
   - Daily panel groups entries by day and computes min/max summary values

5. Alerts behavior:
   - `AlertService` attempts an API-based alert retrieval path

## JavaFX Lifecycle Responsibilities

- `MainApp.start(Stage)` performs:
  - stage/scene setup
  - root layout creation (`BorderPane`)
  - panel initialization and placement
  - wiring panel callbacks (city change, favorites refresh)
  - initial data loading using `AppConfig.DEFAULT_CITY`

UI updates are executed in the JavaFX runtime context, with asynchronous service calls returning results that the UI can safely render.

## Scope and Intent

This architecture intentionally avoids production patterns such as:
- dependency injection frameworks
- persistent databases
- background schedulers
- robust retry policies

The goal is a clear, teachable example of JavaFX + REST API integration.

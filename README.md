# SylvWeather-POC

## 1. Project Overview

**SylvWeather-POC** is a **JavaFX desktop weather dashboard proof-of-concept (POC)** written in Java 17+ and built with Gradle. The goal of this project is to demonstrate JavaFX application structure (layout, event handling, and lifecycle) alongside integration with a real external REST API (OpenWeather). The project prioritizes clarity and learning value over production concerns such as caching, persistence layers, authentication flows, or exhaustive error handling.

---

## 2. UI Preview

> Screenshots to be added.

- `docs/images/main-dashboard.png`
- `docs/images/forecast-panels.png`
- `docs/images/alerts-and-favorites.png`

---

## 3. Implemented Features

The following features are implemented in the current codebase:

- **Live current weather lookup by city** using the OpenWeather Current Weather endpoint
- **Hourly forecast view (next ~24 hours)** based on the 5-day / 3-hour forecast feed
- **Daily forecast summary view** by grouping 3-hour forecast entries by day and calculating daily min/max
- **Favorites list**:
  - Add/remove favorite cities
  - Persist favorites to a local text file (`favorites.txt`)
  - Select a favorite to load its weather
- **Theme toggle (Light/Dark)** applied across panels
- **Temperature unit toggle (°F / °C)** with UI refresh across current + forecast panels
- UI built with modular panels:
  - Current conditions
  - Favorites
  - Hourly forecast
  - Daily forecast
  - Alerts

---

## 4. Architecture Overview

The project is structured to keep UI concerns separate from API access and data models:

### Package layout (actual)

- `com.school.weatherapp.app`
  - `MainApp` (JavaFX entry point; builds the scene and wires panels together)

- `com.school.weatherapp.config`
  - `AppConfig` (central configuration constants)

- `com.school.weatherapp.data.models`
  - `Weather`, `Forecast`, `Alert` (data objects used by the UI)

- `com.school.weatherapp.data.services`
  - `WeatherService` (current conditions)
  - `ForecastService` (5-day/3-hour forecast; derives hourly + daily views)
  - `AlertService` (attempts alert retrieval; falls back to simulated alerts when unavailable)

- `com.school.weatherapp.features`
  - `FavoritesService` (file-based favorite city persistence)

- `com.school.weatherapp.ui.panels`
  - `CurrentWeatherPanel`, `FavoritesPanel`, `HourlyForecastPanel`, `DailyForecastPanel`, `AlertPanel`

- `com.school.weatherapp.util`
  - `DateTimeUtil`, `TemperatureUtil`

### JavaFX lifecycle

- `MainApp extends javafx.application.Application`
- `start(Stage)`:
  - Builds the root layout and scene
  - Initializes panels
  - Wires callbacks between panels (e.g., city changes trigger forecast reload)
  - Loads initial data for the default city

UI updates triggered by API calls are performed safely using JavaFX threading practices (e.g., updating UI on the FX Application Thread).

---

## 5. Weather Data Source

This application uses the **OpenWeather API**:

- **Current weather** via `WeatherService` (`/data/2.5/weather`)
- **Forecast data** via `ForecastService` (`/data/2.5/forecast`)
  - Hourly view: selects the next 8 forecast entries (3-hour increments)
  - Daily view: groups entries by day and computes summary values
- **Alerts**
  - `AlertService` attempts to retrieve alert-like data, but includes fallback behavior:
    - If the API path is unavailable or fails, the UI is populated with **simulated demo alerts**
  - This behavior is intentional for a POC demo and avoids blocking the rest of the UI.

### API key usage

An API key is required to run the application. In the current codebase the key is read from `AppConfig.WEATHER_API_KEY`.

- Do **not** commit real API keys to version control.
- For portfolio use, replace the key with a placeholder and load it from environment variables or a local config file (recommended improvement listed in the roadmap).

Advanced concerns like rate-limiting strategies, retries, and caching are intentionally out of scope for this POC.

---

## 6. Technology Stack

- Java 17+
- JavaFX (Gradle plugin: `org.openjfx.javafxplugin`, JavaFX version configured in `build.gradle`)
- Gradle
- OpenWeather API
- Gson (`com.google.code.gson:gson:2.10.1`) for JSON parsing
- JUnit 5 (`org.junit.jupiter`) included for testing dependencies (tests are limited in current scope)

---

## 7. Getting Started

### Prerequisites

- JDK 17+
- Gradle (or use the Gradle Wrapper if present)
- Internet connection (for live API calls)

### API key setup

1. Obtain an API key from OpenWeather.
2. Set the key in:
   - `src/main/java/com/school/weatherapp/config/AppConfig.java`
   - `WEATHER_API_KEY`

**Important:** Avoid committing keys. For a safer approach, refactor `AppConfig` to read from an environment variable (see “Future Improvements”).

### Build

```bash
./gradlew build

# SylvWeather-POC

## 1. Project Overview

SylvWeather-POC is a Java 17+ / JavaFX desktop weather dashboard built as a proof-of-concept (POC). The project is designed to demonstrate the JavaFX application lifecycle, UI layout composition, and integration with a real external REST API (OpenWeather). The implementation prioritizes clarity and learning value over production concerns such as caching, persistence layers, rate-limit strategies, and exhaustive error handling.

## 2. UI Preview

> Screenshots to be added.

- `docs/images/main-dashboard.png`
- `docs/images/current-weather-and-favorites.png`
- `docs/images/hourly-and-daily-forecast.png`
- `docs/images/alerts-panel.png`

## 3. Implemented Features

Only features present in the current codebase are listed here:

- Search weather by city name and display current conditions in the UI
- Retrieve live data from OpenWeather:
  - Current weather via `WeatherService`
  - Forecast via `ForecastService` (5-day / 3-hour feed)
- Hourly forecast view:
  - Displays a next-period subset of the 3-hour forecast feed (commonly used as an “~24 hours” view)
- Daily forecast summary view:
  - Groups 3-hour forecast entries by day
  - Calculates daily min/max temperatures and displays a summary
- Favorites:
  - Add/remove cities from favorites
  - Favorites are persisted to a local text file (`favorites.txt`) via `FavoritesService`
- Theme toggle (light/dark) applied across panels
- Temperature unit toggle (F/C) that refreshes temperature displays across panels
- Alerts panel:
  - Attempts to fetch alerts via `AlertService`
  - Falls back to simulated alerts when API retrieval is unavailable or fails

## 4. Architecture Overview

The project is organized around clear separation of concerns:

### Packages (actual)

- `com.school.weatherapp.app`
  - `MainApp` — JavaFX entry point; builds scene/layout and wires panel interactions

- `com.school.weatherapp.config`
  - `AppConfig` — centralized configuration constants (API base URL, default city, favorites file path, etc.)

- `com.school.weatherapp.data.models`
  - `Weather`, `Forecast`, `Alert` — data objects used by services and rendered by the UI

- `com.school.weatherapp.data.services`
  - `WeatherService` — current weather fetch + JSON parsing (Gson)
  - `ForecastService` — forecast fetch + transformation for hourly/daily views
  - `AlertService` — attempts alert retrieval and provides simulated fallback alerts

- `com.school.weatherapp.features`
  - `FavoritesService` — loads/saves favorite cities to `favorites.txt`

- `com.school.weatherapp.ui.panels`
  - `CurrentWeatherPanel`, `FavoritesPanel`, `HourlyForecastPanel`, `DailyForecastPanel`, `AlertPanel` — modular UI components

- `com.school.weatherapp.util`
  - `DateTimeUtil`, `TemperatureUtil` — formatting and conversions used by UI components

### JavaFX lifecycle

- `MainApp extends javafx.application.Application`
- `start(Stage)`:
  - creates the root layout (`BorderPane`) and scene
  - initializes panels and composes them into the layout
  - wires callbacks between panels (city change → refresh forecast/alerts; favorites select → load city)
  - triggers initial forecast/alert loading using `AppConfig.DEFAULT_CITY`

## 5. Weather Data Source

This project uses the OpenWeather API.

- Current conditions:
  - Endpoint: `/data/2.5/weather`
  - Implemented in: `com.school.weatherapp.data.services.WeatherService`

- Forecast:
  - Endpoint: `/data/2.5/forecast` (5-day / 3-hour)
  - Implemented in: `com.school.weatherapp.data.services.ForecastService`
  - Used to produce:
    - Hourly subset (next N 3-hour entries)
    - Daily summaries (grouped by day with min/max)

- Alerts:
  - Implemented in: `com.school.weatherapp.data.services.AlertService`
  - Behavior:
    - Attempts a geocoding lookup and a follow-up alerts request
    - If unavailable/failing, the UI displays simulated alerts (intended for demonstration)

### API key usage

An API key is required to run the application. Do not commit real keys to version control.

Recommended setup:
- Provide the key via environment variable (preferred), or use a local config that is excluded from git.

Advanced concerns such as caching, retries, and rate limiting are intentionally out of scope for this POC.

## 6. Technology Stack

- Java 17+
- JavaFX (configured via `org.openjfx.javafxplugin` in `build.gradle`)
- Gradle
- OpenWeather API
- Gson (`com.google.code.gson:gson:2.10.1`) for JSON parsing
- JUnit 5 dependencies included for testing (`org.junit.jupiter`)

## 7. Getting Started

### Prerequisites
- JDK 17+
- Gradle installed and available in PATH
- Internet connection (for live API calls)

### API key setup (recommended)

Set an environment variable:

**Windows (PowerShell)**
```powershell
setx OPENWEATHER_API_KEY "YOUR_KEY_HERE"

macOS/Linux

export OPENWEATHER_API_KEY="YOUR_KEY_HERE"

If your code currently reads the key from AppConfig.WEATHER_API_KEY, update it to read from OPENWEATHER_API_KEY (recommended portfolio hygiene). Do not expose keys in commits.

Build
gradle build

Run
gradle run


(There is also a custom Gradle task runApp defined in build.gradle.)

JavaFX notes

JavaFX is configured through the Gradle JavaFX plugin. Ensure you are using a JDK 17+ and that Gradle resolves the JavaFX dependencies successfully.

8. Design Decisions

JavaFX was chosen to demonstrate desktop UI development using a standard Java application lifecycle and layout composition.

OpenWeather was selected because it provides accessible REST endpoints suitable for educational projects.

The UI is split into panels (com.school.weatherapp.ui.panels) to keep concerns localized and presentation-friendly.

The project emphasizes readability and modular structure over advanced production patterns (DI, persistence layers, complex state management).

9. Limitations

Not production hardened (POC scope)

Limited automated testing

No caching/offline support

API resilience features (timeouts/retries/rate-limit handling) are minimal

Alerts are not guaranteed from the live API path used; simulated fallback alerts may be shown

Favorites persistence is a simple text file (favorites.txt), not a database-backed feature

10. Future Improvements

Improve error handling and user feedback for network and API failures

Add API resilience: timeouts, retries, lightweight caching

Add unit and integration tests for parsing and forecast grouping

Reduce inline styling duplication and improve theming consistency

Cross-platform packaging (e.g., jpackage)

Externalize configuration cleanly (API key, defaults) without committing secrets

11. License & Usage

This project is intended for educational and non-commercial use. If you use OpenWeather endpoints, ensure usage complies with OpenWeather terms and any applicable rate limits.

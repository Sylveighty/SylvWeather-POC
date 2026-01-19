SylvWeather-POC
1. Project Overview

SylvWeather-POC is a Java 17+ / JavaFX desktop weather dashboard built as a proof-of-concept (POC). The project demonstrates the JavaFX application lifecycle, modular UI composition, and integration with a real external REST API (OpenWeather).

This project prioritizes clarity, structure, and learning value over production concerns such as caching, persistence layers, rate limiting, and exhaustive error handling. It is intended for academic evaluation, technical presentations, and early-career portfolio review.

2. UI Preview

Screenshots to be added.

Planned image locations:

docs/images/main-dashboard.png

docs/images/current-weather-and-favorites.png

docs/images/hourly-and-daily-forecast.png

docs/images/alerts-panel.png

3. Implemented Features

Only features present in the current codebase are listed below:

Search weather by city name and display current conditions

Live data retrieval from the OpenWeather API:

Current weather via WeatherService

Forecast data via ForecastService (5-day / 3-hour feed)

Hourly forecast view:

Displays a subset of upcoming 3-hour forecast entries

Daily forecast summary view:

Groups 3-hour forecast data by day

Calculates daily minimum and maximum temperatures

Favorites management:

Add and remove favorite cities

Favorites persisted locally via a text file (favorites.txt)

Light and dark theme toggle using JavaFX CSS

Temperature unit toggle (Celsius / Fahrenheit)

Alerts panel:

Attempts to retrieve alerts via the OpenWeather API

Falls back to simulated alerts when live data is unavailable

4. Architecture Overview

The project follows a clear separation of concerns and modular structure.

Package structure (actual)

com.school.weatherapp.app

MainApp — JavaFX entry point; builds the scene and wires panel interactions

com.school.weatherapp.config

AppConfig — centralized configuration constants and defaults

com.school.weatherapp.data.models

Weather, Forecast, Alert — data models used by services and UI

com.school.weatherapp.data.services

WeatherService — current weather retrieval and parsing

ForecastService — forecast retrieval and transformation

AlertService — alert retrieval with simulated fallback support

com.school.weatherapp.features

FavoritesService — persistence and retrieval of favorite cities

com.school.weatherapp.ui.panels

Modular UI components:

CurrentWeatherPanel

FavoritesPanel

HourlyForecastPanel

DailyForecastPanel

AlertPanel

com.school.weatherapp.util

Utility helpers for date/time formatting and temperature conversion

JavaFX lifecycle

MainApp extends javafx.application.Application

start(Stage):

Creates the root layout and scene

Loads CSS stylesheets

Initializes and composes UI panels

Wires callbacks between panels

Loads initial data using a default city

5. Weather Data Source

This project uses the OpenWeather API.

Endpoints used

Current weather:

Endpoint: /data/2.5/weather

Implemented in WeatherService

Forecast:

Endpoint: /data/2.5/forecast (5-day / 3-hour)

Implemented in ForecastService

Used to produce hourly and daily views

Alerts:

Implemented in AlertService

May return simulated alerts when live data is unavailable

API key usage

An API key is required to run the application.

The key is read from the environment variable OPENWEATHER_API_KEY

API keys must not be committed to version control

Advanced concerns such as retries, caching, and rate limiting are intentionally out of scope for this POC.

6. Technology Stack

Java 17+

JavaFX

Gradle

OpenWeather API

Gson (for JSON parsing)

JUnit 5 (dependencies included for testing)

7. Getting Started
Prerequisites

JDK 17 or higher

Gradle installed and available in PATH

Internet connection for API calls

API key setup

Set the OpenWeather API key as an environment variable.

Windows (PowerShell)

setx OPENWEATHER_API_KEY "YOUR_KEY_HERE"


macOS / Linux

export OPENWEATHER_API_KEY="YOUR_KEY_HERE"

Build
gradle build

Run
gradle run


A custom Gradle task such as runApp may also be available depending on configuration.

JavaFX notes

JavaFX dependencies are managed through the Gradle JavaFX plugin. Ensure you are using a compatible JDK and that Gradle resolves JavaFX modules correctly.

8. Design Decisions

JavaFX was chosen to demonstrate desktop UI development using a standard Java application lifecycle.

OpenWeather was selected due to its accessible REST API suitable for educational projects.

The UI is split into modular panels to improve readability and maintainability.

The project favors straightforward structure over advanced production patterns such as dependency injection or persistence frameworks.

9. Limitations

Proof-of-concept scope; not production-hardened

Limited automated testing

No caching or offline support

Minimal API resilience (timeouts, retries, rate-limit handling)

Alerts may be simulated depending on API availability

Favorites persistence uses a simple text file rather than a database

10. Future Improvements

Improve error handling and user feedback

Add API resilience features (timeouts, retries, lightweight caching)

Introduce unit and integration testing

Improve UI polish and theming consistency

Cross-platform packaging (e.g., jpackage)

Cleaner externalized configuration management

11. License & Usage

This project is intended for educational and non-commercial use.
If using OpenWeather services, ensure compliance with OpenWeather’s terms of service and applicable rate limits.
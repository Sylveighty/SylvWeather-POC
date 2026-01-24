# SylvWeather-POC

## 1. Project Overview

SylvWeather-POC is a Java 17+ / JavaFX desktop weather dashboard built as a proof-of-concept (POC). The project demonstrates the JavaFX application lifecycle, modular UI composition, and integration with a real external REST API (OpenWeather).

This project prioritizes clarity, structure, and learning value over production concerns such as caching, persistence layers, rate limiting, and exhaustive error handling. It is intended for academic evaluation, technical presentations, and early-career portfolio review.

## 2. UI Preview

> Screenshots to be added.

Planned image locations:

- docs/images/main-dashboard.png
- docs/images/current-weather-and-favorites.png
- docs/images/hourly-and-daily-forecast.png
- docs/images/alerts-panel.png

## 3. Implemented Features

- Search weather by city name and display current conditions
- Live data retrieval from the OpenWeather API
- Hourly forecast view derived from the 5-day / 3-hour forecast feed
- Daily forecast summary with min/max temperature calculation
- Favorites management with local persistence (favorites.txt)
- Light and dark theme toggle using JavaFX CSS
- Temperature unit toggle (Celsius / Fahrenheit)
- Alerts panel with simulated fallback when live alerts are unavailable

## 4. Architecture Overview

### Package Structure

- com.school.weatherapp.app  
  Main JavaFX entry point

- com.school.weatherapp.config  
  Centralized configuration

- com.school.weatherapp.data.models  
  Weather, Forecast, Alert data models

- com.school.weatherapp.data.services  
  API interaction and data parsing

- com.school.weatherapp.features  
  Favorites management

- com.school.weatherapp.ui.panels  
  Modular UI components

- com.school.weatherapp.util  
  Formatting and conversion utilities

### JavaFX Lifecycle

- Application entry via Application.start(Stage)
- Scene and layout setup in MainApp
- Panels initialized and composed
- Initial data loaded using a default city

## 5. Weather Data Source

This project uses the OpenWeather API.

Endpoints used:

- /data/2.5/weather
- /data/2.5/forecast

Alerts may be simulated depending on API availability.

API keys are read from the environment variable OPENWEATHER_API_KEY and must not be committed to version control.

## 6. Technology Stack

- Java 17+
- JavaFX
- Gradle
- OpenWeather API
- Gson
- JUnit 5

## 7. Getting Started

### Quick Start (copy/paste)

```bash
# macOS/Linux
export OPENWEATHER_API_KEY="YOUR_KEY_HERE"

./gradlew build
./gradlew run
```

```powershell
# Windows (PowerShell)
setx OPENWEATHER_API_KEY "YOUR_KEY_HERE"

gradlew.bat build
gradlew.bat run
```

### Prerequisites

- JDK 17+
- Gradle
- Internet connection

### API Key Setup

Set the OpenWeather API key as an environment variable.

### Windows (PowerShell)
setx OPENWEATHER_API_KEY "YOUR_KEY_HERE"

### macOS/Linux
export OPENWEATHER_API_KEY="YOUR_KEY_HERE"

### Build
gradle build

### Run
gradle run

### Favorites Persistence

Favorites are stored in a simple text file (`favorites.txt`) in the project working directory.  
To reset favorites, delete the file and restart the app. This is intentionally lightweight for the POC.

### Troubleshooting

- If the API key is missing or invalid, the UI will show a load error and no data will appear.

## 8. Design Decisions

- JavaFX chosen for desktop UI clarity
- OpenWeather selected for accessible REST endpoints
- Modular panel-based UI structure
- Readability prioritized over production complexity

## 9. Limitations

- Proof-of-concept only
- Limited automated testing
- No caching or offline support
- Minimal API resilience
- Simulated alerts may be shown

## 10. Future Improvements

- Improved error handling
- API resilience enhancements
- Unit and integration testing
- UI polish and theming
- Cross-platform packaging

## 11. License & Usage

Educational and non-commercial use only.  
OpenWeather usage must comply with their terms of service.

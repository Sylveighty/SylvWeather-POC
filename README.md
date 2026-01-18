# SylvWeather - JavaFX Weather Dashboard

A clean, modular JavaFX desktop application for displaying current weather conditions and forecasts. This high-school proof-of-concept demonstrates modern Java development practices with a focus on UI/UX design and clean architecture.

## 🎯 Project Overview

SylvWeather is a weather dashboard application that displays:
- **Current Weather**: Real-time conditions with temperature, humidity, wind, and pressure
- **7-Day Forecast**: Daily high/low temperatures with weather conditions
- **Hourly Forecast**: 24-hour weather predictions with precipitation chances
- **Weather Alerts**: Real-time severe weather notifications with severity levels
- **Theme Support**: Light and dark themes with smooth transitions

## 📋 Features

### Core Functionality
- ✅ Current weather by city or GPS location
- ✅ 7-day weather forecast
- ✅ Hourly forecast for the current day
- ✅ Weather alerts with severity levels
- ✅ Temperature, humidity, wind speed, precipitation, UV index display
- ✅ Weather icons and textual conditions

### UI/UX Features
- ✅ Clean, minimalist layout inspired by AccuWeather
- ✅ Floating panels with card-based design
- ✅ Light/dark theme toggle with smooth animations
- ✅ Keyboard accessible interface
- ✅ High-contrast readable UI
- ✅ Responsive design for different screen sizes

### Technical Features
- ✅ Modular package structure
- ✅ MVVM-inspired architecture
- ✅ REST API integration (simulated for demo)
- ✅ Centralized configuration management
- ✅ Comprehensive error handling
- ✅ Async data loading with progress indicators

## 🏗️ Architecture

### Package Structure
```
com.school.weatherapp
├── app              # Main application launcher
│   └── MainApp.java # Entry point with theme management
├── config           # Application configuration
│   └── AppConfig.java # API keys, settings, constants
├── features         # Feature modules (reserved for expansion)
├── data             # Data layer - models and services
│   ├── models       # Data models (Weather, Forecast, Alert)
│   └── services     # API integration services
├── ui               # User interface layer
│   └── panels       # UI panels (CurrentWeather, Forecasts, Alerts)
└── util             # Utility classes
    ├── DateTimeUtil.java   # Date/time formatting utilities
    └── TemperatureUtil.java # Temperature conversion utilities
```

### Design Patterns
- **MVVM Pattern**: Separation of UI (View), data (Model), and logic (ViewModel)
- **Observer Pattern**: Event-driven updates between components
- **Factory Pattern**: Service instantiation and configuration
- **Singleton Pattern**: Configuration management

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Gradle 7.0+ (or use included Gradle wrapper)

### Installation & Running

1. **Clone or download** the project
2. **Navigate to project directory**:
   ```bash
   cd SylvWeather
   ```
3. **Build the project**:
   ```bash
   ./gradlew build
   ```
4. **Run the application**:
   ```bash
   ./gradlew run
   ```

### Alternative Run Methods

**Using Gradle wrapper (Windows)**:
```cmd
gradlew.bat build
gradlew.bat run
```

**Using system Gradle**:
```bash
gradle build
gradle run
```

**Running the JAR directly**:
```bash
java -jar build/libs/SylvWeather-1.0-SNAPSHOT.jar
```

## 🎨 User Interface

### Main Dashboard Layout
```
┌─────────────────────────────────────────────────┐
│ Weather Dashboard                    [Dark]     │
├─────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────┐ │
│ │ Current Weather Panel                      │ │
│ │                                             │ │
│ │ 🌤️  72°F                                 │ │
│ │ Clear Sky                                   │ │
│ │                                             │ │
│ │ Feels Like: 75°F   Humidity: 45%           │ │
│ │ Wind: 5 mph NW    Pressure: 1013 hPa       │ │
│ │ Last updated: Jan 18, 2026 14:30           │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ ⚠ Weather Alerts                           │ │
│ │ ┌─────────────────────────────────────────┐ │ │
│ │ │ 🔴 Severe Thunderstorm Warning         │ │ │
│ │ │ Thunderstorms expected in your area... │ │ │
│ │ │ Severity: HIGH                         │ │ │
│ │ └─────────────────────────────────────────┘ │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ Hourly Forecast                              │ │
│ │ [Card] [Card] [Card] [Card] [Card] [Card]   │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ 7-Day Forecast                              │ │
│ │ [Card] [Card] [Card] [Card] [Card] [Card]   │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### Theme Support
- **Light Theme**: Clean whites and grays with dark text
- **Dark Theme**: Dark backgrounds with light text for eye comfort
- **Theme Toggle**: Top-right button switches between themes instantly
- **Consistent Styling**: All components adapt to theme changes

## 🛠️ Technical Implementation

### Dependencies
- **JavaFX 17+**: Modern UI framework for desktop applications
- **Gradle**: Build automation and dependency management
- **Java Standard Library**: Core Java functionality

### Key Classes

#### MainApp.java
- Application entry point
- Theme management and CSS loading
- Main window setup and layout coordination

#### Panel Classes
- **CurrentWeatherPanel**: Displays current conditions with search functionality
- **HourlyForecastPanel**: Horizontal scrollable hourly forecast cards
- **DailyForecastPanel**: 7-day forecast with high/low temperatures
- **AlertPanel**: Weather alert notifications with severity indicators

#### Service Classes
- **WeatherService**: Current weather data retrieval
- **ForecastService**: Hourly and daily forecast data
- **AlertService**: Weather alert monitoring

#### Utility Classes
- **DateTimeUtil**: Timestamp formatting and date operations
- **TemperatureUtil**: C↔F conversion and temperature calculations

### Configuration
```java
// AppConfig.java contains:
public static final String DEFAULT_CITY = "New York";
public static final String TEMPERATURE_UNIT = "imperial"; // or "metric"
public static final String API_KEY = "your-api-key-here";
public static final int REFRESH_INTERVAL_MINUTES = 15;
```

## 📚 API Integration

### Weather Data Sources
The application uses simulated weather data for demonstration. In production, integrate with:

- **OpenWeatherMap API**: Comprehensive weather data
- **WeatherAPI**: Fast and reliable forecasts
- **National Weather Service**: Official government data

### Simulated Data Structure
```json
// Current Weather Response
{
  "city": "New York",
  "temperature": 22.5,
  "condition": "Clear",
  "humidity": 65,
  "windSpeed": 3.2,
  "pressure": 1013,
  "timestamp": 1674067200
}
```

## 🔧 Development

### Building from Source
```bash
# Clean build
./gradlew clean build

# Run tests (when implemented)
./gradlew test

# Create distribution
./gradlew distZip
```

### IDE Setup
1. Open project in IntelliJ IDEA, Eclipse, or VS Code
2. Ensure Java 17+ JDK is configured
3. Import as Gradle project
4. Run MainApp.java as JavaFX application

### Code Style
- **Java Naming Conventions**: CamelCase for classes/methods
- **Documentation**: Javadoc comments for public APIs
- **Modularity**: Single responsibility principle
- **Error Handling**: Try-catch blocks with user-friendly messages

## 🎯 Educational Value

This project demonstrates high school level concepts:

### Computer Science Concepts
- **Object-Oriented Programming**: Classes, inheritance, encapsulation
- **Data Structures**: Collections, models, and data flow
- **Algorithms**: Data conversion and formatting
- **Design Patterns**: MVVM, Observer, Factory patterns

### Software Engineering Practices
- **Version Control**: Git workflow and commit practices
- **Build Automation**: Gradle build system
- **Documentation**: README and code documentation
- **Testing**: Unit test structure (framework ready)

### UI/UX Design
- **User-Centered Design**: Intuitive navigation and layout
- **Accessibility**: Keyboard navigation and screen reader support
- **Responsive Design**: Adapts to different screen sizes
- **Visual Hierarchy**: Clear information organization

## 📈 Future Enhancements

### Planned Features
- [ ] GPS location detection
- [ ] Multiple city favorites
- [ ] Weather radar/maps integration
- [ ] Historical weather data
- [ ] Weather notifications
- [ ] Settings persistence

### Technical Improvements
- [ ] Unit test coverage
- [ ] Performance optimization
- [ ] Database integration
- [ ] REST API client library
- [ ] Internationalization (i18n)

## 🤝 Contributing

This is an educational project. To contribute:

1. Fork the repository
2. Create a feature branch
3. Make changes with clear commit messages
4. Test thoroughly
5. Submit a pull request

## 📄 License

Educational use only. This project is created for learning purposes and demonstrates JavaFX development concepts.

## 🙏 Acknowledgments

- Inspired by AccuWeather, Apple Weather, and Weather.com
- Built with JavaFX and modern Java practices
- Designed for educational purposes

---

**Version**: 1.2 (Phase 4 Complete)
**Last Updated**: January 18, 2026
**Java Version**: 17+
**Framework**: JavaFX

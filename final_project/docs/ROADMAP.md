# Roadmap

This roadmap outlines potential improvements for SylvWeather-POC while keeping the project aligned with proof-of-concept scope.

## Short-Term Improvements (Planned)

- Ensure API keys are not committed (use environment variables or local config excluded from git)
- Improve error handling and user-facing feedback for:
  - invalid city
  - network failures
  - missing API key
- Add a small set of unit tests for:
  - JSON parsing
  - forecast grouping logic
  - temperature conversion formatting
- Reduce duplication in inline styles where practical (without changing UI behavior)

## Medium-Term Enhancements (Planned)

- Add request timeouts and clearer failure states in services
- Add lightweight caching (in-memory) to reduce repeated calls during a session
- Improve theming consistency using CSS resources (`theme.css`, `theme-dark.css`)
- Add basic CI checks (build + tests)

## Long-Term Vision (Exploratory)

- Cross-platform packaging (e.g., `jpackage`)
- More robust alert support using an officially supported alerts feed (depending on OpenWeather plan/endpoints)
- Accessibility improvements (keyboard navigation, contrast checks)

## Scope Reminder

SylvWeather-POC is a learning-focused JavaFX proof-of-concept. Roadmap items are optional enhancements and should not be interpreted as production readiness goals.

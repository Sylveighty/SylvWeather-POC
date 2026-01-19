# Roadmap

This roadmap describes potential improvements for SylvWeather-POC while keeping the project aligned with its proof-of-concept scope.

---

## Short-Term (Planned)

- Replace in-source API key with a safer approach:
  - environment variable (preferred)
  - or local config file excluded from version control
- Improve user-facing error messages (invalid city, network failures, missing API key)
- Add a small set of unit tests for:
  - JSON parsing
  - forecast grouping logic
  - temperature conversions
- Ensure repository hygiene:
  - remove `bin/` and `.gradle/` from version control
  - add proper `.gitignore` entries

---

## Medium-Term (Planned)

- Add request timeouts and clearer failure states in services
- Implement optional lightweight caching (in-memory) to reduce repeated calls
- Improve UI consistency:
  - unify styling (optionally use `theme.css` / `theme-dark.css` consistently)
  - reduce inline style duplication
- Add basic CI checks:
  - build
  - run tests

---

## Long-Term (Exploratory Ideas)

- Cross-platform packaging (e.g., `jpackage`)
- More robust alerts support using an officially supported OpenWeather alerts feed (if available for the chosen plan/endpoints)
- Offline-friendly mode (cached last-known results)
- Accessibility improvements and keyboard navigation
- Modularization for potential future portability to other platforms

---

## Scope Reminder

SylvWeather-POC remains a learning-focused JavaFX proof-of-concept. The items above are optional enhancements and should not be interpreted as production readiness goals.

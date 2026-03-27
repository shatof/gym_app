# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint check
./gradlew lint

# Clean
./gradlew clean
```

Build settings: minSdk 26, targetSdk 34, Kotlin 1.9.20, JDK 17, Compose BOM 2023.10.01, KSP for Room annotation processing.

## Architecture

**MVVM + Repository pattern** with a single-module Android app.

```
Data flow: Room DB → DAOs → GymRepository → ViewModels (StateFlow/Flow) → Compose UI
```

**`GymTrackerApp.kt`** — Application class that lazily initializes the database and repository singleton, exposed as `database` and `repository` properties. ViewModels access the repository via `ViewModelProvider.Factory` referencing the Application instance.

**Data layer** (`data/`):
- `GymDatabase.kt` — Room database, currently at version 5, with 6 entities. Migration must be handled manually when adding entities or columns.
- `SettingsManager.kt` — DataStore preferences for theme color, dark mode, custom exercises list, and profile settings.
- `repository/GymRepository.kt` — Single source of truth. All business logic (1RM calculation, volume aggregation, etc.) lives here, not in ViewModels.

**Entities and relationships:**
- `Workout` → `Exercise` → `ExerciseSet` (cascade delete)
- `SessionTemplate` → `TemplateExercise`
- `Measurement` (standalone body measurements)

**UI layer** (`ui/`):
- Navigation uses `HorizontalPager` (swipe between tabs) with a bottom `NavigationBar`. Screens are indexed by position, not by named routes — see `Navigation.kt`.
- 5 screens: Workout (active session), History, Progress (charts via Vico), Templates, Settings.
- Each screen has a corresponding ViewModel. `WorkoutViewModel` is the most complex — it manages live session state, debounced DB writes for set edits, and superset grouping.

**Key domain concepts:**
- **Mioreps**: A training technique where 1 miorep = 1/3 effective rep. Stored as a separate field on `ExerciseSet`.
- **1RM estimation**: Epley formula (`weight × (1 + reps/30)`), computed in the repository.
- **Templates**: When starting a session from a template, the previous session's weights/reps are pre-filled automatically.

## Key Libraries

- **Room** v2.6.1 with KSP (not KAPT)
- **Vico** v1.13.1 — charting library used in ProgressScreen
- **Gson** — JSON export/import
- **DataStore Preferences** v1.0.0 — replaces SharedPreferences for settings
- **Coil** v2.5.0 — image loading (profile photo)
- **Material 3** — design system throughout

## Database Migrations

When modifying `GymDatabase.kt`, increment `version` and add a `Migration` object. The database uses `fallbackToDestructiveMigration()` is NOT set — missing migrations will crash. Always add explicit migrations or use `fallbackToDestructiveMigration()` during development only.

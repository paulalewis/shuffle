# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.castlefrog.shuffle.ExampleUnitTest"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

## Architecture

This is an Android app using Jetpack Compose and MVVM.

**Key patterns:**
- `ShuffleApplication` is the DI root — it instantiates `AnalyticsLogger` and `ShuffleListRepository` and exposes them via extension functions on `Context` (`getAnalyticsLogger()`, `getShuffleListRepository()`). `MainActivity` retrieves these and passes them into `MainViewModelFactory`.
- `MainViewModel` uses a unidirectional data flow pattern: `UiEvent` → `handleUiEvent()` → updates `_uiState` (a `StateFlow<UiState>`) or emits one-shot `_uiAction` (a `StateFlow<UiAction>` that resets to `UiAction.None` immediately after being set).
- `UiState` is a sealed class hierarchy with `MainView` (Loading / Empty / ShuffleView / EditListView) and optional `OverlayView` (bottom sheets/dialogs).
- `ShuffleListRepository` is an interface — the implementation (Room-backed) is stubbed out in `ShuffleApplication` and not yet wired up.

**Core domain model:**
- `ShuffleList` — a named list with a `subsetSize` and a list of `ShuffleItem`s. The app randomly selects `subsetSize` items from the list to display.

**Packages:**
- `model/` — plain data classes (`ShuffleList`, `ShuffleItem`, `ShuffleGroup`, `ShuffleColor`)
- `repository/` — `ShuffleListRepository` interface
- `viewmodel/` — `MainViewModel` + `MainViewModelFactory`
- `view/` — Composable screen/component functions
- `analytics/` — `AnalyticsLogger` interface + extension functions for typed event logging; debug impl logs via Timber, production impl (Firebase) is commented out
- `ui/theme/` — Material3 theme

**Navigation:** `MainActivity` hosts a `NavigationSuiteScaffold` with three top-level destinations (Home, Favorites, Profile) managed via `rememberSaveable` state. No Navigation Compose graph yet.

**Many `UiEvent` handlers are `TODO()`** — `SelectShuffle`, `ChangeList`, `CreateNewList`, `OpenEditList`, `OpenListNames` are not yet implemented.

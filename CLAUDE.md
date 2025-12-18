# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Robin Android is a modern Android game server browser built with Jetpack Compose. It provides real-time server browsing, multi-language support (English/Chinese), and features advanced search, caching, and auto-refresh capabilities.

## Build and Development Commands

### Building the App
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.example.robin_android.ExampleTest"
```

### Code Quality
```bash
# Run lint checks
./gradlew lint

# Clean build
./gradlew clean

# Generate dependency report
./gradlew app:dependencies
```

### Development Setup
- Open project in Android Studio or use `./gradlew build` from command line
- Minimum SDK: 24, Target SDK: 35
- Uses modern Android Gradle Plugin with Kotlin DSL

## Architecture Overview

### High-Level Architecture
The app follows a **Repository Pattern** with clean separation between UI, data, and business logic:

```
UI Layer (Compose) ↔ Repository Layer ↔ Data Sources (API/Cache)
```

### Key Components

#### 1. **Data Layer (`/data`)**

**SettingsManager**: Singleton that manages app-wide settings including:
- Language preferences with Android 13+ per-app language support
- API region selection (China/Global)
- Uses SharedPreferences for persistence
- Implements reactive state management with Compose `mutableStateOf`

**ServerRepository**: Central data management for:
- Server list fetching with XML parsing
- Map data with JSON parsing and multi-level caching
- Real-time search functionality
- Automatic refresh with configurable intervals

**Data Models**:
- `GameServer`: Server information with players, map details, and metadata
- `MapInfo`: Map metadata with image URLs and display names
- `ServerDetail`: Expanded server information for detailed views

#### 2. **UI Layer (`/ui`)**

**Navigation**: Uses Navigation Compose with a single `NavHost`:
- Main screen with server list
- Settings screen for language/API configuration
- About screen with app information
- First-time setup wizard for initial configuration

**Core Screens**:
- `MainScreen`: Server browsing with real-time search and auto-refresh
- `SettingsScreen`: Language switching and API region selection
- `ServerDetailPanel`: Expandable server information with map preview

**Key UI Patterns**:
- **Real-time Search**: 300ms debounced search with Kotlin Flow
- **Language Switching**: Android 13+ per-app language support with immediate UI updates
- **Caching Strategy**: Memory (5min) + Persistent (24hr) caching for map data
- **State Management**: Compose state with `remember` and `mutableStateOf`

#### 3. **App Structure**

**App.kt**: Entry point that sets up:
- Language-aware context using `CompositionLocalProvider`
- Material 3 theme configuration
- Navigation controller initialization

**MainActivity**: Minimal activity that:
- Sets initial language configuration
- Delegates all UI to Compose

## Technical Implementation Details

### Language Switching
- Uses Android 13+ per-app language API via `AppCompatDelegate.setApplicationLocales()`
- Maps "zh" to "zh-CN" to match `values-zh-rCN` resource folder
- Implements reactive language state with automatic UI recomposition
- Creates locale-specific configuration contexts for immediate updates

### API Integration
- **Server Data**: XML parsing with custom `ServerListXmlParser`
- **Map Data**: JSON parsing with `MapsJsonParser`
- **Multi-region Support**: China (`*.cn`) and Global (`*.com`) endpoints
- **Error Handling**: Graceful fallback to cached data on network failures
- **API Configuration**: Externalized via BuildConfig (see API Configuration section below)

### Caching Strategy
- **Memory Cache**: Fast access with 5-minute expiration
- **Persistent Cache**: SharedPreferences with 24-hour expiration
- **Fallback Chain**: Memory → Persistent → API → Empty list
- **Data Freshness**: Configurable auto-refresh with 5-second intervals

### Search Implementation
- **Real-time Search**: Immediate response as user types
- **Multi-field Search**: Searches across server names, IPs, players, maps, etc.
- **Debouncing**: 300ms delay to prevent excessive API calls
- **Highlighting**: Visual emphasis on matching text fragments

## Development Patterns

### State Management
- Use Compose `remember` for local state
- Use `mutableStateOf` for observable state that triggers recomposition
- Repository pattern for data that needs to persist across screen changes

### Async Operations
- Coroutines with `Dispatchers.IO` for network operations
- `LaunchedEffect` for one-time async operations in Composables
- Flow operators (`debounce`, `distinctUntilChanged`) for stream processing

### Internationalization
- All user-facing strings must use `stringResource()`
- Resource files: `values/` (default/English), `values-zh-rCN/` (Chinese)
- Language codes: "en" for English, "zh" for Chinese (mapped to zh-CN internally)

### Testing Strategy
- Unit tests for repository logic and data parsing
- UI tests for critical user flows
- Mock data should never be used in production (data is time-sensitive)

## Configuration Files

### `gradle/libs.versions.toml`
Centralized dependency management with version catalog. All external libraries should be declared here.

### `app/src/main/res/xml/locales_config.xml`
Declares supported locales for Android 13+ per-app language feature.

### `AndroidManifest.xml`
- Configures internet permission and locale support
- Declares MainActivity with exported="true"

### `gradle.properties`
- Global configuration including API URLs
- Can be overridden by `local.properties` for local development

### `local.properties`
- Local development overrides (not tracked in Git)
- See `local.properties.example` for template

## API Configuration

### Overview
API regions are configured using a flexible format that supports multiple regions with bilingual labels. The configuration is loaded into BuildConfig at compile time.

### Configuration Format
```
id|url|label_en|label_zh;id2|url2|label_en2|label_zh2
```

### Configuration Priority (High to Low)
1. Command-line parameters: `-PAPI_REGIONS="..."`
2. Environment variables: `export API_REGIONS="..."`
3. `local.properties` (local overrides, not in Git)
4. `gradle.properties` (default configuration)
5. Fallback defaults in `build.gradle.kts`

### Default Configuration
```properties
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|中国大陆;global|https://robin.kreedzt.com/|Global|全球
```

### Quick Configuration Methods

**For local development with custom regions:**
```bash
# Option 1: Create local.properties
echo "API_REGIONS=dev|https://dev.api.com/|Development|开发;staging|https://staging.api.com/|Staging|测试" >> local.properties

# Option 2: Command line
./gradlew assembleDebug -PAPI_REGIONS="dev|https://dev.api.com/|Development|开发;staging|https://staging.api.com/|Staging|测试"

# Option 3: Environment variables
export API_REGIONS="dev|https://dev.api.com/|Development|开发;staging|https://staging.api.com/|Staging|测试"
./gradlew assembleDebug
```

**For CI/CD:**
- Configure GitHub Secret: `API_REGIONS`
- Workflow automatically uses this value

### Documentation
All documentation is located in the [docs/](docs/) directory:

- **[docs/README.md](docs/README.md)** - Documentation index and quick start
- **[docs/NEW_API_CONFIGURATION_FORMAT.md](docs/NEW_API_CONFIGURATION_FORMAT.md)** - New flexible API configuration format (bilingual)
- **[docs/FLEXIBLE_API_IMPLEMENTATION_SUMMARY.md](docs/FLEXIBLE_API_IMPLEMENTATION_SUMMARY.md)** - Implementation summary with before/after comparison (bilingual)
- **[docs/ANDROID_STUDIO_SETUP.md](docs/ANDROID_STUDIO_SETUP.md)** - Android Studio setup guide
- **[docs/API_CONFIGURATION_QUICK_REFERENCE.md](docs/API_CONFIGURATION_QUICK_REFERENCE.md)** - Quick reference for configuration scenarios
- **[.github/GITHUB_ACTIONS_SETUP.md](.github/GITHUB_ACTIONS_SETUP.md)** - CI/CD setup guide

## Continuous Integration

### GitHub Actions
The project uses GitHub Actions for automated building and deployment.

**Workflow file**: `.github/workflows/android-ci.yml`

**Triggers**:
- Push to `master` or `develop` branches
- Pull requests to `master`
- Version tags (e.g., `v1.0.0`)

**Build Jobs**:
1. **build**: Lint, test, and build Debug APK (all pushes/PRs)
2. **build-release**: Build Release APK and create GitHub Release (master/tags only)

**Artifacts**:
- Debug APK (retained for 30 days)
- Release APK (retained for 90 days)
- Lint reports (retained for 7 days)

**Creating a Release**:
```bash
# Update version in app/build.gradle.kts
# versionCode = 2
# versionName = "1.0.0"

# Commit and tag
git commit -am "Release v1.0.0"
git tag v1.0.0
git push origin v1.0.0

# GitHub Actions will automatically:
# - Build Release APK
# - Create GitHub Release
# - Upload APK to Release
```

See [.github/GITHUB_ACTIONS_SETUP.md](.github/GITHUB_ACTIONS_SETUP.md) for detailed CI/CD configuration.
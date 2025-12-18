# Implementation Details

This document provides comprehensive implementation details for Robin Android, covering architecture decisions and technical implementation.

## Overview

Robin Android implements a server browser application with a flexible, maintainable architecture using modern Android development practices. The application connects to [rwrs-server](https://github.com/Kreedzt/rwrs-server) to provide real-time server information for Running With Rifles game.

## Architecture

### High-Level Architecture

The application follows the Repository Pattern with clean separation of concerns:

```
UI Layer (Compose) → Repository Layer → Data Sources (API/Cache)
```

### Key Components

#### 1. Data Layer

**SettingsManager**: Singleton managing application-wide settings
- Language preferences with Android 13+ per-app language support
- API region selection with dynamic configuration
- SharedPreferences persistence
- Reactive state management with Compose

**ServerRepository**: Central data management
- Server list fetching with XML parsing
- Map data with JSON parsing and caching
- Real-time search functionality
- Automatic refresh with configurable intervals

**Data Models**:
- `GameServer`: Server information with players, maps, metadata
- `MapInfo`: Map metadata with image URLs and display names
- `ServerDetail`: Expanded server information for detailed views

#### 2. UI Layer

**Navigation**: Single-activity architecture with Navigation Compose
- Main screen with server list
- Settings screen for configuration
- About screen with application information
- First-time setup wizard for initial configuration

**Core Screens**:
- `MainScreen`: Server browsing with real-time search and auto-refresh
- `SettingsScreen`: Language and API region configuration
- `ServerDetailPanel`: Expandable server information with map preview

**UI Patterns**:
- Real-time Search: 300ms debounced search with Kotlin Flow
- Language Switching: Android 13+ per-app language with immediate UI updates
- Caching Strategy: Memory (5min) + Persistent (24hr) caching for map data
- State Management: Compose state with `remember` and `mutableStateOf`

#### 3. App Structure

**App.kt**: Entry point that sets up:
- Language-aware context using `CompositionLocalProvider`
- Material 3 theme configuration
- Navigation controller initialization

**MainActivity**: Minimal activity that:
- Sets initial language configuration
- Delegates all UI to Compose

## Technical Implementation

### Language Support

**Implementation**:
- Uses Android 13+ per-app language API via `AppCompatDelegate.setApplicationLocales()`
- Maps "zh" to "zh-CN" to match `values-zh-rCN` resource folder
- Implements reactive language state with automatic UI recomposition
- Creates locale-specific configuration contexts for immediate updates

### API Integration

**Configuration System**:
- Flexible multi-region configuration with bilingual labels
- Format: `id|url|label_en|label_zh;id2|url2|label_en2|label_zh2`
- Multiple configuration methods with priority ordering
- BuildConfig generation at compile time

**Data Processing**:
- **Server Data**: XML parsing with custom `ServerListXmlParser`
- **Map Data**: JSON parsing with `MapsJsonParser`
- **Error Handling**: Graceful fallback to cached data on network failures

### Caching Strategy

**Two-Tier Caching**:
1. **Memory Cache**: Fast access with 5-minute expiration
2. **Persistent Cache**: SharedPreferences with 24-hour expiration
3. **Fallback Chain**: Memory → Persistent → API → Empty list

**Benefits**:
- Reduces network requests
- Provides offline capability
- Improves response time
- Saves battery life

### Search Implementation

**Real-time Search Architecture**:
- Immediate response as user types
- Multi-field search across servers, players, maps
- Debouncing: 300ms delay to prevent excessive API calls
- Result highlighting with visual emphasis

### Background Updates

**Auto-refresh System**:
- Configurable refresh intervals (default: 5 seconds)
- Lifecycle-aware updates (pause when app is backgrounded)
- User control through settings

## Implementation Details

### Settings Management

```kotlin
class SettingsManager private constructor(private val context: Context) {
    // API Region Configuration
    val API_REGIONS: List<ApiRegionConfig> by lazy {
        ApiRegionConfig.parseFromString(BuildConfig.API_REGIONS_CONFIG)
    }

    // Reactive state management
    val languageState = mutableStateOf(DEFAULT_LANGUAGE)
    val themeState = mutableStateOf(ThemeMode.SYSTEM)

    // Language switching
    fun applyLanguage(languageCode: String) {
        val localeCode = when (languageCode) {
            "zh" -> "zh-CN"
            else -> languageCode
        }

        val locale = Locale.forLanguageTag(localeCode)
        Locale.setDefault(locale)

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(localeCode)
        )
    }
}
```

### Repository Implementation

```kotlin
class ServerRepository(
    private val context: Context,
    private val settingsManager: SettingsManager
) {
    private val baseUrl: String get() = settingsManager.apiBaseUrl

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    private val apiService: RobinApiService = retrofit.create(RobinApiService::class.java)

    suspend fun getServers(): List<GameServer> {
        return try {
            val response = apiService.getServerList()
            parseServerList(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch servers", e)
            getCachedServers()
        }
    }
}
```

### UI Implementation

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val servers by viewModel.servers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(servers) { server ->
            ServerListItem(
                server = server,
                onClick = {
                    navController.navigate("serverDetail/${server.id}")
                }
            )
        }
    }
}
```

## Development Patterns

### State Management

- Use `remember` for local state within composables
- Use `mutableStateOf` for observable state that triggers recomposition
- Repository pattern for data that needs persistence across screen changes
- `collectAsState()` for Flow to State conversions

### Asynchronous Operations

- Coroutines with `Dispatchers.IO` for network operations
- `LaunchedEffect` for one-time async operations in composables
- Flow operators (`debounce`, `distinctUntilChanged`) for stream processing

### Internationalization

- All user-facing strings must use `stringResource()`
- Resource files: `values/` (default/English), `values-zh-rCN/` (Chinese)
- Language codes: "en" for English, "zh" for Chinese

### Testing Strategy

- Unit tests for repository logic and data parsing
- UI tests for critical user flows
- Mock data should not be used in production (data is time-sensitive)

## Configuration

### Gradle Configuration

```kotlin
android {
    defaultConfig {
        // API Regions Configuration
        val apiRegionsConfig = project.findProperty("API_REGIONS")?.toString()
            ?: System.getenv("API_REGIONS")
            ?: "china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646;global|https://robin.kreedzt.com/|Global|\u5168\u7403"

        buildConfigField("String", "API_REGIONS_CONFIG", "\"${apiRegionsConfig.replace("\"", "\\\"")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}
```

### Property Files

**gradle.properties** (global defaults):
```properties
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646;global|https://robin.kreedzt.com/|Global|\u5168\u7403
```

**local.properties** (local overrides, not in Git):
```properties
API_REGIONS=dev|https://dev.api.com/|Development|\u5f00\u53d1
```

## Continuous Integration

### GitHub Actions

**Workflow**: `.github/workflows/android-ci.yml`

**Triggers**:
- Push to `master` or `develop` branches
- Pull requests to `master`
- Version tags (e.g., `v1.0.0`)

**Jobs**:
1. **build**: Lint, test, and build Debug APK
2. **build-release**: Build Release APK and create GitHub Release

**Environment Variables**:
- `API_REGIONS`: API region configuration override
- Sign-related variables for release builds

## Security Considerations

### Network Security
- HTTPS enforced for production endpoints
- Certificate pinning for critical APIs
- Input validation for all network responses

### Data Protection
- No sensitive data stored in plaintext
- Secure SharedPreferences for settings
- Minimal permissions requested

### API Security
- Rate limiting awareness
- Timeout configurations
- Graceful degradation on failures

## Performance Optimization

### Caching Strategy
- Two-tier caching reduces network requests
- Intelligent cache invalidation
- Background data refresh

### Memory Management
- Lazy initialization where appropriate
- Proper cleanup in composables
- Efficient data structures

### Battery Optimization
- Adaptive refresh intervals
- Background task management
- Network request batching

## Maintenance

### Code Organization
- Clear package structure
- Consistent naming conventions
- Comprehensive documentation
- Modular design

### Version Management
- Semantic versioning
- Clear changelog
- Automated release process

### Monitoring
- Crash reporting integration
- Performance metrics
- Usage analytics

---

For API configuration details, see [API Configuration Guide](API_CONFIGURATION.md).
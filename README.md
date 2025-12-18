# Robin Android

A modern Android client for browsing Running With Rifles (RWR) game servers, providing real-time server information and comprehensive search capabilities.

## Overview

Robin Android connects to the [rwrs-server](https://github.com/Kreedzt/rwrs-server) backend to deliver:
- Real-time server browsing with player counts and current maps
- Advanced multi-field search across servers, players, and maps
- Multi-region support with automatic failover
- Bilingual interface (English and Chinese)

## Features

### Core Functionality
- **Server Monitoring**: Track active RWR servers with detailed information
- **Real-time Updates**: Automatic refresh every 5 seconds
- **Smart Search**: Multi-field search with 300ms debouncing
- **Caching System**: Two-tier caching (memory: 5min, persistent: 24hr)

### Architecture
- **Modern Android**: Built with Jetpack Compose and Material 3
- **Repository Pattern**: Clean separation of concerns
- **Coroutines**: Asynchronous operations with proper thread management
- **Flexible Configuration**: Externalized API region support

## Quick Start

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- Android SDK (API 26+)
- JDK 17

### Building

```bash
# Clone the repository
git clone https://github.com/your-username/robin-android.git
cd robin-android

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

### Running
1. Open the project in Android Studio
2. Sync Gradle files
3. Run on device or emulator
4. Configure API region on first launch

## Configuration

Robin Android supports flexible API region configuration:

### Default Regions
```properties
# gradle.properties
# Note: Chinese characters use Unicode escape sequences to avoid encoding issues
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646;global|https://robin.kreedzt.com/|Global|\u5168\u7403
```

### Format Specification
```
id|url|label_en|label_zh;id2|url2|label_en2|label_zh2
```

**Important**: When using Chinese characters in configuration files, use Unicode escape sequences:
- `中国大陆` becomes `\u4e2d\u56fd\u5927\u9646`
- `全球` becomes `\u5168\u7403`
- This prevents encoding issues during Gradle processing

### Configuration Methods

1. **Properties File** (gradle.properties)
2. **Local Override** (local.properties, not tracked)
3. **Environment Variables** (`export API_REGIONS="..."`)
4. **Command Line** (`./gradlew assembleDebug -PAPI_REGIONS="..."`)

#### Unicode Encoding Requirements
**Important**: When configuring Chinese labels in properties files, use Unicode escape sequences:

```properties
# ❌ Incorrect - will cause encoding issues
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|中国大陆

# ✅ Correct - uses Unicode escapes
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646
```

**Conversion Tools**:
- Java: `native2ascii -encoding UTF-8 input.txt output.txt`
- Online: Various Unicode converter tools
- Reference: Use the examples in `local.properties.example`

For detailed configuration options, see the [documentation](docs/).

## Development

### Project Structure
```
app/src/main/java/com/kreedzt/robin/
├── data/           # Data layer
│   ├── SettingsManager.kt      # App settings management
│   ├── ServerRepository.kt      # Data repository
│   └── ApiRegionConfig.kt       # API region configuration
├── ui/            # UI layer
│   ├── MainScreen.kt            # Server browsing interface
│   ├── SettingsScreen.kt       # Settings configuration
│   └── FirstLaunchSetup.kt      # Initial setup wizard
└── App.kt         # Application entry point
```

### Key Technologies
- **Jetpack Compose**: Modern declarative UI framework
- **Navigation Compose**: Single-activity navigation
- **Retrofit 2**: HTTP client with XML/JSON converters
- **Coroutines**: Asynchronous programming
- **SharedPreferences**: Settings persistence

### Build Configuration
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Android 14)
- **Compile SDK**: 36 (Android 14)

## Continuous Integration

GitHub Actions provides automated building and deployment:

### Triggers
- Push to `master` or `develop` branches
- Pull requests to `master`
- Version tags (v1.0.0, v2.1.3, etc.)

### Build Jobs
- **build**: Lint, test, and build Debug APK
- **build-release**: Build Release APK and create GitHub Release

### Creating Releases
```bash
# Update version
versionCode=2
versionName=1.0.0

# Commit and tag
git commit -am "Release v1.0.0"
git tag v1.0.0
git push origin v1.0.0
```

## API Integration

Robin Android integrates with the [rwrs-server](https://github.com/Kreedzt/rwrs-server) backend, which provides a stable API proxy for official game server information:

- **Server Data**: XML parsing from `/api/server_list` (originating from official RWR servers)
- **Map Information**: JSON parsing from `/api/maps` (originating from official RWR servers)
- **Error Handling**: Graceful degradation with cached data
- **Regional Support**: China and global endpoints with automatic failover
- **Data Transparency**: All data ultimately comes from official Running With Rifles game servers

## Localization

The application supports:
- **English**: Primary language
- **Chinese (Simplified)**: Simplified Chinese (zh-CN)
- **Dynamic Switching**: Runtime language changes without app restart
- **Android 13+**: Per-app language support

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Use Compose best practices
- Include tests for new features
- Update documentation

## Disclaimer

**Important Notice**: Robin Android is an unofficial, community-developed application that is not affiliated with, endorsed by, or authorized by the creators or publishers of Running With Rifles.

### Data Source and Usage
- **Official Game Data**: All server data originates from official Running With Rifles game servers
- **API Proxy**: Data is accessed through [rwrs-server](https://github.com/Kreedzt/rwrs-server), which provides a stable API proxy for the official server information
- **No Modification**: We do not modify, alter, or manipulate the game data in any way
- **Read-Only Access**: This application only reads publicly available server information
- **Community Purpose**: Built for the RWR community to enhance server browsing experience

### Legal Notice
This project is provided "as is" without any warranties, express or implied. The developers assume no responsibility for:
- Accuracy of server information (data is provided as-is from the API)
- Service availability or network connectivity
- Any misuse of this application

Running With Rifles is a trademark of its respective owners. All game-related content, trademarks, and materials belong to their respective owners.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Kreedzt/rwrs-server](https://github.com/Kreedzt/rwrs-server) - Backend API provider
- Android Jetpack team for Compose and architecture components
- The RWR community for feedback and suggestions

---

**Robin Android** - Your window into the Running With Rifles universe.
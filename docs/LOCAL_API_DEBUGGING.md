# Local API Debugging Guide

This guide explains how to configure and use a local API for debugging in the Robin Android app.

## Problem

Android 9 (API 28) and above blocks cleartext HTTP traffic by default. When connecting to a local HTTP server (e.g., `http://127.0.0.1:5800/`), you may encounter:

```
CLEARTEXT communication to 127.0.0.1 not permitted by network security policy
```

## Solution

### 1. Network Security Configuration

The app uses separate network security policies:

- **Debug build** (`app/src/debug/res/xml/network_security_config.xml`):
  - Allows all cleartext traffic
  - Allows local IPs (127.0.0.1, localhost, 10.0.2.2, etc.)
  - Allows local subnets (192.168.x.x, 10.x.x.x)

- **Release build** (`app/src/release/res/xml/network_security_config.xml`):
  - Blocks cleartext traffic (default)
  - Enforces HTTPS

### 2. Build Type Configuration

Configured in `build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        manifestPlaceholders["usesCleartextTraffic"] = "true"
    }
    release {
        manifestPlaceholders["usesCleartextTraffic"] = "false"
    }
}
```

### 3. API Region Configuration

Local API region is pre-configured in `gradle.properties`:

```properties
API_REGIONS=local|http://127.0.0.1:5800/|Local|Local;global|https://robin.kreedzt.com/|Global|Global;china|https://robin.kreedzt.cn/|China Mainland|China Mainland
```

## Usage

### Method 1: In-App Settings

1. Run the Debug build
2. Go to Settings
3. Select "API Region"
4. Choose "Local"

### Method 2: gradle.properties

1. Add to `gradle.properties`:
   ```properties
   API_REGIONS=local|http://127.0.0.1:5800/|Local|Local
   ```

2. Rebuild:
   ```bash
   ./gradlew clean build
   ```

### Method 3: Command Line

```bash
./gradlew assembleDebug -PAPI_REGIONS="local|http://127.0.0.1:5800/|Local|Local"
```

### Method 4: Environment Variable

```bash
export API_REGIONS="local|http://127.0.0.1:5800/|Local|Local"
./gradlew assembleDebug
```

## Local Server Setup

Ensure your local API server:
1. Runs on `127.0.0.1:5800`
2. Allows CORS
3. Provides correct API endpoints

Example endpoints:
- Server list: `http://127.0.0.1:5800/servers`
- Map data: `http://127.0.0.1:5800/maps.json`

## Debugging Tips

### Android Emulator

Use `10.0.2.2` instead of `127.0.0.1`:

```properties
API_REGIONS=local|http://10.0.2.2:5800/|Local|Local
```

### Check Network Connectivity

```bash
adb shell ping 127.0.0.1
adb shell telnet 127.0.0.1 5800
```

### View Network Logs

In Logcat, search for:
- `okhttp` - HTTP requests
- `NetworkSecurityConfig` - Security policy

### Chrome DevTools

1. Open `chrome://inspect`
2. Select your app
3. View Network tab

## Troubleshooting

**Q: Still getting CLEARTEXT error**
- Ensure Debug build is running
- Verify network security config is correct
- Restart the app

**Q: Cannot connect to local server**
- Check server is running
- Verify port number
- Check firewall settings
- Verify emulator network config

**Q: Production configuration**
- Disable cleartext traffic
- Use HTTPS
- Configure proper certificates
- Remove debug config

## Security Notes

⚠️ **Important**:
- Cleartext traffic allowed for Debug builds only
- Production must use HTTPS
- Never expose local API config in production
- Regularly review network security settings

## Related Files

- `app/src/main/res/xml/network_security_config.xml` - Default config
- `app/src/debug/res/xml/network_security_config.xml` - Debug config
- `app/src/release/res/xml/network_security_config.xml` - Release config
- `app/src/main/AndroidManifest.xml` - App manifest
- `gradle.properties` - Gradle properties
- `build.gradle.kts` - Build config
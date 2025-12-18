# API Configuration Guide

This document explains how to configure API regions in Robin Android, supporting multiple endpoints with custom labels.

## ⚠️ Important: Unicode Encoding Requirements

**When using Chinese characters in configuration files, you MUST use Unicode escape sequences** to avoid encoding issues:

```properties
# ❌ Incorrect - will cause encoding issues
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|中国大陆

# ✅ Correct - uses Unicode escapes
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646
```

### Common Unicode Conversions
- `中国大陆` → `\u4e2d\u56fd\u5927\u9646`
- `全球` → `\u5168\u7403`
- `开发环境` → `\u5f00\u53d1\u73af\u5883`
- `本地服务器` → `\u672c\u5730\u670d\u52a1\u5668`
- `测试` → `\u6d4b\u8bd5`
- `生产` → `\u751f\u4ea7`

### Conversion Tools
- **Java**: `native2ascii -encoding UTF-8 input.txt output.txt`
- **Online**: Various Unicode converter tools
- **Reference**: See `local.properties.example` for complete examples

## Overview

Robin Android uses a flexible configuration system that allows you to define multiple API regions with bilingual labels. The configuration is loaded into BuildConfig at compile time and can be overridden through multiple methods.

## Configuration Format

### Syntax
```
id|url|label_en|label_zh;id2|url2|label_en2|label_zh2
```

### Fields
| Field | Description | Example |
|-------|-------------|---------|
| `id` | Unique identifier for the region | `china`, `global`, `dev` |
| `url` | API base URL (must end with `/`) | `https://api.example.com/` |
| `label_en` | English label for UI display | `China Mainland` |
| `label_zh` | Chinese label for UI display | `中国大陆` |

### Separators
- **Semicolon (`;`)**: Separates different regions
- **Pipe (`|`)**: Separates fields within a region

## Examples

### Default Configuration (2 regions)
```properties
# Note: Chinese characters use Unicode escape sequences
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646;global|https://robin.kreedzt.com/|Global|\u5168\u7403
```

### Development Environment (3 regions)
```properties
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646;global|https://robin.kreedzt.com/|Global|\u5168\u7403;dev|https://dev.api.com/|Development|\u5f00\u53d1\u73af\u5883
```

### Local Testing (single region)
```properties
API_REGIONS=local|http://192.168.1.100:3000/|Local Server|\u672c\u5730\u670d\u52a1\u5668
```

### Multiple Environments
```properties
API_REGIONS=staging|https://staging.api.com/|Staging|\u6d4b\u8bd5;prod|https://api.com/|Production|\u751f\u4ea7
```

## Configuration Methods

### 1. Gradle Properties (Global Default)

File: `gradle.properties`

```properties
API_REGIONS=china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646;global|https://robin.kreedzt.com/|Global|\u5168\u7403
```

### 2. Local Properties (Development Override)

File: `local.properties` (not tracked in Git)

```properties
API_REGIONS=dev|https://dev.api.com/|Development|\u5f00\u53d1;staging|https://staging.api.com/|Staging|\u6d4b\u8bd5
```

### 3. Environment Variables

```bash
export API_REGIONS="china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646;global|https://robin.kreedzt.com/|Global|\u5168\u7403"
./gradlew assembleDebug
```

### 4. Command Line Parameters

```bash
./gradlew assembleDebug \
  -PAPI_REGIONS="dev|https://dev.api.com/|Development|\u5f00\u53d1;staging|https://staging.api.com/|Staging|\u6d4b\u8bd5"
```

### 5. GitHub Actions (CI/CD)

Configure in GitHub repository secrets:

- **Secret Name**: `API_REGIONS`
- **Value**: `china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646;global|https://robin.kreedzt.com/|Global|\u5168\u7403`

## Configuration Priority

The configuration is resolved with the following priority (high to low):

1. Command-line parameters (`-PAPI_REGIONS="..."`)
2. Environment variables (`export API_REGIONS="..."`)
3. `local.properties` (local overrides, not in Git)
4. `gradle.properties` (default configuration)
5. Fallback defaults in `build.gradle.kts`

## Common Use Cases

### Development with Local Server

```properties
# local.properties
API_REGIONS=local|http://192.168.1.100:3000/|Local Server|\u672c\u5730\u670d\u52a1\u5668
```

### Testing with Staging Environment

```properties
# local.properties
API_REGIONS=staging|https://staging.api.com/|Staging|\u6d4b\u8bd5;prod|https://api.com/|Production|\u751f\u4ea7
```

### Production Deployment

```bash
# Environment or command line
export API_REGIONS="china|https://robin.kreedzt.cn/|China Mainland|\u4e2d\u56fd\u5927\u9646;global|https://robin.kreedzt.com/|Global|\u5168\u7403"
```

## Verification

### Check BuildConfig

After building, inspect the generated BuildConfig:

```java
// app/build/generated/source/buildConfig/debug/com/kreedzt/robin/BuildConfig.java
public static final String API_REGIONS_CONFIG = "china|https://robin.kreedzt.cn/|...;global|https://robin.kreedzt.com/|...";
```

### In-App Verification

1. Run the application
2. Open Settings
3. Check API Region section
4. Verify regions are displayed correctly

## Technical Implementation

### Data Class

```kotlin
data class ApiRegionConfig(
    val id: String,
    val url: String,
    val labelEn: String,
    val labelZh: String
) {
    fun getLabel(languageCode: String): String {
        return when (languageCode) {
            "zh" -> labelZh
            else -> labelEn
        }
    }
}
```

### Parsing Logic

```kotlin
fun parseFromString(configString: String?): List<ApiRegionConfig> {
    if (configString.isNullOrBlank()) return DEFAULT_REGIONS

    return try {
        configString
            .split(";")
            .filter { it.isNotBlank() }
            .mapNotNull { regionStr ->
                val parts = regionStr.split("|")
                if (parts.size == 4) {
                    ApiRegionConfig(
                        id = parts[0].trim(),
                        url = parts[1].trim(),
                        labelEn = parts[2].trim(),
                        labelZh = parts[3].trim()
                    )
                } else null
            }
            .takeIf { it.isNotEmpty() } ?: DEFAULT_REGIONS
    } catch (e: Exception) {
        Log.e("ApiRegionConfig", "Failed to parse config", e)
        DEFAULT_REGIONS
    }
}
```

## Best Practices

### URL Format
- Always end URLs with `/`
- Use HTTPS for production environments
- Test endpoints with HTTP where necessary

### Label Guidelines
- Keep labels concise but descriptive
- Use consistent terminology
- Provide accurate translations

### Error Handling
- The system falls back to default configuration on parsing errors
- Invalid regions are automatically excluded
- Minimum of one region is always maintained

### Performance Considerations
- Configuration is parsed once at startup
- Results are cached in memory
- No runtime overhead after initialization

## Troubleshooting

### Configuration Not Applied
1. Verify the configuration string format
2. Check for correct separators (`;` and `|`)
3. Ensure all required fields are present
4. Rebuild the project after changes

### Regions Not Showing
1. Check `BuildConfig.API_REGIONS_CONFIG`
2. Verify no parsing errors in logs
3. Test with simpler configuration first
4. Check for special characters that need escaping

### Localization Issues
1. Verify language codes: `en` and `zh`
2. Check resource files: `values/` and `values-zh-rCN/`
3. Test language switching functionality
4. Ensure proper `locale_config.xml` setup

## Migration Notes

This configuration system was introduced to replace the hardcoded dual-region system. Previous implementations using `API_URL_CHINA` and `API_URL_GLOBAL` are no longer supported.

For projects upgrading from the old system:

1. Convert old endpoints to new format
2. Add descriptive labels
3. Test all configuration methods
4. Update CI/CD pipelines

---

For more detailed information, see the [documentation index](README.md).
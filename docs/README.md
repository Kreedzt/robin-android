# Documentation

This directory contains comprehensive documentation for Robin Android project.

## Documents

### Core Documentation
- **[API Configuration](API_CONFIGURATION.md)** - Guide for configuring API regions with multiple endpoints
- **[Implementation](IMPLEMENTATION.md)** - Technical implementation details and architecture decisions
- **[CI/CD](CI_CD.md)** - Continuous integration and deployment setup

### Legacy Documents
No legacy documents are maintained as the project has not been released yet.

## Quick Start

### For Developers
1. Read the [main README](../README.md) for project overview
2. Configure your local environment following [API Configuration](API_CONFIGURATION.md)
3. Set up CI/CD using [CI/CD](CI_CD.md) guide

### For API Configuration
- Default configuration supports 2 regions (China and Global)
- Configure additional regions using the format: `id|url|label_en|label_zh;...`
- Multiple configuration methods: properties, environment variables, command line

### For CI/CD
- Configure GitHub Secrets for API regions
- Setup follows standard GitHub Actions workflow
- Automatic APK generation for both debug and release

## Project Structure

```
robin-android/
├── docs/               # Core documentation
│   ├── README.md        # Documentation index
│   ├── API_CONFIGURATION.md
│   ├── IMPLEMENTATION.md
│   └── CI_CD.md
├── app/                 # Android app module
├── gradle/              # Gradle configuration
├── .github/             # GitHub workflows
├── CLAUDE.md            # Technical project overview
├── README.md            # Main project README
└── LICENSE              # MIT License
```

## Contributing

When contributing to the documentation:

1. Keep documentation up-to-date with code changes
2. Use clear, concise language
3. Include code examples where appropriate
4. Update the table of contents if adding new sections
5. Maintain consistency across all documents

## Documentation Standards

### Writing Style
- Use clear, professional language
- Include code blocks with syntax highlighting
- Use tables and lists for structured information
- Include examples for common scenarios

### Code Examples
```kotlin
// Example: Configure API regions
val regions = "china|https://api.cn/|China|中国;global|https://api.com/|Global|全球"
```

### Cross-References
- Use relative paths for internal links
- Reference external resources with full URLs
- Include table of contents for longer documents

---

For complete project information, see the [main project README](../README.md).
# CI/CD Configuration Guide

This guide covers the continuous integration and continuous deployment setup for Robin Android using GitHub Actions.

## Overview

Robin Android uses GitHub Actions for automated building, testing, and deployment. The workflow ensures code quality, runs tests, and produces release-ready APKs.

## Workflow Configuration

### File Location
`.github/workflows/android-ci.yml`

### Triggers
```yaml
on:
  push:
    branches: [ master, develop ]    # Push to main or development branch
    tags:
      - 'v*'                        # Version tags (v1.0.0, v2.1.3, etc.)
  pull_request:
    branches: [ master ]          # Pull requests to master
```

### Jobs

#### Build and Test Job (build)
Runs on all pushes and pull requests:
- Lint checks
- Unit tests
- Debug APK build
- Artifact upload

#### Release Build Job (build-release)
Runs only on:
- Push to master branch
- Version tag pushes
- Creates signed or unsigned release APKs
- Generates GitHub Releases

## Required Secrets

Configure in GitHub repository settings (`Settings → Secrets and variables → Actions`):

### API Configuration (Required)
| Secret Name | Value | Purpose |
|------------|-------|---------|
| `API_REGIONS` | API region configuration string | Defines available API endpoints |

**Example Value**:
```
china|https://robin.kreedzt.cn/|China Mainland|中国大陆;global|https://robin.kreedzt.com/|Global|全球
```

### Signing Configuration (Optional for Release)
| Secret Name | Value | Purpose |
|------------|-------|---------|
| `KEYSTORE_BASE64` | Base64-encoded keystore file | APK signing |
| `KEYSTORE_PASSWORD` | Keystore password | Signing verification |
| `KEY_ALIAS` | Key alias | Signing identification |
| `KEY_PASSWORD` | Key password | Signing verification |

### Generating KeyStore Base64

**macOS/Linux**:
```bash
base64 -i your-release-key.jks | pbcopy   # Copy to clipboard
```

**Windows PowerShell**:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("your-release-key.jks")) | Set-Clipboard
```

## Build Process

### Debug Build

```yaml
- name: Build Debug APK
  env:
    API_REGIONS: ${{ secrets.API_REGIONS || 'default-config' }}
  run: |
    ./gradlew assembleDebug \
      -PAPI_REGIONS="${API_REGIONS}"
```

### Release Build

#### Unsigned Release
```yaml
- name: Build Release APK (Unsigned)
  env:
    API_REGIONS: ${{ secrets.API_REGIONS || 'default-config' }}
  run: |
    ./gradlew assembleRelease \
      -PAPI_REGIONS="${API_REGIONS}"
```

#### Signed Release
```yaml
- name: Build Release APK (Signed)
  env:
    API_REGIONS: ${{ secrets.API_REGIONS || 'default-config' }}
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: |
    ./gradlew assembleRelease \
      -PAPI_REGIONS="${API_REGIONS}" \
      -Pandroid.injected.signing.store.file="${KEYSTORE_FILE}" \
      -Pandroid.injected.signing.store.password="${KEYSTORE_PASSWORD}" \
      -Pandroid.injected.signing.key.alias="${KEY_ALIAS}" \
      -Pandroid.injected.signing.key.password="${KEY_PASSWORD}"
```

## Artifact Management

### Generated Artifacts

| Artifact Type | Retention | Description |
|---------------|----------|-------------|
| Debug APK | 30 days | Development builds |
| Release APK | 90 days | Production builds |
| Lint Reports | 7 days | Code quality reports |

### Downloading Artifacts

#### GitHub Web Interface
1. Navigate to repository Actions tab
2. Select workflow run
3. Download from Artifacts section

#### GitHub CLI
```bash
# List recent runs
gh run list --workflow=android-ci.yml --limit 5

# Download specific artifact
gh run download <run-id> -n app-release-<commit-sha>
```

## Release Management

### Creating Releases

#### Method 1: Git Tags
```bash
# Create and push tag
git tag v1.0.0
git push origin v1.0.0

# This triggers:
# 1. build-release job
# 2. GitHub Release creation
# 3. APK upload to Release
```

#### Method 2: GitHub Web Interface
1. Go to repository Releases page
2. Click "Draft a new release"
3. Enter tag: `v1.0.0`
4. Click "Publish release"

### Version Naming

Use semantic versioning:
- `v1.0.0` - Major release
- `v1.1.0` - Minor release (new features)
- `v1.0.1` - Patch release (bug fixes)
- `v1.0.0-beta.1` - Pre-release

## Build Optimization

### Caching Strategy
```yaml
- name: Cache Gradle packages
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

### Parallel Jobs
- Jobs run independently when possible
- Build and test jobs can run in parallel
- Release job depends on build job success

## Environment Configuration

### Java Development Kit
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: gradle
```

### Android SDK
- Android SDK is part of Android build tools
- No separate SDK installation required
- Gradle handles SDK downloading automatically

## Testing Strategy

### Unit Tests
```yaml
- name: Run Unit Tests
  run: ./gradlew testDebugUnitTest
```

### Lint Checks
```yaml
- name: Run Lint
  run: ./gradlew lint
```

### Instrumentation Tests (Optional)
```yaml
- name: Run Instrumentation Tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 29
    script: ./gradlew connectedDebugTest
```

## Troubleshooting

### Common Issues

#### Gradle Build Failures
**Symptoms**: Build errors, dependency issues

**Solutions**:
```yaml
- name: Clean Build
  run: |
    ./gradlew clean
    ./gradlew build
```

#### Signing Failures
**Symptoms**: APK signing errors

**Verification**:
```yaml
- name: Verify Keystore
  run: |
    keytool -list -v -keystore release.keystore \
      -storepass ${{ secrets.KEYSTORE_PASSWORD }}
```

#### Configuration Issues
**Symptoms**: API regions not configured

**Debugging**:
```yaml
- name: Debug Configuration
  run: |
    echo "API_REGIONS: $API_REGIONS"
    echo "Build type: ${{ matrix.build-type }}"
```

## Performance Considerations

### Build Time Optimization
- Parallel job execution
- Dependency caching
- Incremental builds

### Resource Usage
- GitHub-hosted runners have memory and CPU limits
- Consider self-hosted runners for large projects
- Optimize Gradle configuration for CI

## Security Considerations

### Secret Management
- All secrets stored in GitHub Secrets
- Never commit sensitive data to repository
- Regular rotation of signing keys

### Build Security
- Dependency vulnerability scanning
- Code signing certificate validation
- Secure artifact storage

## Customization

### Adding Build Variants
```yaml
strategy:
  matrix:
    build-type: [debug, release]
    api-level: [26, 28, 29, 30, 31, 33]
```

### Custom Build Steps
```yaml
- name: Custom Step
  run: |
    # Custom build commands
    echo "Executing custom build step"
```

### Notification Integration
```yaml
- name: Notify Success
  if: success()
  uses: 8398a7/action-slack@v3
  with:
    status: success
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## Monitoring

### Build Metrics
- Build duration tracking
- Success/failure rates
- Artifact download statistics

### Alerting
- Build failure notifications
- Test failure alerts
- Performance regression detection

## Advanced Features

### Multi-Environment Deployment
- Separate configurations for development/staging/production
- Environment-specific API endpoints
- Conditional artifact publishing

### Automated Testing
- Integration tests with backend APIs
- UI automation testing
- Performance testing

### Release Automation
- Automatic version incrementing
- Changelog generation
- Release note creation

---

For detailed configuration options, see the [main documentation](README.md).
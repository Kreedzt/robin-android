package com.kreedzt.robin.data

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for VersionApiResponse model and extension functions
 * Focuses on null value handling for android.version and android.url fields
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class VersionApiResponseTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `PlatformVersionInfo_toVersionInfo_returns_null_when_version_is_null`() {
        // Given
        val platformInfo = PlatformVersionInfo(version = null, url = "https://example.com/download.apk")

        // When
        val result = platformInfo.toVersionInfo()

        // Then
        assertNull("toVersionInfo should return null when version is null", result)
    }

    @Test
    fun `PlatformVersionInfo_toVersionInfo_returns_null_when_version_is_empty`() {
        // Given
        val platformInfo = PlatformVersionInfo(version = "", url = "https://example.com/download.apk")

        // When
        val result = platformInfo.toVersionInfo()

        // Then
        assertNull("toVersionInfo should return null when version is empty string", result)
    }

    @Test
    fun `PlatformVersionInfo_toVersionInfo_returns_null_when_version_is_blank`() {
        // Given
        val platformInfo = PlatformVersionInfo(version = "   ", url = "https://example.com/download.apk")

        // When
        val result = platformInfo.toVersionInfo()

        // Then
        assertNull("toVersionInfo should return null when version is blank", result)
    }

    @Test
    fun `PlatformVersionInfo_toVersionInfo_returns_null_when_version_is_invalid_format`() {
        // Given - invalid semver format
        val platformInfo = PlatformVersionInfo(version = "invalid-version", url = "https://example.com/download.apk")

        // When
        val result = platformInfo.toVersionInfo()

        // Then
        assertNull("toVersionInfo should return null when version format is invalid", result)
    }

    @Test
    fun `PlatformVersionInfo_toVersionInfo_returns_VersionInfo_when_version_is_valid`() {
        // Given
        val platformInfo = PlatformVersionInfo(version = "1.2.3", url = "https://example.com/download.apk")

        // When
        val result = platformInfo.toVersionInfo()

        // Then
        assertNotNull("toVersionInfo should return VersionInfo for valid version", result)
        assertEquals(1, result?.major)
        assertEquals(2, result?.minor)
        assertEquals(3, result?.patch)
        assertEquals("https://example.com/download.apk", result?.downloadUrl)
    }

    @Test
    fun `PlatformVersionInfo_toVersionInfo_returns_VersionInfo_with_null_url`() {
        // Given - valid version but null url
        val platformInfo = PlatformVersionInfo(version = "2.0.0", url = null)

        // When
        val result = platformInfo.toVersionInfo()

        // Then
        assertNotNull("toVersionInfo should return VersionInfo even when url is null", result)
        assertEquals(2, result?.major)
        assertEquals(0, result?.minor)
        assertEquals(0, result?.patch)
        assertNull(result?.downloadUrl)
    }

    @Test
    fun `PlatformVersionInfo_toVersionInfo_handles_version_with_v_prefix`() {
        // Given
        val platformInfo = PlatformVersionInfo(version = "v1.5.0", url = "https://example.com/download.apk")

        // When
        val result = platformInfo.toVersionInfo()

        // Then
        assertNotNull("toVersionInfo should handle 'v' prefix", result)
        assertEquals(1, result?.major)
        assertEquals(5, result?.minor)
        assertEquals(0, result?.patch)
    }

    @Test
    fun `toUpdateInfo_returns_null_when_android_version_is_null`() {
        // Given
        val currentVersion = VersionInfo(major = 1, minor = 0, patch = 0, versionName = "1.0.0")
        val response = VersionApiResponse(
            android = PlatformVersionInfo(version = null, url = null),
            web = PlatformVersionInfo(version = "2.0.0", url = "https://example.com/web")
        )

        // When
        val result = response.toUpdateInfo(currentVersion)

        // Then
        assertNull("toUpdateInfo should return null when android version is null", result)
    }

    @Test
    fun `toUpdateInfo_returns_null_when_android_version_is_empty`() {
        // Given
        val currentVersion = VersionInfo(major = 1, minor = 0, patch = 0, versionName = "1.0.0")
        val response = VersionApiResponse(
            android = PlatformVersionInfo(version = "", url = "https://example.com/download.apk"),
            web = PlatformVersionInfo(version = "2.0.0", url = "https://example.com/web")
        )

        // When
        val result = response.toUpdateInfo(currentVersion)

        // Then
        assertNull("toUpdateInfo should return null when android version is empty", result)
    }

    @Test
    fun `toUpdateInfo_returns_null_when_latest_version_is_same_as_current`() {
        // Given
        val currentVersion = VersionInfo(major = 1, minor = 2, patch = 3, versionName = "1.2.3")
        val response = VersionApiResponse(
            android = PlatformVersionInfo(version = "1.2.3", url = "https://example.com/download.apk"),
            web = PlatformVersionInfo(version = "2.0.0", url = "https://example.com/web")
        )

        // When
        val result = response.toUpdateInfo(currentVersion)

        // Then
        assertNull("toUpdateInfo should return null when versions are the same", result)
    }

    @Test
    fun `toUpdateInfo_returns_null_when_latest_version_is_older_than_current`() {
        // Given
        val currentVersion = VersionInfo(major = 2, minor = 0, patch = 0, versionName = "2.0.0")
        val response = VersionApiResponse(
            android = PlatformVersionInfo(version = "1.9.9", url = "https://example.com/download.apk"),
            web = PlatformVersionInfo(version = "2.0.0", url = "https://example.com/web")
        )

        // When
        val result = response.toUpdateInfo(currentVersion)

        // Then
        assertNull("toUpdateInfo should return null when latest version is older", result)
    }

    @Test
    fun `toUpdateInfo_returns_UpdateInfo_when_newer_version_is_available`() {
        // Given
        val currentVersion = VersionInfo(major = 1, minor = 0, patch = 0, versionName = "1.0.0")
        val response = VersionApiResponse(
            android = PlatformVersionInfo(version = "1.2.3", url = "https://example.com/download.apk"),
            web = PlatformVersionInfo(version = "2.0.0", url = "https://example.com/web")
        )

        // When
        val result = response.toUpdateInfo(currentVersion)

        // Then
        assertNotNull("toUpdateInfo should return UpdateInfo when newer version exists", result)
        assertTrue("isUpdateAvailable should be true", result?.isUpdateAvailable == true)
        assertEquals("1.2.3", result?.latestVersion?.versionString)
        assertEquals("https://example.com/download.apk", result?.downloadUrl)
    }

    @Test
    fun `toUpdateInfo_returns_UpdateInfo_with_null_downloadUrl_when_url_is_null`() {
        // Given
        val currentVersion = VersionInfo(major = 1, minor = 0, patch = 0, versionName = "1.0.0")
        val response = VersionApiResponse(
            android = PlatformVersionInfo(version = "2.0.0", url = null),
            web = PlatformVersionInfo(version = "2.0.0", url = "https://example.com/web")
        )

        // When
        val result = response.toUpdateInfo(currentVersion)

        // Then
        assertNotNull("toUpdateInfo should return UpdateInfo even with null url", result)
        assertTrue("isUpdateAvailable should be true", result?.isUpdateAvailable == true)
        assertNull("downloadUrl should be null when url is null", result?.downloadUrl)
    }

    @Test
    fun `JSON_deserialization_handles_null_android_version`() {
        // Given
        val jsonWithNullVersion = """
            {
                "android": {
                    "version": null,
                    "url": null
                },
                "web": {
                    "version": "2.0.0",
                    "url": "https://example.com/web"
                }
            }
        """.trimIndent()

        // When
        val response = json.decodeFromString<VersionApiResponse>(jsonWithNullVersion)

        // Then
        assertNotNull("Response should deserialize successfully", response)
        assertNull("android.version should be null", response.android.version)
        assertNull("android.url should be null", response.android.url)
    }

    @Test
    fun `JSON_deserialization_handles_null_android_url_only`() {
        // Given
        val jsonWithNullUrl = """
            {
                "android": {
                    "version": "1.5.0",
                    "url": null
                },
                "web": {
                    "version": "2.0.0",
                    "url": "https://example.com/web"
                }
            }
        """.trimIndent()

        // When
        val response = json.decodeFromString<VersionApiResponse>(jsonWithNullUrl)

        // Then
        assertNotNull("Response should deserialize successfully", response)
        assertEquals("1.5.0", response.android.version)
        assertNull("android.url should be null", response.android.url)
    }

    @Test
    fun `JSON_deserialization_handles_empty_string_version`() {
        // Given
        val jsonWithEmptyVersion = """
            {
                "android": {
                    "version": "",
                    "url": "https://example.com/download.apk"
                },
                "web": {
                    "version": "2.0.0",
                    "url": "https://example.com/web"
                }
            }
        """.trimIndent()

        // When
        val response = json.decodeFromString<VersionApiResponse>(jsonWithEmptyVersion)

        // Then
        assertNotNull("Response should deserialize successfully", response)
        assertEquals("", response.android.version)
        assertEquals("https://example.com/download.apk", response.android.url)
    }

    @Test
    fun `JSON_deserialization_omitting_optional_fields`() {
        // Given - JSON without android fields (kotlinx.serialization uses default null)
        val jsonWithoutAndroidFields = """
            {
                "android": {},
                "web": {
                    "version": "2.0.0",
                    "url": "https://example.com/web"
                }
            }
        """.trimIndent()

        // When
        val response = json.decodeFromString<VersionApiResponse>(jsonWithoutAndroidFields)

        // Then
        assertNotNull("Response should deserialize successfully", response)
        assertNull("android.version should default to null", response.android.version)
        assertNull("android.url should default to null", response.android.url)
    }

    @Test
    fun `end_to_end_null_version_flow_returns_no_update`() {
        // Given - simulating backend response with null version
        val jsonWithNullVersion = """
            {
                "android": {
                    "version": null,
                    "url": null
                },
                "web": {
                    "version": "2.0.0",
                    "url": "https://example.com/web"
                }
            }
        """.trimIndent()

        val currentVersion = VersionInfo(major = 1, minor = 0, patch = 0, versionName = "1.0.0")

        // When
        val response = json.decodeFromString<VersionApiResponse>(jsonWithNullVersion)
        val updateInfo = response.toUpdateInfo(currentVersion)

        // Then
        assertNotNull("Response should deserialize", response)
        assertNull("updateInfo should be null when android version is null", updateInfo)
    }

    @Test
    fun `end_to_end_valid_version_flow_returns_update_info`() {
        // Given - simulating normal backend response
        val validJson = """
            {
                "android": {
                    "version": "2.0.0",
                    "url": "https://example.com/download.apk"
                },
                "web": {
                    "version": "3.0.0",
                    "url": "https://example.com/web"
                }
            }
        """.trimIndent()

        val currentVersion = VersionInfo(major = 1, minor = 5, patch = 0, versionName = "1.5.0")

        // When
        val response = json.decodeFromString<VersionApiResponse>(validJson)
        val updateInfo = response.toUpdateInfo(currentVersion)

        // Then
        assertNotNull("Response should deserialize", response)
        assertNotNull("updateInfo should not be null", updateInfo)
        assertTrue("isUpdateAvailable should be true", updateInfo?.isUpdateAvailable == true)
        assertEquals("2.0.0", updateInfo?.latestVersion?.versionString)
    }
}

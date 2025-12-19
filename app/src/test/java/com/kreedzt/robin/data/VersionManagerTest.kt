package com.kreedzt.robin.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

@RunWith(MockitoJUnitRunner::class)
class VersionManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPackageManager: PackageManager

    @Mock
    private lateinit var mockPackageInfo: PackageInfo

    private lateinit var versionManager: VersionManager

    @Before
    fun setup() {
        `when`(mockContext.packageManager).thenReturn(mockPackageManager)
        `when`(mockContext.packageName).thenReturn("com.kreedzt.robin")
        versionManager = VersionManager(mockContext)
    }

    @Test
    fun `getCurrentVersion returns correct version info`() {
        // Given
        val versionName = "1.2.3"
        val versionCode = 123
        mockPackageInfo.versionName = versionName

        `when`(mockPackageManager.getPackageInfo("com.kreedzt.robin", 0))
            .thenReturn(mockPackageInfo)

        // Mock versionCode based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mockPackageInfo.longVersionCode = versionCode.toLong()
        } else {
            @Suppress("DEPRECATION")
            mockPackageInfo.versionCode = versionCode
        }

        // When
        val result = versionManager.currentVersion

        // Then
        assertNotNull(result)
        assertEquals(1, result!!.major)
        assertEquals(2, result.minor)
        assertEquals(3, result.patch)
        assertEquals(versionCode, result.versionCode)
        assertEquals(versionName, result.versionName)
        assertEquals("1.2.3", result.versionString)
        assertEquals(versionName, result.displayName)
    }

    @Test
    fun `getCurrentVersion returns null when package info not found`() {
        // Given
        `when`(mockPackageManager.getPackageInfo("com.kreedzt.robin", 0))
            .thenThrow(PackageManager.NameNotFoundException())

        // When
        val result = versionManager.currentVersion

        // Then
        assertNull(result)
    }

    @Test
    fun `checkForUpdate returns null when no update available`() {
        // Given
        val currentVersionName = "1.2.3"
        mockPackageInfo.versionName = currentVersionName

        `when`(mockPackageManager.getPackageInfo("com.kreedzt.robin", 0))
            .thenReturn(mockPackageInfo)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mockPackageInfo.longVersionCode = 123
        } else {
            @Suppress("DEPRECATION")
            mockPackageInfo.versionCode = 123
        }

        val latestVersion = VersionInfo.fromString("1.2.3")!!

        // When
        val result = versionManager.checkForUpdate(latestVersion)

        // Then
        assertNull(result)
    }

    @Test
    fun `checkForUpdate returns update info when update available`() {
        // Given
        val currentVersionName = "1.2.3"
        mockPackageInfo.versionName = currentVersionName

        `when`(mockPackageManager.getPackageInfo("com.kreedzt.robin", 0))
            .thenReturn(mockPackageInfo)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mockPackageInfo.longVersionCode = 123
        } else {
            @Suppress("DEPRECATION")
            mockPackageInfo.versionCode = 123
        }

        val latestVersion = VersionInfo.fromString("1.2.4")!!

        // When
        val result = versionManager.checkForUpdate(latestVersion)

        // Then
        assertNotNull(result)
        assertTrue(result!!.isUpdateAvailable)
        assertFalse(result.isMandatory)
        assertEquals("1.2.3", result.currentVersion.displayName)
        assertEquals("1.2.4", result.latestVersion.displayName)
        assertEquals("Update available: 1.2.3 → 1.2.4", result.updateMessage)
    }

    @Test
    fun `checkForUpdate with version string works correctly`() {
        // Given
        val currentVersionName = "1.0.0"
        mockPackageInfo.versionName = currentVersionName

        `when`(mockPackageManager.getPackageInfo("com.kreedzt.robin", 0))
            .thenReturn(mockPackageInfo)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mockPackageInfo.longVersionCode = 100
        } else {
            @Suppress("DEPRECATION")
            mockPackageInfo.versionCode = 100
        }

        // When
        val result = versionManager.checkForUpdate("1.1.0", "https://example.com/download")

        // Then
        assertNotNull(result)
        assertTrue(result!!.isUpdateAvailable)
        assertEquals("https://example.com/download", result.downloadUrl)
    }

    @Test
    fun `parseVersion returns null for invalid version string`() {
        val result = versionManager.parseVersion("invalid.version")
        assertNull(result)
    }

    @Test
    fun `parseVersion returns correct version for valid semver`() {
        val result = versionManager.parseVersion("2.5.1")
        assertNotNull(result)
        assertEquals(2, result!!.major)
        assertEquals(5, result.minor)
        assertEquals(1, result.patch)
    }

    @Test
    fun `isValidVersion returns correct validation results`() {
        assertTrue(versionManager.isValidVersion("1.0.0"))
        assertTrue(versionManager.isValidVersion("10.20.30"))
        assertFalse(versionManager.isValidVersion("1.0"))
        assertFalse(versionManager.isValidVersion("1.0.0.0"))
        assertFalse(versionManager.isValidVersion("not.a.version"))
    }
}

@RunWith(MockitoJUnitRunner::class)
class VersionInfoTest {

    @Test
    fun `fromString creates correct VersionInfo`() {
        val result = VersionInfo.fromString("1.2.3", 123)

        assertNotNull(result)
        assertEquals(1, result!!.major)
        assertEquals(2, result.minor)
        assertEquals(3, result.patch)
        assertEquals(123, result.versionCode)
        assertEquals("1.2.3", result.versionName)
    }

    @Test
    fun `fromString returns null for invalid format`() {
        assertNull(VersionInfo.fromString("1.2"))
        assertNull(VersionInfo.fromString("1.2.3.4"))
        assertNull(VersionInfo.fromString("not.a.version"))
        assertNull(VersionInfo.fromString("a.b.c"))
    }

    @Test
    fun `version comparison works correctly`() {
        val v100 = VersionInfo.fromString("1.0.0")!!
        val v110 = VersionInfo.fromString("1.1.0")!!
        val v200 = VersionInfo.fromString("2.0.0")!!
        val v101 = VersionInfo.fromString("1.0.1")!!

        assertTrue(v110.isNewerThan(v100))
        assertTrue(v200.isNewerThan(v110))
        assertTrue(v101.isNewerThan(v100))

        assertTrue(v100.isOlderThan(v110))
        assertTrue(v100.isOlderThan(v200))
        assertTrue(v100.isOlderThan(v101))

        assertTrue(v100.isSameVersionAs(VersionInfo.fromString("1.0.0")!!))
        assertFalse(v100.isSameVersionAs(v110))
    }

    @Test
    fun `compareTo returns correct comparison values`() {
        val v100 = VersionInfo.fromString("1.0.0")!!
        val v110 = VersionInfo.fromString("1.1.0")!!
        val v200 = VersionInfo.fromString("2.0.0")!!

        assertEquals(-1, v100.compareTo(v110))
        assertEquals(-1, v100.compareTo(v200))
        assertEquals(1, v110.compareTo(v100))
        assertEquals(0, v100.compareTo(v100))
    }

    @Test
    fun `displayName uses versionName when available`() {
        val versionWithName = VersionInfo(1, 2, 3, 123, "1.2.3-beta")
        assertEquals("1.2.3-beta", versionWithName.displayName)

        val versionWithoutName = VersionInfo(1, 2, 3, 123, "")
        assertEquals("1.2.3", versionWithoutName.displayName)
    }
}
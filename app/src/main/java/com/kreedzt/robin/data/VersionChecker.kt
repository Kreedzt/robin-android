package com.kreedzt.robin.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class for checking and managing app version updates
 * This demonstrates how to use the VersionManager for version checking
 */
class VersionChecker private constructor(private val context: Context) {

    private val versionManager = VersionManager.getInstance(context)

    /**
     * Gets the current app version
     */
    suspend fun getCurrentVersion(): VersionInfo? = withContext(Dispatchers.IO) {
        versionManager.currentVersion
    }

    /**
     * Checks for updates from a remote version string
     * @param remoteVersionString Version string from API in semver format (x.y.z)
     * @param downloadUrl Optional download URL for the update
     * @return UpdateInfo if an update is available, null otherwise
     */
    suspend fun checkForUpdate(
        remoteVersionString: String,
        downloadUrl: String? = null
    ): UpdateInfo? = withContext(Dispatchers.IO) {
        versionManager.checkForUpdate(remoteVersionString, downloadUrl)
    }

    /**
     * Checks for updates from a remote VersionInfo object
     * @param latestVersion Latest version info from API
     * @return UpdateInfo if an update is available, null otherwise
     */
    suspend fun checkForUpdate(latestVersion: VersionInfo): UpdateInfo? =
        withContext(Dispatchers.IO) {
            versionManager.checkForUpdate(latestVersion)
        }

    /**
     * Validates if a version string follows semver format
     */
    fun isValidVersion(versionString: String): Boolean {
        return versionManager.isValidVersion(versionString)
    }

    /**
     * Parses a version string into VersionInfo
     */
    fun parseVersion(versionString: String): VersionInfo? {
        return versionManager.parseVersion(versionString)
    }

    companion object {
        @Volatile
        private var INSTANCE: VersionChecker? = null

        fun getInstance(context: Context): VersionChecker {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VersionChecker(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/**
 * Example usage:
 *
 * ```kotlin
 * // In a ViewModel or Repository
 * val versionChecker = VersionChecker.getInstance(context)
 *
 * // Get current version
 * val currentVersion = versionChecker.getCurrentVersion()
 *
 * // Check for update from API response
 * val latestVersionFromApi = "1.1.0"
 * val updateInfo = versionChecker.checkForUpdate(
 *     remoteVersionString = latestVersionFromApi,
 *     downloadUrl = "https://example.com/download"
 * )
 *
 * // Handle update
 * updateInfo?.let {
 *     if (it.isUpdateAvailable) {
 *         // Show update dialog
 *         showUpdateDialog(
 *             currentVersion = it.currentVersion.displayName,
 *             latestVersion = it.latestVersion.displayName,
 *             downloadUrl = it.downloadUrl,
 *             isMandatory = it.isMandatory
 *         )
 *     }
 * }
 * ```
 */
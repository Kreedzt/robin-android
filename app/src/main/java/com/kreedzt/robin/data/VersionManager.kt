package com.kreedzt.robin.data

import android.content.Context
import android.content.pm.PackageManager

/**
 * Manager for handling application version information
 * Provides access to current app version and version comparison utilities
 */
class VersionManager internal constructor(private val context: Context) {

    private val packageManager = context.packageManager
    private val packageName = context.packageName

    /**
     * Gets the current version information of the app
     */
    val currentVersion: VersionInfo?
        get() {
            return try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val versionName = packageInfo.versionName ?: return null
                val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                }

                VersionInfo.fromPackageInfo(versionName, versionCode)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Checks if there's a newer version available
     * @param latestVersionInfo The latest version info from remote source
     * @return UpdateInfo with update details if an update is available
     */
    fun checkForUpdate(latestVersionInfo: VersionInfo): UpdateInfo? {
        val current = currentVersion ?: return null

        return when {
            latestVersionInfo.isNewerThan(current) -> UpdateInfo(
                currentVersion = current,
                latestVersion = latestVersionInfo,
                isUpdateAvailable = true,
                isMandatory = false // You can add logic for mandatory updates
            )
            else -> null
        }
    }

    /**
     * Checks if there's a newer version available with remote version string
     * @param latestVersionString The latest version string in semver format (x.y.z)
     * @param downloadUrl Optional download URL for the update
     * @return UpdateInfo with update details if an update is available
     */
    fun checkForUpdate(latestVersionString: String, downloadUrl: String? = null): UpdateInfo? {
        val latestVersion = VersionInfo.fromString(latestVersionString) ?: return null
        val updateInfo = checkForUpdate(latestVersion)
        return updateInfo?.copy(downloadUrl = downloadUrl)
    }

    /**
     * Parses a version string and returns VersionInfo
     * @param versionString Version string in semver format (x.y.z)
     * @return VersionInfo or null if parsing fails
     */
    fun parseVersion(versionString: String): VersionInfo? {
        return VersionInfo.fromString(versionString)
    }

    /**
     * Validates if a version string follows semver format (x.y.z)
     */
    fun isValidVersion(versionString: String): Boolean {
        return VersionInfo.fromString(versionString) != null
    }

    companion object {
        @Volatile
        private var INSTANCE: VersionManager? = null

        /**
         * Gets the singleton instance of VersionManager
         */
        fun getInstance(context: Context): VersionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VersionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/**
 * Data class containing update information
 */
data class UpdateInfo(
    val currentVersion: VersionInfo,
    val latestVersion: VersionInfo,
    val isUpdateAvailable: Boolean,
    val isMandatory: Boolean = false,
    val downloadUrl: String? = null,
    val changelog: String? = null,
    val releaseNotes: String? = null
) {
    /**
     * Gets a user-friendly update message
     */
    val updateMessage: String
        get() = when {
            isMandatory -> "A mandatory update is required (${currentVersion.displayName} → ${latestVersion.displayName})"
            else -> "Update available: ${currentVersion.displayName} → ${latestVersion.displayName}"
        }
}
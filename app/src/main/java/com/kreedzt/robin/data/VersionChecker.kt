package com.kreedzt.robin.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class for checking and managing app version updates
 * This demonstrates how to use the VersionManager for version checking
 */
class VersionChecker private constructor(private val context: Context) {

    private val versionManager = VersionManager.getInstance(context)
    private val versionApiService = VersionApiService.getInstance()

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

    /**
     * Checks for update from remote API
     * @param apiUrl The base URL of the API (e.g., "https://robin.kreedzt.com")
     * @return UpdateInfo if an update is available, null otherwise
     */
    suspend fun checkForUpdateFromApi(apiUrl: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            // Get current version first
            val currentVersion = versionManager.currentVersion
                ?: return@withContext null

            // Fetch version info from API
            val apiResult = versionApiService.fetchVersionInfo(apiUrl)
            val versionResponse = apiResult.getOrNull()
                ?: return@withContext null

            // Convert to UpdateInfo
            versionResponse.toUpdateInfo(currentVersion)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks for update from remote API with proper error handling
     * @param apiUrl The base URL of the API
     * @return Result containing UpdateInfo or error
     */
    suspend fun checkForUpdateFromApiSafely(apiUrl: String): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            // Get current version first
            val currentVersion = versionManager.currentVersion
            if (currentVersion != null) {
                Log.d("VersionChecker", "Current app version: ${currentVersion.displayName} (${currentVersion.versionString})")
            } else {
                Log.e("VersionChecker", "Could not get current version")
                return@withContext Result.failure(Exception("Could not get current version"))
            }

            // Fetch version info from API
            Log.d("VersionChecker", "Fetching version info from API...")
            val apiResult = versionApiService.fetchVersionInfo(apiUrl)
            val versionResponse = apiResult.getOrElse { error ->
                Log.e("VersionChecker", "Failed to fetch version info from API", error)
                return@withContext Result.failure(error)
            }

            if (versionResponse != null) {
                val androidVersion = versionResponse.android.version ?: "null"
                Log.d("VersionChecker", "API response received - Android version: $androidVersion")
            } else {
                Log.w("VersionChecker", "API response is null")
            }

            // Convert to UpdateInfo
            val updateInfo = versionResponse?.toUpdateInfo(currentVersion)
            if (updateInfo != null) {
                Log.i("VersionChecker", "Update available! Current: ${currentVersion.displayName}, Latest: ${updateInfo.latestVersion.displayName}")
                Log.d("VersionChecker", "Download URL: ${updateInfo.downloadUrl}")
            } else {
                Log.i("VersionChecker", "No update available. Current version: ${currentVersion.displayName}")
            }

            Result.success(updateInfo)
        } catch (e: Exception) {
            Log.e("VersionChecker", "Error checking for update", e)
            Result.failure(e)
        }
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
 * // Handle update in Compose UI
 * updateInfo?.let { update ->
 *     if (update.isUpdateAvailable) {
 *         // Show UpdateDialog
 *         UpdateDialog(
 *             updateInfo = update,
 *             onDownload = {
 *                 // Open download URL
 *                 val intent = Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl))
 *                 context.startActivity(intent)
 *             },
 *             onDismiss = { /* Handle dismiss */ },
 *             onLater = { /* Handle later */ }
 *         )
 *     }
 * }
 *
 * // Or use custom message with string resources
 * val message = if (update.isMandatory) {
 *     context.getString(R.string.mandatory_update_dialog_message, update.latestVersion.displayName)
 * } else {
 *     context.getString(
 *         R.string.update_dialog_message,
 *         update.latestVersion.displayName,
 *         update.currentVersion.displayName
 *     )
 * }
 * ```
 */
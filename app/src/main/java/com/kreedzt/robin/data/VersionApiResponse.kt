package com.kreedzt.robin.data

import android.util.Log
import kotlinx.serialization.Serializable

/**
 * Response model for the /api/version endpoint
 */
@Serializable
data class VersionApiResponse(
    val android: PlatformVersionInfo,
    val web: PlatformVersionInfo
)

/**
 * Platform-specific version information
 */
@Serializable
data class PlatformVersionInfo(
    val version: String? = null,
    val url: String? = null
)

/**
 * Extension function to convert PlatformVersionInfo to VersionInfo
 * Returns null if version is null or empty, or if version string is invalid
 */
fun PlatformVersionInfo.toVersionInfo(): VersionInfo? {
    val versionString = version
    if (versionString.isNullOrBlank()) {
        return null
    }
    return VersionInfo.fromString(versionString, 0)?.copy(
        downloadUrl = url
    )
}

/**
 * Extension function to create UpdateInfo from API response
 * Returns null if android version is not available or no update is needed
 */
fun VersionApiResponse.toUpdateInfo(currentVersion: VersionInfo): UpdateInfo? {
    val latestVersion = android.toVersionInfo()
    if (latestVersion == null) {
        Log.w("VersionApiResponse", "Android version is null, empty, or invalid. Cannot check for update.")
        return null
    }

    Log.d("VersionApiResponse", "Comparing versions - Current: ${currentVersion.versionString} (${currentVersion.versionName}), Latest: ${latestVersion.versionString} (${latestVersion.versionName})")
    Log.d("VersionApiResponse", "Version comparison - IsLatestNewer: ${latestVersion.isNewerThan(currentVersion)}")
    return if (latestVersion.isNewerThan(currentVersion)) {
        Log.i("VersionApiResponse", "Update needed! Current: ${currentVersion.displayName} → Latest: ${latestVersion.displayName}")
        UpdateInfo(
            currentVersion = currentVersion,
            latestVersion = latestVersion,
            isUpdateAvailable = true,
            isMandatory = false, // You can add logic for mandatory updates
            downloadUrl = latestVersion.downloadUrl
        )
    } else {
        Log.d("VersionApiResponse", "No update needed. Current version is same or newer.")
        null
    }
}
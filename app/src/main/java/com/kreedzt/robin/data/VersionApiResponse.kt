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
    val version: String,
    val url: String
)

/**
 * Extension function to convert PlatformVersionInfo to VersionInfo
 */
fun PlatformVersionInfo.toVersionInfo(): VersionInfo? {
    return VersionInfo.fromString(version, 0)?.copy(
        downloadUrl = url
    )
}

/**
 * Extension function to create UpdateInfo from API response
 */
fun VersionApiResponse.toUpdateInfo(currentVersion: VersionInfo): UpdateInfo? {
    return android.toVersionInfo()?.let { latestVersion ->
        Log.d("VersionApiResponse", "Comparing versions - Current: ${currentVersion.versionString} (${currentVersion.versionName}), Latest: ${latestVersion.versionString} (${latestVersion.versionName})")
        Log.d("VersionApiResponse", "Version comparison - IsLatestNewer: ${latestVersion.isNewerThan(currentVersion)}")
        if (latestVersion.isNewerThan(currentVersion)) {
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
}
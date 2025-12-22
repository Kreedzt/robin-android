package com.kreedzt.robin.data

/**
 * Data class representing application version information
 */
data class VersionInfo(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val versionCode: Int = 0,
    val versionName: String = "",
    val downloadUrl: String? = null
) {
    /**
     * Returns the version string in semver format (x.y.z)
     */
    val versionString: String
        get() = "$major.$minor.$patch"

    /**
     * Returns the version name if available, otherwise uses semver format
     */
    val displayName: String
        get() = versionName.ifEmpty { versionString }

    /**
     * Compares this version with another version
     * @return -1 if this version is older, 0 if equal, 1 if newer
     */
    fun compareTo(other: VersionInfo): Int {
        return when {
            major != other.major -> major.compareTo(other.major)
            minor != other.minor -> minor.compareTo(other.minor)
            patch != other.patch -> patch.compareTo(other.patch)
            else -> 0
        }
    }

    /**
     * Checks if this version is newer than the given version
     */
    fun isNewerThan(other: VersionInfo): Boolean = compareTo(other) > 0

    /**
     * Checks if this version is older than the given version
     */
    fun isOlderThan(other: VersionInfo): Boolean = compareTo(other) < 0

    /**
     * Checks if this version is equal to the given version
     */
    fun isSameVersionAs(other: VersionInfo): Boolean = compareTo(other) == 0

    companion object {
        /**
         * Creates a VersionInfo from a semver string (x.y.z)
         * Accepts versions with or without 'v' prefix (e.g., "1.2.3" or "v1.2.3")
         */
        fun fromString(versionString: String, versionCode: Int = 0): VersionInfo? {
            // Remove 'v' prefix if present
            val cleanVersionString = if (versionString.startsWith("v", ignoreCase = true)) {
                versionString.substring(1)
            } else {
                versionString
            }

            val parts = cleanVersionString.split(".")
            if (parts.size != 3) return null

            return try {
                VersionInfo(
                    major = parts[0].toInt(),
                    minor = parts[1].toInt(),
                    patch = parts[2].toInt(),
                    versionCode = versionCode,
                    versionName = versionString
                )
            } catch (e: NumberFormatException) {
                null
            }
        }

        /**
         * Creates a VersionInfo from version name and version code (from PackageManager)
         */
        fun fromPackageInfo(versionName: String, versionCode: Int): VersionInfo? {
            return fromString(versionName, versionCode)
        }
    }
}
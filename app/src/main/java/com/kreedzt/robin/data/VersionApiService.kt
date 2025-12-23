package com.kreedzt.robin.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import java.io.IOException

/**
 * Service for checking app versions from remote API
 */
class VersionApiService private constructor(
    private val client: OkHttpClient,
    private val json: Json
) {

    /**
     * Fetches version information from the given API endpoint
     * @param baseUrl The base URL of the API (e.g., "https://robin.kreedzt.com")
     * @return VersionApiResponse if successful, null otherwise
     */
    suspend fun fetchVersionInfo(baseUrl: String): Result<VersionApiResponse?> = withContext(Dispatchers.IO) {
        val url = "${baseUrl.trimEnd('/')}/api/version"
        Log.d(TAG, "Fetching version info from: $url")

        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "HTTP response code: ${response.code}")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        Log.d(TAG, "Response body: $responseBody")
                        try {
                            val versionResponse = json.decodeFromString<VersionApiResponse>(responseBody)
                            val androidVersion = versionResponse.android.version ?: "null"
                            val androidUrl = versionResponse.android.url ?: "null"
                            Log.d(TAG, "Parsed version response - Android: $androidVersion, URL: $androidUrl")
                            Result.success(versionResponse)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse JSON response", e)
                            Result.failure(e)
                        }
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(IOException("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code} - ${response.message}")
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "VersionApiService"
        private var INSTANCE: VersionApiService? = null

        /**
         * Gets the singleton instance of VersionApiService
         */
        fun getInstance(
            client: OkHttpClient = OkHttpClient(),
            json: Json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        ): VersionApiService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VersionApiService(client, json).also { INSTANCE = it }
            }
        }
    }
}
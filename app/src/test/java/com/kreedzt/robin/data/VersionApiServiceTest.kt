package com.kreedzt.robin.data

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class VersionApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var versionApiService: VersionApiService
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        val client = OkHttpClient.Builder().build()
        versionApiService = VersionApiService.getInstance(client, json)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `fetchVersionInfo_returns_success_result_for_valid_response`() = runTest {
        // Given
        val validResponse = """
            {
                "android": {
                    "version": "1.2.3",
                    "url": "https://example.com/download.apk"
                },
                "web": {
                    "version": "2.0.0",
                    "url": "https://example.com/web"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(validResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200)
        )

        // When
        val result = versionApiService.fetchVersionInfo(mockWebServer.url("/").toString())

        // Then
        assertTrue(result.isSuccess)
        val versionResponse = result.getOrNull()
        assertNotNull(versionResponse)
        assertEquals("1.2.3", versionResponse?.android?.version)
        assertEquals("https://example.com/download.apk", versionResponse?.android?.url)
        assertEquals("2.0.0", versionResponse?.web?.version)
        assertEquals("https://example.com/web", versionResponse?.web?.url)
    }

    @Test
    fun `fetchVersionInfo_returns_failure_result_for_HTTP_error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("Not Found")
        )

        // When
        val result = versionApiService.fetchVersionInfo(mockWebServer.url("/").toString())

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IOException)
        assertTrue(exception?.message?.contains("404") == true)
    }

    @Test
    fun `fetchVersionInfo_returns_failure_result_for_invalid_JSON`() = runTest {
        // Given
        val invalidJsonResponse = """
            {
                "android": {
                    "version": "1.2.3"
                    // Missing URL field - invalid JSON
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(invalidJsonResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200)
        )

        // When
        val result = versionApiService.fetchVersionInfo(mockWebServer.url("/").toString())

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is Exception)
    }

    @Test
    fun `fetchVersionInfo_returns_failure_result_for_empty_response`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setBody("")
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200)
        )

        // When
        val result = versionApiService.fetchVersionInfo(mockWebServer.url("/").toString())

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        // Just verify that it fails - the specific exception type may vary
        // The important thing is that empty response causes a failure
    }

    @Test
    fun `fetchVersionInfo_handles_network_errors`() = runTest {
        // Given
        mockWebServer.shutdown()

        // When
        val result = versionApiService.fetchVersionInfo("http://invalid-server-that-does-not-exist.com")

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is Exception)
    }

    @Test
    fun `fetchVersionInfo_correctly_constructs_URL_with_trailing_slash`() = runTest {
        // Given
        val baseUrl = "${mockWebServer.url("/").toString()}/" // with trailing slash

        val validResponse = """
            {
                "android": {
                    "version": "1.0.0",
                    "url": "https://example.com/download"
                },
                "web": {
                    "version": "1.0.0",
                    "url": "https://example.com/web"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(validResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200)
        )

        // When
        val result = versionApiService.fetchVersionInfo(baseUrl)

        // Then
        assertTrue(result.isSuccess)
        val request = mockWebServer.takeRequest()
        assertEquals("/api/version", request.path)
    }

    @Test
    fun `fetchVersionInfo_correctly_constructs_URL_without_trailing_slash`() = runTest {
        // Given
        val baseUrl = mockWebServer.url("/").toString() // without trailing slash

        val validResponse = """
            {
                "android": {
                    "version": "1.0.0",
                    "url": "https://example.com/download"
                },
                "web": {
                    "version": "1.0.0",
                    "url": "https://example.com/web"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(validResponse)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200)
        )

        // When
        val result = versionApiService.fetchVersionInfo(baseUrl)

        // Then
        assertTrue(result.isSuccess)
        val request = mockWebServer.takeRequest()
        assertEquals("/api/version", request.path)
    }

    @Test
    fun `getInstance_returns_singleton_instance`() {
        // Given
        val instance1 = VersionApiService.getInstance()
        val instance2 = VersionApiService.getInstance()

        // Then
        assertSame(instance1, instance2)
    }

    @Test
    fun `getInstance_with_custom_parameters_returns_same_instance_if_already_created`() {
        // Given
        val customClient = OkHttpClient.Builder().build()
        val customJson = Json { ignoreUnknownKeys = true }

        val instance1 = VersionApiService.getInstance(customClient, customJson)
        val instance2 = VersionApiService.getInstance(customClient, customJson)

        // Then - should return the same instance since singleton is already created
        assertSame(instance1, instance2)
    }
}
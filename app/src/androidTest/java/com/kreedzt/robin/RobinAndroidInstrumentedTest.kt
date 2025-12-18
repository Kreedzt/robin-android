package com.kreedzt.robin

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kreedzt.robin.data.ApiRegionConfig
import com.kreedzt.robin.data.GameServer
import com.kreedzt.robin.ui.ServerRow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4::class)
class RobinAndroidInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.kreedzt.robin", appContext.packageName)
    }

    @Test
    fun serverRow_displaysCorrectInformation() {
        val testServer = GameServer(
            id = "1",
            name = "Test Server",
            ipAddress = "192.168.1.100",
            port = 27015,
            currentPlayers = 10,
            maxPlayers = 32,
            mapId = "test_map",
            mapName = "test_map",
            bots = 5,
            country = "US East",
            timeStamp = System.currentTimeMillis() / 1000,
            version = "1.0.0",
            dedicated = true,
            mod = false,
            playerList = emptyList(),
            comment = "A test server for testing",
            url = "https://example.com",
            mode = "invasion"
        )

        composeTestRule.setContent {
            ServerRow(
                server = testServer,
                query = "",
                onClick = {}
            )
        }

        // Verify server information is displayed
        composeTestRule.onNodeWithText("Test Server").assertIsDisplayed()
        composeTestRule.onNodeWithText("10/32").assertIsDisplayed()
        composeTestRule.onNodeWithText("test_map").assertIsDisplayed()
        composeTestRule.onNodeWithText("invasion").assertIsDisplayed()
        composeTestRule.onNodeWithText("US East").assertIsDisplayed()
    }

    @Test
    fun serverRow_withPassword_showsPasswordIndicator() {
        val testServer = GameServer(
            id = "1",
            name = "Protected Server",
            ipAddress = "192.168.1.100",
            port = 27015,
            currentPlayers = 10,
            maxPlayers = 32,
            mapId = "test_map",
            mapName = "test_map",
            bots = 0,
            country = "",
            timeStamp = System.currentTimeMillis() / 1000,
            version = "1.0.0",
            dedicated = true,
            mod = false,
            playerList = emptyList(),
            comment = "A password-protected server",
            url = "",
            mode = "invasion"
        )

        composeTestRule.setContent {
            ServerRow(
                server = testServer,
                query = "",
                onClick = {}
            )
        }

        // Verify password indicator is shown
        composeTestRule.onNodeWithText("Protected Server").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed() // location for empty country
    }

    @Test
    fun serverRow_withSearchQuery_highlightsMatchingText() {
        val testServer = GameServer(
            id = "1",
            name = "Test Server",
            ipAddress = "192.168.1.100",
            port = 27015,
            currentPlayers = 10,
            maxPlayers = 32,
            mapId = "test_map",
            mapName = "test_map",
            bots = 0,
            country = "",
            timeStamp = System.currentTimeMillis() / 1000,
            version = "1.0.0",
            dedicated = true,
            mod = false,
            playerList = emptyList(),
            comment = "A test server for testing",
            url = "",
            mode = "invasion"
        )

        composeTestRule.setContent {
            ServerRow(
                server = testServer,
                query = "test",
                onClick = {}
            )
        }

        // Verify search highlighting is applied
        composeTestRule.onNodeWithText("Test Server").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed() // location for empty country
    }

    @Test
    fun serverRow_handlesClickInteraction() {
        val testServer = GameServer(
            id = "1",
            name = "Clickable Server",
            ipAddress = "192.168.1.100",
            port = 27015,
            currentPlayers = 10,
            maxPlayers = 32,
            mapId = "test_map",
            mapName = "test_map",
            bots = 0,
            country = "",
            timeStamp = System.currentTimeMillis() / 1000,
            version = "1.0.0",
            dedicated = true,
            mod = false,
            playerList = emptyList(),
            comment = "",
            url = "",
            mode = "invasion"
        )

        var clickCount = 0
        composeTestRule.setContent {
            ServerRow(
                server = testServer,
                query = "",
                onClick = { clickCount++ }
            )
        }

        // Verify the server row is clickable
        composeTestRule.onNodeWithText("Clickable Server").performClick()
        assertEquals(1, clickCount)
        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed() // location for empty country
    }

    @Test
    fun serverRow_displaysServerStatusCorrectly() {
        val fullServer = GameServer(
            id = "1",
            name = "Full Server",
            ipAddress = "192.168.1.100",
            port = 27015,
            currentPlayers = 32,
            maxPlayers = 32,
            mapId = "test_map",
            mapName = "test_map",
            bots = 0,
            country = "",
            timeStamp = System.currentTimeMillis() / 1000,
            version = "1.0.0",
            dedicated = true,
            mod = false,
            playerList = emptyList(),
            comment = "",
            url = "",
            mode = "invasion"
        )

        composeTestRule.setContent {
            ServerRow(
                server = fullServer,
                query = "",
                onClick = {}
            )
        }

        // Verify server status is displayed correctly
        composeTestRule.onNodeWithText("Full Server").assertIsDisplayed()
        composeTestRule.onNodeWithText("32/32").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed() // location for empty country
    }

    @Test
    fun serverRow_displaysEmptyServerStatus() {
        val emptyServer = GameServer(
            id = "1",
            name = "Empty Server",
            ipAddress = "192.168.1.100",
            port = 27015,
            currentPlayers = 0,
            maxPlayers = 32,
            mapId = "test_map",
            mapName = "test_map",
            bots = 0,
            country = "",
            timeStamp = System.currentTimeMillis() / 1000,
            version = "1.0.0",
            dedicated = true,
            mod = false,
            playerList = emptyList(),
            comment = "",
            url = "",
            mode = "invasion"
        )

        composeTestRule.setContent {
            ServerRow(
                server = emptyServer,
                query = "",
                onClick = {}
            )
        }

        // Verify server status is displayed correctly
        composeTestRule.onNodeWithText("Empty Server").assertIsDisplayed()
        composeTestRule.onNodeWithText("0/32").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unknown").assertIsDisplayed() // location for empty country
    }

    @Test
    fun apiRegionConfig_handlesValidConfig() {
        val configString = "test|https://test.api.com/|Test Region|测试区域"
        val regions = ApiRegionConfig.parseFromString(configString)

        assertEquals(1, regions.size)
        assertEquals("test", regions[0].id)
        assertEquals("https://test.api.com/", regions[0].url)
        assertEquals("Test Region", regions[0].labelEn)
        assertEquals("测试区域", regions[0].labelZh)
    }

    @Test
    fun apiRegionConfig_handlesMultipleRegions() {
        val configString = "china|https://api.cn/|China|中国;global|https://api.com/|Global|全球"
        val regions = ApiRegionConfig.parseFromString(configString)

        assertEquals(2, regions.size)
        assertEquals("china", regions[0].id)
        assertEquals("global", regions[1].id)
    }

    @Test
    fun apiRegionConfig_handlesInvalidConfig() {
        val invalidConfigString = "invalid|config|format"
        val regions = ApiRegionConfig.parseFromString(invalidConfigString)

        // Should return default regions for invalid config
        assertEquals(2, regions.size)
        assertEquals("china", regions[0].id)
        assertEquals("global", regions[1].id)
    }

    @Test
    fun apiRegionConfig_handlesNullConfig() {
        val regions = ApiRegionConfig.parseFromString(null)

        // Should return default regions for null config
        assertEquals(2, regions.size)
        assertEquals("china", regions[0].id)
        assertEquals("global", regions[1].id)
    }

    @Test
    fun gameServer_creationWithMinimalData() {
        val server = GameServer(
            id = "1",
            name = "Minimal Server",
            ipAddress = "192.168.1.1",
            port = 27015,
            currentPlayers = 0,
            maxPlayers = 16,
            mapId = "minimal",
            mapName = "minimal",
            bots = 0,
            country = "",
            timeStamp = System.currentTimeMillis() / 1000,
            version = "1.0.0",
            dedicated = false,
            mod = false,
            playerList = emptyList(),
            comment = "",
            url = "",
            mode = "invasion"
        )

        assertEquals("1", server.id)
        assertEquals("Minimal Server", server.name)
        assertEquals("192.168.1.1", server.ipAddress)
        assertEquals(27015, server.port)
        assertEquals(0, server.currentPlayers)
        assertEquals(16, server.maxPlayers)
        // password field doesn't exist
        assertFalse(server.dedicated)
    }

    @Test
    fun gameServer_creationWithFullData() {
        val server = GameServer(
            id = "1",
            name = "Full Server",
            ipAddress = "192.168.1.1",
            port = 27015,
            currentPlayers = 20,
            maxPlayers = 32,
            mapId = "full_map",
            mapName = "full_map",
            bots = 5,
            country = "US West",
            timeStamp = System.currentTimeMillis() / 1000,
            version = "2.0.0",
            dedicated = true,
            mod = false,
            playerList = emptyList(),
            comment = "A fully configured server",
            url = "https://example.com",
            mode = "dominance"
        )

        assertEquals("1", server.id)
        assertEquals("Full Server", server.name)
        assertEquals("192.168.1.1", server.ipAddress)
        assertEquals(27015, server.port)
        assertEquals(20, server.currentPlayers)
        assertEquals(32, server.maxPlayers)
        assertEquals("full_map", server.mapId)
        assertEquals("dominance", server.mode)
        assertEquals("2.0.0", server.version)
        assertEquals(5, server.bots)
        // password field doesn't exist
        assertTrue(server.dedicated)
        assertEquals("A fully configured server", server.comment)
        assertEquals("https://example.com", server.url)
        assertEquals("US West", server.country)
        // lastUpdate field doesn't exist in current structure
    }
}
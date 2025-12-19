package com.kreedzt.robin.data

import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.test.runTest

class ServerRepositoryTest {

    @Mock
    private lateinit var settingsManager: SettingsManager

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    @org.junit.Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Mock SharedPreferences
        `when`(context.getSharedPreferences("maps_cache", Context.MODE_PRIVATE))
            .thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.putLong(anyString(), anyLong())).thenReturn(editor)
        `when`(sharedPreferences.getLong(anyString(), anyLong())).thenReturn(0L)
        `when`(sharedPreferences.getString(anyString(), any())).thenReturn(null)
    }

    @Test
    fun searchServersInLocalData_whenQueryIsPlayerName_returnsOnlyServersWithThatPlayer() = runTest {
        // Given
        `when`(settingsManager.apiBaseUrl).thenReturn("https://test.api.com/")
        val repository = ServerRepository(settingsManager, context)

        val servers = listOf(
            GameServer(
                id = "1",
                name = "Gaming Server",
                ipAddress = "192.168.1.1",
                port = 27015,
                currentPlayers = 2,
                maxPlayers = 32,
                mapId = "map1",
                mapName = "Map 1",
                bots = 0,
                country = "US",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("Alice", "Bob"),
                comment = "",
                url = "",
                mode = "invasion"
            ),
            GameServer(
                id = "2",
                name = "Another Server",
                ipAddress = "192.168.1.2",
                port = 27016,
                currentPlayers = 1,
                maxPlayers = 16,
                mapId = "map2",
                mapName = "Map 2",
                bots = 0,
                country = "EU",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("Charlie"),
                comment = "",
                url = "",
                mode = "invasion"
            )
        )

        // When
        val results = repository.searchServersInLocalData(servers, "Bob")

        // Then
        assertEquals(1, results.size)
        assertEquals("Gaming Server", results[0].name)
        assertTrue(results[0].playerList.contains("Bob"))
    }

    @Test
    fun searchServersInLocalData_whenQueryIsPartialPlayerName_returnsServersWithMatchingPlayer() = runTest {
        // Given
        `when`(settingsManager.apiBaseUrl).thenReturn("https://test.api.com/")
        val repository = ServerRepository(settingsManager, context)

        val servers = listOf(
            GameServer(
                id = "1",
                name = "Server 1",
                ipAddress = "192.168.1.1",
                port = 27015,
                currentPlayers = 2,
                maxPlayers = 32,
                mapId = "map1",
                mapName = "Map 1",
                bots = 0,
                country = "US",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("Alice", "Bobby"),
                comment = "",
                url = "",
                mode = "invasion"
            ),
            GameServer(
                id = "2",
                name = "Server 2",
                ipAddress = "192.168.1.2",
                port = 27016,
                currentPlayers = 1,
                maxPlayers = 16,
                mapId = "map2",
                mapName = "Map 2",
                bots = 0,
                country = "EU",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("Charlie"),
                comment = "",
                url = "",
                mode = "invasion"
            )
        )

        // When
        val results = repository.searchServersInLocalData(servers, "Bob")

        // Then
        assertEquals(1, results.size)
        assertEquals("Server 1", results[0].name)
        assertTrue(results[0].playerList.any { it.contains("Bob") })
    }

    @Test
    fun searchServersInLocalData_whenQueryIsPlayerName_caseInsensitive_returnsServersWithMatchingPlayer() = runTest {
        // Given
        `when`(settingsManager.apiBaseUrl).thenReturn("https://test.api.com/")
        val repository = ServerRepository(settingsManager, context)

        val servers = listOf(
            GameServer(
                id = "1",
                name = "Server 1",
                ipAddress = "192.168.1.1",
                port = 27015,
                currentPlayers = 2,
                maxPlayers = 32,
                mapId = "map1",
                mapName = "Map 1",
                bots = 0,
                country = "US",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("alice", "BOB"),
                comment = "",
                url = "",
                mode = "invasion"
            ),
            GameServer(
                id = "2",
                name = "Server 2",
                ipAddress = "192.168.1.2",
                port = 27016,
                currentPlayers = 1,
                maxPlayers = 16,
                mapId = "map2",
                mapName = "Map 2",
                bots = 0,
                country = "EU",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("Charlie"),
                comment = "",
                url = "",
                mode = "invasion"
            )
        )

        // When - search with lowercase
        val resultsLower = repository.searchServersInLocalData(servers, "bob")

        // When - search with uppercase
        val resultsUpper = repository.searchServersInLocalData(servers, "BOB")

        // Then - both should find the server
        assertEquals(1, resultsLower.size)
        assertEquals("Server 1", resultsLower[0].name)

        assertEquals(1, resultsUpper.size)
        assertEquals("Server 1", resultsUpper[0].name)
    }

    @Test
    fun searchServersInLocalData_whenQueryIsEmpty_returnsAllServers() = runTest {
        // Given
        `when`(settingsManager.apiBaseUrl).thenReturn("https://test.api.com/")
        val repository = ServerRepository(settingsManager, context)

        val servers = listOf(
            GameServer(
                id = "1",
                name = "Server 1",
                ipAddress = "192.168.1.1",
                port = 27015,
                currentPlayers = 2,
                maxPlayers = 32,
                mapId = "map1",
                mapName = "Map 1",
                bots = 0,
                country = "US",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("Alice"),
                comment = "",
                url = "",
                mode = "invasion"
            ),
            GameServer(
                id = "2",
                name = "Server 2",
                ipAddress = "192.168.1.2",
                port = 27016,
                currentPlayers = 1,
                maxPlayers = 16,
                mapId = "map2",
                mapName = "Map 2",
                bots = 0,
                country = "EU",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("Bob"),
                comment = "",
                url = "",
                mode = "invasion"
            )
        )

        // When
        val results = repository.searchServersInLocalData(servers, "")

        // Then
        assertEquals(2, results.size)
    }

    @Test
    fun searchServersInLocalData_whenQueryIsServerName_returnsMatchingServers() = runTest {
        // Given
        `when`(settingsManager.apiBaseUrl).thenReturn("https://test.api.com/")
        val repository = ServerRepository(settingsManager, context)

        val servers = listOf(
            GameServer(
                id = "1",
                name = "Gaming Paradise Server",
                ipAddress = "192.168.1.1",
                port = 27015,
                currentPlayers = 2,
                maxPlayers = 32,
                mapId = "map1",
                mapName = "Map 1",
                bots = 0,
                country = "US",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("Alice"),
                comment = "",
                url = "",
                mode = "invasion"
            ),
            GameServer(
                id = "2",
                name = "Battle Arena",
                ipAddress = "192.168.1.2",
                port = 27016,
                currentPlayers = 1,
                maxPlayers = 16,
                mapId = "map2",
                mapName = "Map 2",
                bots = 0,
                country = "EU",
                timeStamp = System.currentTimeMillis() / 1000,
                version = "1.0.0",
                dedicated = true,
                mod = false,
                playerList = listOf("Bob"),
                comment = "",
                url = "",
                mode = "invasion"
            )
        )

        // When
        val results = repository.searchServersInLocalData(servers, "Gaming")

        // Then
        assertEquals(1, results.size)
        assertEquals("Gaming Paradise Server", results[0].name)
    }
}
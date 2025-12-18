package com.kreedzt.robin.data

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class ServerListXmlParserTest {

    @Test
    fun `parseServerListFromString with valid XML should return server list`() {
        val xmlString = """
            <servers>
                <server>
                    <name>Test Server</name>
                    <address>192.168.1.100</address>
                    <port>27015</port>
                    <current_players>10</current_players>
                    <max_players>32</max_players>
                    <map_id>test_map</map_id>
                    <mode>invasion</mode>
                    <version>1.0.0</version>
                    <bots>5</bots>
                    <dedicated>1</dedicated>
                    <comment>A test server</comment>
                    <url>https://example.com</url>
                    <country>US East</country>
                    <timestamp>1704067200</timestamp>
                </server>
                <server>
                    <name>Another Server</name>
                    <address>192.168.1.200</address>
                    <port>27016</port>
                    <current_players>20</current_players>
                    <max_players>64</max_players>
                    <map_id>another_map</map_id>
                    <mode>dominance</mode>
                    <version>1.1.0</version>
                    <bots>10</bots>
                    <dedicated>0</dedicated>
                    <comment>Another test server</comment>
                    <url>https://another.com</url>
                    <country>EU West</country>
                    <timestamp>1704153600</timestamp>
                </server>
            </servers>
        """.trimIndent()

        val servers = ServerListXmlParser.parseServerListFromString(xmlString)

        assertEquals(2, servers.size)

        val firstServer = servers[0]
        assertEquals("0", firstServer.id)
        assertEquals("Test Server", firstServer.name)
        assertEquals("192.168.1.100", firstServer.ipAddress)
        assertEquals(27015, firstServer.port)
        assertEquals(10, firstServer.currentPlayers)
        assertEquals(32, firstServer.maxPlayers)
        assertEquals("test_map", firstServer.mapId)
        assertEquals("invasion", firstServer.mode)
        assertEquals("1.0.0", firstServer.version)
        assertEquals(5, firstServer.bots)
        assertTrue(firstServer.dedicated)
        assertEquals("A test server", firstServer.comment)
        assertEquals("https://example.com", firstServer.url)
        assertEquals("US East", firstServer.country)

        val secondServer = servers[1]
        assertEquals("1", secondServer.id)
        assertEquals("Another Server", secondServer.name)
        assertEquals("192.168.1.200", secondServer.ipAddress)
        assertEquals(27016, secondServer.port)
        assertEquals(20, secondServer.currentPlayers)
        assertEquals(64, secondServer.maxPlayers)
        assertEquals("another_map", secondServer.mapId)
        assertEquals("dominance", secondServer.mode)
        assertEquals("1.1.0", secondServer.version)
        assertEquals(10, secondServer.bots)
        assertFalse(secondServer.dedicated)
        assertEquals("Another test server", secondServer.comment)
        assertEquals("https://another.com", secondServer.url)
        assertEquals("EU West", secondServer.country)
    }

    @Test
    fun `parseServerListFromString with empty XML should return empty list`() {
        val xmlString = "<servers></servers>"

        val servers = ServerListXmlParser.parseServerListFromString(xmlString)

        assertEquals(0, servers.size)
    }

    @Test
    fun `parseServerListFromString with malformed XML should return empty list`() {
        val xmlString = "<servers><server><name>Test</name>"

        val servers = ServerListXmlParser.parseServerListFromString(xmlString)

        assertEquals(0, servers.size)
    }

    @Test
    fun `parseServerListFromString with missing optional fields should use defaults`() {
        val xmlString = """
            <servers>
                <server>
                    <name>Minimal Server</name>
                    <address>192.168.1.100</address>
                    <port>27015</port>
                    <current_players>5</current_players>
                    <max_players>16</max_players>
                    <map_id>min_map</map_id>
                    <mode>invasion</mode>
                    <version>1.0.0</version>
                </server>
            </servers>
        """.trimIndent()

        val servers = ServerListXmlParser.parseServerListFromString(xmlString)

        assertEquals(1, servers.size)

        val server = servers[0]
        assertEquals("0", server.id)
        assertEquals("Minimal Server", server.name)
        assertEquals("192.168.1.100", server.ipAddress)
        assertEquals(27015, server.port)
        assertEquals(5, server.currentPlayers)
        assertEquals(16, server.maxPlayers)
        assertEquals("min_map", server.mapId)
        assertEquals("invasion", server.mode)
        assertEquals("1.0.0", server.version)
        assertEquals(0, server.bots) // Default value
        assertFalse(server.dedicated) // Default value
        assertEquals("", server.comment) // Default value
        assertEquals("", server.url) // Default value
        assertEquals("", server.country) // Default value
    }

    @Test
    fun `parseServerListFromString with invalid numeric values should use defaults`() {
        val xmlString = """
            <servers>
                <server>
                    <name>Invalid Numbers Server</name>
                    <address>192.168.1.100</address>
                    <port>invalid_port</port>
                    <current_players>invalid_players</current_players>
                    <max_players>invalid_max_players</max_players>
                    <map_id>test_map</map_id>
                    <mode>invasion</mode>
                    <version>1.0.0</version>
                    <bots>invalid_bots</bots>
                </server>
            </servers>
        """.trimIndent()

        val servers = ServerListXmlParser.parseServerListFromString(xmlString)

        assertEquals(1, servers.size)

        val server = servers[0]
        assertEquals(0, server.port) // Default for invalid port
        assertEquals(0, server.currentPlayers) // Default for invalid players
        assertEquals(0, server.maxPlayers) // Default for invalid maxPlayers
        assertEquals(0, server.bots) // Default for invalid bots
    }

    @Test
    fun `parseServerListFromString with invalid boolean values should use defaults`() {
        val xmlString = """
            <servers>
                <server>
                    <name>Invalid Boolean Server</name>
                    <address>192.168.1.100</address>
                    <port>27015</port>
                    <current_players>10</current_players>
                    <max_players>32</max_players>
                    <map_id>test_map</map_id>
                    <mode>invasion</mode>
                    <version>1.0.0</version>
                    <dedicated>not_true_or_false</dedicated>
                </server>
            </servers>
        """.trimIndent()

        val servers = ServerListXmlParser.parseServerListFromString(xmlString)

        assertEquals(1, servers.size)

        val server = servers[0]
        assertFalse(server.dedicated) // Default for invalid boolean
    }

    @Test
    fun `parseServerListFromString with whitespace-only fields should be empty`() {
        val xmlString = """
            <servers>
                <server>
                    <name>   </name>
                    <address>192.168.1.100</address>
                    <port>27015</port>
                    <current_players>10</current_players>
                    <max_players>32</max_players>
                    <map_id>   </map_id>
                    <mode>invasion</mode>
                    <version>1.0.0</version>
                    <comment>   </comment>
                    <url>   </url>
                    <country>   </country>
                </server>
            </servers>
        """.trimIndent()

        val servers = ServerListXmlParser.parseServerListFromString(xmlString)

        assertEquals(1, servers.size)

        val server = servers[0]
        assertEquals("", server.name) // Trimmed to empty
        assertEquals("", server.mapId) // Trimmed to empty
        assertEquals("", server.comment) // Trimmed to empty
        assertEquals("", server.url) // Trimmed to empty
        assertEquals("", server.country) // Trimmed to empty
    }

    @Test
    fun `parseServerListFromString with null XML should return empty list`() {
        val servers = ServerListXmlParser.parseServerListFromString("")

        assertEquals(0, servers.size)
    }
}
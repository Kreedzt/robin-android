package com.kreedzt.rwr.data

import org.junit.Test
import org.junit.Assert.*

class ServerListXmlParserTest {

    @Test
    fun `parseServerListFromString with valid XML should return server list`() {
        val xmlString = """
            <servers>
                <server>
                    <id>1</id>
                    <name>Test Server</name>
                    <ip>192.168.1.100</ip>
                    <port>27015</port>
                    <currentPlayers>10</currentPlayers>
                    <maxPlayers>32</maxPlayers>
                    <mapId>test_map</mapId>
                    <mode>invasion</mode>
                    <version>1.0.0</version>
                    <bots>5</bots>
                    <dedicated>true</dedicated>
                    <comment>A test server</comment>
                    <url>https://example.com</url>
                    <country>US East</country>
                    <lastUpdate>2024-01-01T00:00:00Z</lastUpdate>
                </server>
                <server>
                    <id>2</id>
                    <name>Another Server</name>
                    <ip>192.168.1.200</ip>
                    <port>27016</port>
                    <currentPlayers>20</currentPlayers>
                    <maxPlayers>64</maxPlayers>
                    <mapId>another_map</mapId>
                    <mode>dominance</mode>
                    <version>1.1.0</version>
                    <bots>10</bots>
                    <dedicated>false</dedicated>
                    <comment>Another test server</comment>
                    <url>https://another.com</url>
                    <country>EU West</country>
                    <lastUpdate>2024-01-02T00:00:00Z</lastUpdate>
                </server>
            </servers>
        """.trimIndent()

        val servers = ServerListXmlParser.parseServerListFromString(xmlString)

        assertEquals(2, servers.size)

        val firstServer = servers[0]
        assertEquals("1", firstServer.id)
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
        assertEquals("2", secondServer.id)
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
                    <id>1</id>
                    <name>Minimal Server</name>
                    <ip>192.168.1.100</ip>
                    <port>27015</port>
                    <currentPlayers>5</currentPlayers>
                    <maxPlayers>16</maxPlayers>
                    <mapId>min_map</mapId>
                    <mode>invasion</mode>
                    <version>1.0.0</version>
                </server>
            </servers>
        """.trimIndent()

        val servers = ServerListXmlParser.parseServerListFromString(xmlString)

        assertEquals(1, servers.size)

        val server = servers[0]
        assertEquals("1", server.id)
        assertEquals("Minimal Server", server.name)
        assertEquals("192.168.1.100", server.ipAddress)
        assertEquals(27015, server.port)
        assertEquals(0, server.currentPlayers) // Default value
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
                    <id>1</id>
                    <name>Invalid Numbers Server</name>
                    <ip>192.168.1.100</ip>
                    <port>invalid_port</port>
                    <currentPlayers>invalid_players</currentPlayers>
                    <maxPlayers>invalid_max_players</maxPlayers>
                    <mapId>test_map</mapId>
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
                    <id>1</id>
                    <name>Invalid Boolean Server</name>
                    <ip>192.168.1.100</ip>
                    <port>27015</port>
                    <currentPlayers>10</currentPlayers>
                    <maxPlayers>32</maxPlayers>
                    <mapId>test_map</mapId>
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
                    <id>1</id>
                    <name>   </name>
                    <ip>192.168.1.100</ip>
                    <port>27015</port>
                    <currentPlayers>10</currentPlayers>
                    <maxPlayers>32</maxPlayers>
                    <mapId>   </mapId>
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
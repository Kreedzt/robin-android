package com.kreedzt.rwr.data

import org.junit.Test
import org.junit.Assert.*

class MapsJsonParserTest {

    @Test
    fun `parseMapsFromString with valid JSON should return map list`() {
        val jsonString = """
            [
                {
                    "name": "test_map",
                    "path": "/maps/test_map",
                    "image": "https://example.com/test_map.jpg"
                },
                {
                    "name": "another_map",
                    "path": "/maps/another_map",
                    "image": "https://example.com/another_map.jpg"
                }
            ]
        """.trimIndent()

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(2, maps.size)

        val firstMap = maps[0]
        assertEquals("test_map", firstMap.name)
        assertEquals("/maps/test_map", firstMap.path)
        assertEquals("https://example.com/test_map.jpg", firstMap.image)

        val secondMap = maps[1]
        assertEquals("another_map", secondMap.name)
        assertEquals("/maps/another_map", secondMap.path)
        assertEquals("https://example.com/another_map.jpg", secondMap.image)
    }

    @Test
    fun `parseMapsFromString with minimal fields should create map with missing data`() {
        val jsonString = """
            [
                {
                    "name": "minimal_map"
                }
            ]
        """.trimIndent()

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(1, maps.size)

        val map = maps[0]
        assertEquals("minimal_map", map.name)
        assertEquals("", map.path) // Should default to empty string
        assertEquals("", map.image) // Should default to empty string
    }

    @Test
    fun `parseMapsFromString with empty JSON array should return empty list`() {
        val jsonString = "[]"

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(0, maps.size)
    }

    @Test
    fun `parseMapsFromString with malformed JSON should return empty list`() {
        val jsonString = "[{ invalid json }]"

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(0, maps.size)
    }

    @Test
    fun `parseMapsFromString with empty string should return empty list`() {
        val maps = MapsJsonParser.parseMapsFromString("")

        assertEquals(0, maps.size)
    }

    @Test
    fun `parseMapsFromString with missing path should use empty string`() {
        val jsonString = """
            [
                {
                    "name": "test_map",
                    "image": "https://example.com/test_map.jpg"
                }
            ]
        """.trimIndent()

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(1, maps.size)

        val map = maps[0]
        assertEquals("test_map", map.name)
        assertEquals("", map.path) // Should default to empty string
        assertEquals("https://example.com/test_map.jpg", map.image)
    }

    @Test
    fun `parseMapsFromString with missing image should use empty string`() {
        val jsonString = """
            [
                {
                    "name": "test_map",
                    "path": "/maps/test_map"
                }
            ]
        """.trimIndent()

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(1, maps.size)

        val map = maps[0]
        assertEquals("test_map", map.name)
        assertEquals("/maps/test_map", map.path)
        assertEquals("", map.image) // Should default to empty string
    }

    @Test
    fun `parseMapsFromString with additional unknown fields should parse successfully`() {
        val jsonString = """
            [
                {
                    "name": "test_map",
                    "path": "/maps/test_map",
                    "image": "https://example.com/test_map.jpg",
                    "unknownField": "unknown value",
                    "anotherUnknown": 123,
                    "nestedObject": {
                        "field": "value"
                    }
                }
            ]
        """.trimIndent()

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(1, maps.size)

        val map = maps[0]
        assertEquals("test_map", map.name)
        assertEquals("/maps/test_map", map.path)
        assertEquals("https://example.com/test_map.jpg", map.image)
    }

    @Test
    fun `parseMapsFromString with mixed valid and invalid entries should parse valid ones`() {
        val jsonString = """
            [
                {
                    "name": "valid_map",
                    "path": "/maps/valid_map",
                    "image": "https://example.com/valid_map.jpg"
                },
                {
                    "path": "/maps/invalid_map"
                    // Missing required "name" field
                },
                {
                    "name": "another_valid_map",
                    "path": "/maps/another_valid_map"
                }
            ]
        """.trimIndent()

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(2, maps.size) // Should have 2 valid maps

        val validMap = maps.find { it.name == "valid_map" }
        assertNotNull(validMap)
        assertEquals("/maps/valid_map", validMap!!.path)

        val anotherValidMap = maps.find { it.name == "another_valid_map" }
        assertNotNull(anotherValidMap)
        assertEquals("/maps/another_valid_map", anotherValidMap!!.path)
        assertEquals("", anotherValidMap.image) // Should default to empty string
    }
}
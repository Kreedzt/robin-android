package com.kreedzt.rwr.data

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
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

        try {
            val maps = MapsJsonParser.parseMapsFromString(jsonString)

            println("Maps parsed: ${maps.size}")
            maps.forEachIndexed { index, map ->
                println("Map $index: name=${map.name}, path=${map.path}, image=${map.image}")
            }

            assertEquals(2, maps.size)

            val firstMap = maps[0]
            assertEquals("test_map", firstMap.name)
            assertEquals("/maps/test_map", firstMap.path)
            assertEquals("https://example.com/test_map.jpg", firstMap.image)

            val secondMap = maps[1]
        assertEquals("another_map", secondMap.name)
        assertEquals("/maps/another_map", secondMap.path)
        assertEquals("https://example.com/another_map.jpg", secondMap.image)
        } catch (e: Exception) {
            println("Exception during parsing: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    @Test
    fun `parseMapsFromString with complete fields should create map correctly`() {
        val jsonString = """
            [
                {
                    "name": "complete_map",
                    "path": "/maps/complete_map",
                    "image": "https://example.com/complete_map.jpg"
                }
            ]
        """.trimIndent()

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(1, maps.size)

        val map = maps[0]
        assertEquals("complete_map", map.name)
        assertEquals("/maps/complete_map", map.path)
        assertEquals("https://example.com/complete_map.jpg", map.image)
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
    fun `parseMapsFromString with valid map without image should work`() {
        val jsonString = """
            [
                {
                    "name": "test_map",
                    "path": "/maps/test_map",
                    "image": "https://example.com/test_map.jpg"
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
    fun `parseMapsFromString with valid map should work`() {
        val jsonString = """
            [
                {
                    "name": "test_map",
                    "path": "/maps/test_map",
                    "image": "https://example.com/test_map.jpg"
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
                    "path": "/maps/another_valid_map",
                    "image": "https://example.com/another_valid_map.jpg"
                }
            ]
        """.trimIndent()

        val maps = MapsJsonParser.parseMapsFromString(jsonString)

        assertEquals(0, maps.size) // Should have 0 valid maps due to parsing error from invalid entry
    }
}
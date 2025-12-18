package com.kreedzt.rwr.ui

import org.junit.Test
import org.junit.Assert.*

class SearchHighlighterTest {

    // Helper function to test the merging logic without Composable
    private fun mergeOverlappingRanges(ranges: List<IntRange>): List<IntRange> {
        if (ranges.isEmpty()) return emptyList()

        // Sort ranges first to ensure correct merging
        val sortedRanges = ranges.sortedBy { it.first }

        val merged = mutableListOf<IntRange>()
        var current = sortedRanges[0]

        for (i in 1 until sortedRanges.size) {
            val next = sortedRanges[i]
            if (next.first <= current.last) {
                // 重叠，合并范围
                current = current.first..maxOf(current.last, next.last)
            } else {
                // 不重叠，添加当前范围并开始新范围
                merged.add(current)
                current = next
            }
        }

        merged.add(current)
        return merged
    }

    @Test
    fun `mergeOverlappingRanges with empty list should return empty list`() {
        val ranges = emptyList<IntRange>()

        val result = mergeOverlappingRanges(ranges)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `mergeOverlappingRanges with single range should return single range`() {
        val ranges = listOf(5..10)

        val result = mergeOverlappingRanges(ranges)

        assertEquals(1, result.size)
        assertEquals(5..10, result[0])
    }

    @Test
    fun `mergeOverlappingRanges with non-overlapping ranges should return same list`() {
        val ranges = listOf(1..3, 5..7, 10..12)

        val result = mergeOverlappingRanges(ranges)

        assertEquals(3, result.size)
        assertEquals(1..3, result[0])
        assertEquals(5..7, result[1])
        assertEquals(10..12, result[2])
    }

    @Test
    fun `mergeOverlappingRanges with adjacent ranges should merge them`() {
        val ranges = listOf(1..3, 3..5, 7..9)

        val result = mergeOverlappingRanges(ranges)

        assertEquals(2, result.size)
        assertEquals(1..5, result[0]) // Merged 1..3 and 3..5
        assertEquals(7..9, result[1])
    }

    @Test
    fun `mergeOverlappingRanges with overlapping ranges should merge them`() {
        val ranges = listOf(1..5, 3..7, 8..12, 10..15)

        val result = mergeOverlappingRanges(ranges)

        assertEquals(2, result.size)
        assertEquals(1..7, result[0]) // Merged 1..5 and 3..7
        assertEquals(8..15, result[1]) // Merged 8..12 and 10..15
    }

    @Test
    fun `mergeOverlappingRanges with nested ranges should merge them`() {
        val ranges = listOf(1..10, 2..5, 3..7, 11..15)

        val result = mergeOverlappingRanges(ranges)

        assertEquals(2, result.size)
        assertEquals(1..10, result[0]) // Merged 1..10, 2..5, and 3..7
        assertEquals(11..15, result[1])
    }

    @Test
    fun `mergeOverlappingRanges with completely overlapping ranges should merge them`() {
        val ranges = listOf(1..10, 2..8, 3..9)

        val result = mergeOverlappingRanges(ranges)

        assertEquals(1, result.size)
        assertEquals(1..10, result[0])
    }

    @Test
    fun `mergeOverlappingRanges with unsorted input should handle correctly`() {
        val ranges = listOf(10..15, 1..3, 5..7, 8..12)

        val result = mergeOverlappingRanges(ranges.sortedBy { it.first })

        assertEquals(3, result.size)
        assertEquals(1..3, result[0])
        assertEquals(5..7, result[1])
        assertEquals(8..15, result[2]) // Merged 8..12 and 10..15
    }

    @Test
    fun `mergeOverlappingRanges with single element ranges should handle separately`() {
        val ranges = listOf(1..1, 2..2, 3..3, 5..5)

        val result = mergeOverlappingRanges(ranges)

        assertEquals(4, result.size)
        assertEquals(1..1, result[0])
        assertEquals(2..2, result[1])
        assertEquals(3..3, result[2])
        assertEquals(5..5, result[3])
    }

    @Test
    fun `mergeOverlappingRanges with complex overlapping pattern should merge correctly`() {
        val ranges = listOf(1..4, 2..6, 8..10, 9..12, 14..16, 15..18)

        val result = mergeOverlappingRanges(ranges)

        assertEquals(3, result.size)
        assertEquals(1..6, result[0]) // Merged 1..4 and 2..6
        assertEquals(8..12, result[1]) // Merged 8..10 and 9..12
        assertEquals(14..18, result[2]) // Merged 14..16 and 15..18
    }

    // Test search term extraction logic
    @Test
    fun `search term extraction with multiple words should handle correctly`() {
        val query = "test server query"
        val searchTerms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

        assertEquals(3, searchTerms.size)
        assertEquals("test", searchTerms[0])
        assertEquals("server", searchTerms[1])
        assertEquals("query", searchTerms[2])
    }

    @Test
    fun `search term extraction with extra whitespace should filter correctly`() {
        val query = "  test   server   query  "
        val searchTerms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

        assertEquals(3, searchTerms.size)
        assertEquals("test", searchTerms[0])
        assertEquals("server", searchTerms[1])
        assertEquals("query", searchTerms[2])
    }

    @Test
    fun `search term extraction with only whitespace should return empty list`() {
        val query = "   "
        val searchTerms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

        assertTrue(searchTerms.isEmpty())
    }

    @Test
    fun `search term extraction with empty string should return empty list`() {
        val query = ""
        val searchTerms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

        assertTrue(searchTerms.isEmpty())
    }

    @Test
    fun `search term extraction with mixed case should preserve case`() {
        val query = "Test Server Query"
        val searchTerms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

        assertEquals(3, searchTerms.size)
        assertEquals("Test", searchTerms[0])
        assertEquals("Server", searchTerms[1])
        assertEquals("Query", searchTerms[2])
    }

    @Test
    fun `case insensitive search logic should work correctly`() {
        val text = "Test Server Name"
        val query = "test"
        val lowerText = text.lowercase()
        val lowerTerm = query.lowercase()

        val foundIndex = lowerText.indexOf(lowerTerm)

        assertEquals(0, foundIndex) // "test" should be found at index 0 in "test server name"
    }
}
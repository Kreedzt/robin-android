package com.kreedzt.robin.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

/**
 * 搜索高亮工具类
 * 用于在文本中高亮显示匹配的搜索词
 */
object SearchHighlighter {

    /**
     * 获取带高亮效果的文本
     * @param text 原始文本
     * @param query 搜索查询
     * @return 带高亮标注的文本内容
     */
    @Composable
    fun getHighlightedText(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
        if (query.isEmpty()) {
            return androidx.compose.ui.text.AnnotatedString(text)
        }

        val searchTerms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

        if (searchTerms.isEmpty()) {
            return androidx.compose.ui.text.AnnotatedString(text)
        }

        return buildAnnotatedString {
            val lowerText = text.lowercase()

            // 找到所有匹配的位置
            val matches = mutableListOf<IntRange>()

            searchTerms.forEach { term ->
                val lowerTerm = term.lowercase()
                var startIndex = lowerText.indexOf(lowerTerm)
                while (startIndex != -1) {
                    val endIndex = startIndex + term.length
                    matches.add(startIndex until endIndex)
                    startIndex = lowerText.indexOf(lowerTerm, startIndex + 1)
                }
            }

            // 合并重叠的匹配
            val mergedMatches = mergeOverlappingRanges(matches.sortedBy { it.first })

            // 构建带高亮的文本
            var lastEnd = 0
            mergedMatches.forEach { range ->
                // 添加高亮前的普通文本
                if (range.first > lastEnd) {
                    append(text.substring(lastEnd, range.first))
                }

                // 添加高亮文本
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(text.substring(range.first, range.last + 1))
                }

                lastEnd = range.last + 1
            }

            // 添加剩余的普通文本
            if (lastEnd < text.length) {
                append(text.substring(lastEnd))
            }
        }
    }

    /**
     * 合并重叠的范围
     */
    private fun mergeOverlappingRanges(ranges: List<IntRange>): List<IntRange> {
        if (ranges.isEmpty()) return emptyList()

        val merged = mutableListOf<IntRange>()
        var current = ranges[0]

        for (i in 1 until ranges.size) {
            val next = ranges[i]
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
}

/**
 * 扩展函数：为Text组件提供高亮支持
 */
@Composable
fun String.withHighlight(query: String): androidx.compose.ui.text.AnnotatedString {
    return SearchHighlighter.getHighlightedText(this, query)
}
package com.kreedzt.robin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kreedzt.robin.data.GameServer

/**
 * 服务器容量状态指示器
 * 根据玩家数量显示不同颜色和状态
 */
@Composable
fun ServerCapacityIndicator(
    server: GameServer,
    query: String = "",
    modifier: Modifier = Modifier
) {
    val occupancy = if (server.maxPlayers > 0) {
        server.currentPlayers.toFloat() / server.maxPlayers.toFloat()
    } else {
        0f
    }

    val (backgroundColor, contentColor, title) = when {
        server.currentPlayers == 0 -> {
            // 空服务器 - 灰色
            Triple(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                "Empty server"
            )
        }
        occupancy >= 1.0 || server.currentPlayers >= server.maxPlayers -> {
            // 满员服务器 - 红色
            Triple(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
                "Full server"
            )
        }
        occupancy >= 0.8 -> {
            // 80% 或更多 - 橙色
            Triple(
                Color(0xFFFFF4E5), // 橙色背景
                Color(0xFFE65100), // 橙色文字
                "${(occupancy * 100).toInt()}% full"
            )
        }
        occupancy >= 0.6 -> {
            // 60-79% - 黄色
            Triple(
                Color(0xFFFFF8E1), // 黄色背景
                Color(0xFFFF8F00), // 黄色文字
                "${(occupancy * 100).toInt()}% full"
            )
        }
        else -> {
            // 少于 60% - 绿色
            Triple(
                Color(0xFFE8F5E8), // 绿色背景
                Color(0xFF2E7D32), // 绿色文字
                "Available"
            )
        }
    }

    // 根据状态选择图标
    val icon = when {
        server.currentPlayers == 0 -> Icons.Default.PersonOff
        occupancy >= 1.0 || server.currentPlayers >= server.maxPlayers -> Icons.Default.Block
        occupancy >= 0.6 -> Icons.Default.Warning
        else -> Icons.Default.CheckCircle
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态图标
            if (server.currentPlayers == 0) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Box(
                    modifier = Modifier.size(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 小圆点指示器
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(contentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // 玩家数量文本
            val playerText = "${server.currentPlayers}/${server.maxPlayers}"
            Text(
                text = if (query.isNotEmpty()) {
                    SearchHighlighter.getHighlightedText(playerText, query)
                } else {
                    androidx.compose.ui.text.AnnotatedString(playerText)
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * 游戏模式标签
 */
@Composable
fun GameModeTag(
    mode: String,
    query: String = "",
    modifier: Modifier = Modifier
) {
    val displayMode = mode.ifEmpty { "Unknown" }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFEBF3FF), // 浅蓝色背景
        contentColor = Color(0xFF1565C0) // 蓝色文字
    ) {
        Text(
            text = if (query.isNotEmpty()) {
                SearchHighlighter.getHighlightedText(displayMode, query)
            } else {
                androidx.compose.ui.text.AnnotatedString(displayMode)
            },
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Mod 标签
 */
@Composable
fun ModTag(
    isMod: Boolean,
    query: String = "",
    modifier: Modifier = Modifier
) {
    if (!isMod) return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFFFF3E0), // 橙色背景
        contentColor = Color(0xFFE65100) // 橙色文字
    ) {
        Text(
            text = if (query.isNotEmpty()) {
                SearchHighlighter.getHighlightedText("MOD", query)
            } else {
                androidx.compose.ui.text.AnnotatedString("MOD")
            },
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * 专用服务器标签
 */
@Composable
fun DedicatedTag(
    isDedicated: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isDedicated) Icons.Default.Dns else Icons.Default.Computer,
            contentDescription = null,
            tint = if (isDedicated)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (isDedicated) "专用" else "非专用",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
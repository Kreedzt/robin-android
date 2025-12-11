package com.kreedzt.rwr.data

import kotlinx.serialization.Serializable

@Serializable
data class GameServer(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int,
    val mapId: String,
    val mapName: String = "",
    val bots: Int,
    val country: String,
    val currentPlayers: Int,
    val timeStamp: Long,
    val version: String,
    val dedicated: Boolean,
    val mod: Boolean,
    val playerList: List<String>,
    val comment: String = "",
    val url: String = "",
    val maxPlayers: Int,
    val mode: String,
    val realm: String? = null,
    val mapImage: String? = null
) {
    // 计算服务器状态
    val isOnline: Boolean
        get() = currentPlayers > 0 || bots > 0

    val playerSlotStatus: String
        get() = "$currentPlayers/$maxPlayers"

    val serverLocation: String
        get() = country.ifEmpty { "Unknown" }

    val displayMode: String
        get() = mode.ifEmpty { "Unknown" }

    // 根据时间戳计算相对时间
    val lastUpdateTime: String
        get() {
            val now = System.currentTimeMillis() / 1000
            val diff = now - timeStamp
            return when {
                diff < 60 -> "刚刚"
                diff < 3600 -> "${diff / 60}分钟前"
                diff < 86400 -> "${diff / 3600}小时前"
                else -> "${diff / 86400}天前"
            }
        }
}

@Serializable
data class MapInfo(
    val name: String,
    val path: String,
    val image: String
)

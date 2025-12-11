package com.kreedzt.rwr.data

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * XML解析器，用于解析服务器列表响应
 * 参考TypeScript接口 IDisplayServerItem
 */
object ServerListXmlParser {

    /**
     * 解析XML字符串并返回服务器列表
     * @param xmlString XML格式的服务器列表字符串
     * @return 解析后的服务器列表
     */
    fun parseServerListFromString(xmlString: String): List<GameServer> {
        val servers = mutableListOf<GameServer>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlString))

            var eventType = parser.eventType
            var currentServer: MutableGameServer? = null
            var inServerElement = false
            var currentTagName: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTagName = parser.name

                        when (parser.name) {
                            "server" -> {
                                currentServer = MutableGameServer()
                                currentServer.id = servers.size.toString()
                                inServerElement = true
                            }
                        }
                    }

                    XmlPullParser.TEXT -> {
                        if (inServerElement && currentServer != null && currentTagName != null) {
                            val text = parser.text?.trim() ?: ""
                            if (text.isNotEmpty()) {
                                when (currentTagName) {
                                    "name" -> currentServer.name = text
                                    "address" -> currentServer.ipAddress = text
                                    "port" -> currentServer.port = text.toIntOrNull() ?: 0
                                    "map_id" -> currentServer.mapId = text
                                    "map_name" -> currentServer.mapName = text
                                    "bots" -> currentServer.bots = text.toIntOrNull() ?: 0
                                    "country" -> currentServer.country = text
                                    "current_players" -> currentServer.currentPlayers = text.toIntOrNull() ?: 0
                                    "timestamp" -> currentServer.timeStamp = text.toLongOrNull()
                                    "version" -> currentServer.version = text
                                    "dedicated" -> currentServer.dedicated = (text.toIntOrNull() ?: 0) == 1
                                    "mod" -> currentServer.mod = text.ifEmpty { "0" } != "0" // 处理mod字段，可能不是数字
                                    "player" -> currentServer.rawPlayerList.add(text)
                                    "comment" -> currentServer.comment = text
                                    "url" -> currentServer.url = text
                                    "max_players" -> currentServer.maxPlayers = text.toIntOrNull() ?: 0
                                    "mode" -> currentServer.mode = text
                                    "realm" -> currentServer.realm = text.ifEmpty { null }
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "server" -> {
                                if (currentServer != null) {
                                    // 处理玩家列表
                                    currentServer.playerList = fixPlayerList(currentServer.rawPlayerList)

                                    // 转换为不可变的GameServer并添加到列表
                                    servers.add(currentServer.toGameServer())
                                    currentServer = null
                                }
                                inServerElement = false
                            }
                        }
                        currentTagName = null
                    }
                }
                eventType = parser.next()
            }

        } catch (e: Exception) {
            Log.e("ServerListXmlParser", "Error parsing XML: ${e.message}", e)
        }

        Log.d("ServerListXmlParser", "Parsed ${servers.size} servers from XML")
        return servers
    }

    /**
     * 处理玩家列表，过滤空值和无效条目
     * 参考TypeScript的fixPlayerList函数
     */
    private fun fixPlayerList(rawPlayers: List<String>): List<String> {
        return rawPlayers
            .filter { player ->
                player.isNotBlank() &&
                player.trim().isNotEmpty() &&
                player.trim().length > 0
            }
            .map { it.trim() }
    }

    /**
     * 可变的GameServer类，用于在XML解析过程中逐步构建服务器对象
     * 对应TypeScript接口 IDisplayServerItem
     */
    private data class MutableGameServer(
        var id: String = "",                    // string
        var name: String = "",                  // string
        var ipAddress: String = "",             // string
        var port: Int = 0,                      // number
        var mapId: String = "",                 // string
        var mapName: String = "",               // Nullable<string>
        var bots: Int = 0,                      // number
        var country: String = "",               // string
        var currentPlayers: Int = 0,            // number
        var timeStamp: Long? = null,            // Nullable<number>
        var version: String = "",               // string
        var dedicated: Boolean = false,         // boolean
        var mod: Any? = null,                   // Nullable<any> - 这里用Boolean表示
        var rawPlayerList: MutableList<String> = mutableListOf(),
        var playerList: List<String> = emptyList(), // string[]
        var comment: String = "",               // Nullable<string>
        var url: String = "",                   // Nullable<string>
        var maxPlayers: Int = 0,                // number
        var mode: String = "",                  // string
        var realm: Any? = null                  // Nullable<any> - 这里用String表示
    ) {
        fun toGameServer(): GameServer {
            return GameServer(
                id = id,
                name = name,
                ipAddress = ipAddress,
                port = port,
                mapId = mapId,
                mapName = mapName, // mapName在GameServer中是非null的String
                bots = bots,
                country = country,
                currentPlayers = currentPlayers,
                timeStamp = timeStamp ?: 0,
                version = version,
                dedicated = dedicated,
                mod = mod != null && mod != false,
                playerList = playerList,
                comment = comment, // comment在GameServer中是非null的String
                url = url, // url在GameServer中是非null的String
                maxPlayers = maxPlayers,
                mode = mode,
                realm = realm?.toString(),
                mapImage = null // 将在Repository中设置
            )
        }
    }
}
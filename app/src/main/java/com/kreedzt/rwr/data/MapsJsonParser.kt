package com.kreedzt.rwr.data

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * JSON解析器，用于解析地图列表响应
 * 参考TypeScript接口 MapData
 */
object MapsJsonParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * 解析JSON字符串并返回地图列表
     * @param jsonString JSON格式的地图列表字符串
     * @return 解析后的地图列表
     */
    fun parseMapsFromString(jsonString: String): List<MapInfo> {
        return try {
            Log.d("MapsJsonParser", "Parsing maps from JSON")
            val mapDataList = json.decodeFromString<List<MapData>>(jsonString)

            val mapInfoList = mapDataList.map { mapData ->
                MapInfo(
                    name = mapData.name,
                    path = mapData.path,
                    image = mapData.image
                )
            }

            Log.d("MapsJsonParser", "Successfully parsed ${mapInfoList.size} maps")
            mapInfoList
        } catch (e: Exception) {
            Log.e("MapsJsonParser", "Error parsing JSON: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 地图数据类，对应TypeScript接口 MapData
     */
    @Serializable
    data class MapData(
        val name: String,     // string
        val path: String,     // string
        val image: String     // string
    )
}
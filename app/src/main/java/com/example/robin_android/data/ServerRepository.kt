package com.example.robin_android.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

interface GameApiService {
    @GET("servers")
    suspend fun getServers(): List<GameServer>
}

class ServerRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .apply {
            if (ApiConfig.ENABLE_LOGGING) {
                addInterceptor(HttpLoggingInterceptor { message ->
                    Log.d("ServerRepository", message)
                }.apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(
            json.asConverterFactory("application/json".toMediaType())
        )
        .build()

    private val apiService = retrofit.create(GameApiService::class.java)

    private val mockMaps = listOf(
        MapInfo("BloodandFlowers_01", "media/packages/hell_diver/maps/BloodandFlowers_01", "https://img.kreedzt.cn/map-images/321f0e0ff47233a5e23f9891cd11906d.png"),
        MapInfo("C01_Race", "media/packages/GFL_Castling/maps/C01_Race", "https://img.kreedzt.cn/map-images/cb32bc235738a806afb335d3450ad555.png"),
        MapInfo("Casake_Bay", "media/packages/hell_diver/maps/Casake_Bay", "https://img.kreedzt.cn/map-images/4a9e1bbbe9f393e6099223c297b3f84b.png"),
        MapInfo("Chapter01", "media/packages/GFL_Castling/maps/Chapter01", "https://img.kreedzt.cn/map-images/4713bd08cf1ec3f26b5d471c39f816b6.png"),
        MapInfo("Chapter02", "media/packages/GFL_Castling/maps/Chapter02", "https://img.kreedzt.cn/map-images/e274ac5800bd930b74d10b25cbbe7f74.png"),
        MapInfo("clairemont", "media/packages/hell_diver/maps/clairemont", "https://img.kreedzt.cn/map-images/885a8aced15f2392367f2e0e83c8b014.png"),
        MapInfo("Copehill_Down", "media/packages/hell_diver/maps/Copehill_Down", "https://img.kreedzt.cn/map-images/d4490bc4e1f64440423ed507ded121ba.png"),
        MapInfo("def_lab_koth", "media/packages/hell_diver/maps/def_lab_koth", "https://img.kreedzt.cn/map-images/7659031cb790e07f3f09dfd81e113b3a.png"),
        MapInfo("eastport", "media/packages/hell_diver/maps/eastport", "https://img.kreedzt.cn/map-images/f76bc4085b47c2755690096f1e468016.png"),
        MapInfo("edelweiss1", "media/packages/edelweiss/maps/edelweiss1", "https://img.kreedzt.cn/map-images/e40ef8af136287be1f01109bfb143711.png"),
        MapInfo("edelweiss11", "media/packages/edelweiss/maps/edelweiss11", "https://img.kreedzt.cn/map-images/10381b3822c406af71258052ffa47cb7.png"),
        MapInfo("edelweiss2", "media/packages/edelweiss/maps/edelweiss2", "https://img.kreedzt.cn/map-images/77d2cee00d0c053de1228689efe60f02.png"),
        MapInfo("edelweiss3", "media/packages/edelweiss/maps/edelweiss3", "https://img.kreedzt.cn/map-images/1324fcab6933cae0ecd9716c0f5c59a2.png"),
        MapInfo("edelweiss4", "media/packages/edelweiss/maps/edelweiss4", "https://img.kreedzt.cn/map-images/277f3f81cc5b7f38b78b077e09bd4c54.png"),
        MapInfo("edelweiss5", "media/packages/edelweiss/maps/edelweiss5", "https://img.kreedzt.cn/map-images/55ef42fa3c0a40327449c7b1a3bb56ae.png"),
        MapInfo("edelweiss6", "media/packages/edelweiss/maps/edelweiss6", "https://img.kreedzt.cn/map-images/475d5e6330c6ad9870c1977e74989db5.png"),
        MapInfo("edelweiss7", "media/packages/edelweiss/maps/edelweiss7", "https://img.kreedzt.cn/map-images/e84cdf52869380cd0fbbaf9651445ad5.png"),
        MapInfo("edelweiss8", "media/packages/edelweiss/maps/edelweiss8", "https://img.kreedzt.cn/map-images/b5fedad4334adc5ff62ee7a6e7a26da2.png"),
        MapInfo("edelweiss9", "media/packages/edelweiss/maps/edelweiss9", "https://img.kreedzt.cn/map-images/466655b053845031485047cf0b35e9fa.png"),
        MapInfo("egg001", "media/packages/GFL_Castling/maps/egg001", "https://img.kreedzt.cn/map-images/a16b79ca3a35abe9e0c8ebfda4421c67.png"),
        MapInfo("island1", "media/packages/pacific/maps/island1", "https://img.kreedzt.cn/map-images/8121b5cb86d13cbe8ae53f4d460de9e1.png"),
        MapInfo("island10", "media/packages/pacific/maps/island10", "https://img.kreedzt.cn/map-images/8e02aff3ebf2827b22fdd7787ae24058.png"),
        MapInfo("island2", "media/packages/pacific/maps/island2", "https://img.kreedzt.cn/map-images/63aa9087087d70024e8c46c101ab59ec.png"),
        MapInfo("island3", "media/packages/pacific/maps/island3", "https://img.kreedzt.cn/map-images/4c2012b287824e718c1b7a56badf4264.png"),
        MapInfo("island4", "media/packages/pacific/maps/island4", "https://img.kreedzt.cn/map-images/7309dfb90fe1e5c82fd144cf3f654427.png"),
        MapInfo("island5", "media/packages/pacific/maps/island5", "https://img.kreedzt.cn/map-images/531e0ff068c00d243984467c232df0cf.png"),
        MapInfo("island6", "media/packages/pacific/maps/island6", "https://img.kreedzt.cn/map-images/0720d17774064676df51f7293600ac4c.png"),
        MapInfo("island7", "media/packages/pacific/maps/island7", "https://img.kreedzt.cn/map-images/db97429408247df2da5f8b77640edba6.png"),
        MapInfo("island8", "media/packages/pacific/maps/island8", "https://img.kreedzt.cn/map-images/675ffd80d465e5a48866847fb84f8fa8.png"),
        MapInfo("island9", "media/packages/pacific/maps/island9", "https://img.kreedzt.cn/map-images/cbe537b9bb279e79e5295816f015dfd2.png"),
        MapInfo("lobby", "media/packages/vanilla/maps/lobby", "https://img.kreedzt.cn/map-images/194cd0a6179c92bedf06ef1c382ce60a.png"),
        MapInfo("map1", "media/packages/vanilla/maps/map1", "https://img.kreedzt.cn/map-images/e64869727000432330421245bd05f866.png"),
        MapInfo("map1_2", "media/packages/vanilla/maps/map1_2", "https://img.kreedzt.cn/map-images/ae34d77d5cb495ce109efdab0b0fbc9c.png"),
        MapInfo("map10", "media/packages/vanilla.desert/maps/map10", "https://img.kreedzt.cn/map-images/ba64fc5015bd07e3bad8cfedb1aef8ff.png"),
        MapInfo("map11", "media/packages/vanilla/maps/map11", "https://img.kreedzt.cn/map-images/d4490bc4e1f64440423ed507ded121ba.png"),
        MapInfo("map12", "media/packages/vanilla.winter/maps/map12", "https://img.kreedzt.cn/map-images/0e40ea59b40b7fe6a2046e8fe4d19ab6.png"),
        MapInfo("map13", "media/packages/vanilla/maps/map13", "https://img.kreedzt.cn/map-images/28670ebb6e1d360c4111339e6d0f61c2.png"),
        MapInfo("map13_2", "media/packages/vanilla/maps/map13_2", "https://img.kreedzt.cn/map-images/7efd942695a62991db3b375bf681e49b.png"),
        MapInfo("map14", "media/packages/vanilla/maps/map14", "https://img.kreedzt.cn/map-images/9fc1698adffcaf0064bf32107aabb147.png"),
        MapInfo("map15", "media/packages/vanilla/maps/map15", "https://img.kreedzt.cn/map-images/5617c81e8575306e2ff61652b6a4338e.png"),
        MapInfo("map16", "media/packages/vanilla/maps/map16", "https://img.kreedzt.cn/map-images/0a627c6f0875d651e3c529933066fe7f.png"),
        MapInfo("map17", "media/packages/vanilla/maps/map17", "https://img.kreedzt.cn/map-images/ae452ef9846460fa80692e45719e6edd.png"),
        MapInfo("map18", "media/packages/vanilla/maps/map18", "https://img.kreedzt.cn/map-images/22d17b0ecae61022d454619879e20c33.png"),
        MapInfo("map19", "media/packages/vanilla/maps/map19", "https://img.kreedzt.cn/map-images/7accef6d89f2025b62c2f6558a526b68.png"),
        MapInfo("map2", "media/packages/vanilla/maps/map2", "https://img.kreedzt.cn/map-images/9d68bcc75beec30c9d66af088f103cd9.png"),
        MapInfo("map20", "media/packages/vanilla/maps/map20", "https://img.kreedzt.cn/map-images/f960e299592e77255a2d0dba11f2ed85.png"),
        MapInfo("map21", "media/packages/vanilla/maps/map21", "https://img.kreedzt.cn/map-images/738aad550362a7fd587fe15c00bbf103.png"),
        MapInfo("map3", "media/packages/vanilla/maps/map3", "https://img.kreedzt.cn/map-images/fc4821e5bcf229103b6c917a0110a90b.png"),
        MapInfo("map4", "media/packages/vanilla.winter/maps/map4", "https://img.kreedzt.cn/map-images/023168c10b730e2bf1a3ff600b99d250.png"),
        MapInfo("map5", "media/packages/vanilla/maps/map5", "https://img.kreedzt.cn/map-images/a03c7c0f38af514af87b1002da15fb10.png"),
        MapInfo("map6", "media/packages/vanilla.desert/maps/map6", "https://img.kreedzt.cn/map-images/f20a5fb44099856ac80aad371402f40a.png"),
        MapInfo("map7", "media/packages/vanilla/maps/map7", "https://img.kreedzt.cn/map-images/fc07cf36c565e965cfed334a9327dbd2.png"),
        MapInfo("map8", "media/packages/vanilla/maps/map8", "https://img.kreedzt.cn/map-images/4bbab80dfa2e38a9cdd25b1bc45691e2.png"),
        MapInfo("map8_2", "media/packages/vanilla.winter/maps/map8_2", "https://img.kreedzt.cn/map-images/8b502bdfd3b2b7863ccd75886026c328.png"),
        MapInfo("map9", "media/packages/vanilla.desert/maps/map9", "https://img.kreedzt.cn/map-images/b852330f999a0ce2b49beb1840564a47.png"),
        MapInfo("race1", "media/packages/hell_diver/maps/race1", "https://img.kreedzt.cn/map-images/d9f9909ee8d9d79d0bd79a2be1acc22c.png"),
        MapInfo("fob", "media/packages/GFLNP_INF/maps/fob", "https://img.kreedzt.cn/map-images/default-map.png")
    )

    private val mockServers = listOf(
        GameServer(
            id = "0",
            name = "InvasionASIA7",
            ipAddress = "103.161.224.194",
            port = 1236,
            mapId = "media/packages/vanilla/maps/map3",
            mapName = "",
            bots = 114,
            country = "Asia",
            currentPlayers = 22,
            timeStamp = 1765361881,
            version = "1.98.1",
            dedicated = true,
            mod = false,
            playerList = listOf("AZLIN", "MR. JET", "MR. LBB", "ANWUFAI", "F-01-69", "STONE2333", "DNF6516", "SHUBAO", "MR. GREENHUSAU", "AWAYG", "LWZJ-WWN", "MR.NOHAIR", "WANG STEVE", "MICHAELZHANG", "SOLITARY DREAM", "MRSSR", "SMPAOHUIREN", "EILIN", "LANSL", "ZHUZHU233", "MR. LUOZ", "GREEN 10"),
            comment = "Coop campaign",
            url = "",
            maxPlayers = 32,
            mode = "COOP",
            realm = "official_invasion"
        ),
        GameServer(
            id = "1",
            name = "InvasionJP1",
            ipAddress = "45.32.63.85",
            port = 1234,
            mapId = "media/packages/vanilla.desert/maps/map10",
            mapName = "",
            bots = 159,
            country = "Japan",
            currentPlayers = 2,
            timeStamp = 1765361890,
            version = "1.98.1",
            dedicated = true,
            mod = false,
            playerList = listOf("FCC", "LIUCCCC"),
            comment = "Coop campaign",
            url = "",
            maxPlayers = 32,
            mode = "COOP",
            realm = "official_invasion"
        ),
        GameServer(
            id = "2",
            name = "InvasionUS1",
            ipAddress = "162.248.88.126",
            port = 1236,
            mapId = "media/packages/vanilla/maps/map8",
            mapName = "",
            bots = 36,
            country = "USA, Chicago",
            currentPlayers = 3,
            timeStamp = 1765361815,
            version = "1.98.1",
            dedicated = true,
            mod = false,
            playerList = listOf("-MAN BA-", "JAX6", "HAYK"),
            comment = "Coop campaign",
            url = "",
            maxPlayers = 32,
            mode = "COOP",
            realm = "official_invasion"
        ),
        GameServer(
            id = "3",
            name = "[Castling][GFL-5 LV4]",
            ipAddress = "121.4.91.113",
            port = 1234,
            mapId = "media/packages/GFL_Castling/maps/map16",
            mapName = "",
            bots = 450,
            country = "China",
            currentPlayers = 2,
            timeStamp = 1765361708,
            version = "1.98.1",
            dedicated = true,
            mod = true,
            playerList = listOf("KAIDILAKE", "7W523"),
            comment = "Read server rules in our discord: discord.gg/wwUM3kYmRC, QQ Group: 706234535",
            url = "https://castling.fandom.com/wiki/Castling_Wiki",
            maxPlayers = 15,
            mode = "Castling",
            realm = null
        ),
        GameServer(
            id = "4",
            name = "EGOTRIP",
            ipAddress = "94.143.50.131",
            port = 1234,
            mapId = "media/packages/pacific/maps/island8",
            mapName = "",
            bots = 593,
            country = "Not set",
            currentPlayers = 2,
            timeStamp = 1765361831,
            version = "1.98.1",
            dedicated = false,
            mod = false,
            playerList = listOf("EGOTRIP", "EDUARD ERMOLAEV"),
            comment = "",
            url = "",
            maxPlayers = 2,
            mode = "",
            realm = null
        ),
        GameServer(
            id = "5",
            name = "[Castling][GFL-6 LV4]",
            ipAddress = "101.43.0.212",
            port = 1234,
            mapId = "media/packages/GFL_Castling/maps/map21",
            mapName = "",
            bots = 382,
            country = "China",
            currentPlayers = 1,
            timeStamp = 1765361789,
            version = "1.98.1",
            dedicated = true,
            mod = true,
            playerList = listOf("QWQNEKO"),
            comment = "Read server rules in our discord: discord.gg/wwUM3kYmRC, QQ Group: 706234535",
            url = "https://castling.fandom.com/wiki/Castling_Wiki",
            maxPlayers = 10,
            mode = "Castling",
            realm = null
        )
    )

    fun findMapImage(mapId: String): String? {
        return mockMaps.find { it.path == mapId }?.image
    }

    fun getMapName(mapId: String): String {
        return mockMaps.find { it.path == mapId }?.name ?: "Unknown Map"
    }

    suspend fun getServers(): List<GameServer> = withContext(Dispatchers.IO) {
        try {
            // 尝试从网络获取服务器数据
            val serversFromApi = apiService.getServers()
            // 为每个服务器匹配地图图片
            serversFromApi.map { server ->
                server.copy(
                    mapImage = findMapImage(server.mapId),
                    mapName = getMapName(server.mapId)
                )
            }
        } catch (e: Exception) {
            Log.e("ServerRepository", "Failed to fetch servers from API, using mock data", e)
            // 网络请求失败时使用模拟数据
            mockServers.map { server ->
                server.copy(
                    mapImage = findMapImage(server.mapId),
                    mapName = getMapName(server.mapId)
                )
            }
        }
    }

    suspend fun searchServers(query: String): List<GameServer> = withContext(Dispatchers.IO) {
        if (query.isEmpty()) {
            return@withContext getServers()
        }

        val searchTerms = query.trim().lowercase().split("\\s+".toRegex())
        getServers().filter { server ->
            searchTerms.all { term ->
                server.name.contains(term, ignoreCase = true) ||
                server.comment.contains(term, ignoreCase = true) ||
                server.mode.contains(term, ignoreCase = true) ||
                server.mapName.contains(term, ignoreCase = true) ||
                server.serverLocation.contains(term, ignoreCase = true)
            }
        }
    }

    // 刷新服务器数据
    suspend fun refreshServers(): Result<List<GameServer>> = withContext(Dispatchers.IO) {
        try {
            val servers = apiService.getServers()
            val serversWithMaps = servers.map { server ->
                server.copy(
                    mapImage = findMapImage(server.mapId),
                    mapName = getMapName(server.mapId)
                )
            }
            Result.success(serversWithMaps)
        } catch (e: Exception) {
            Log.e("ServerRepository", "Failed to refresh servers", e)
            Result.failure(e)
        }
    }
}
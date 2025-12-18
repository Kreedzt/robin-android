package com.kreedzt.robin.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.Response as OkHttpResponse
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.kreedzt.robin.data.ServerListXmlParser

interface GameApiService {
    @GET("api/server_list")
    suspend fun getServerList(
        @Query("start") start: Int = 0,
        @Query("size") size: Int = 100,
        @Query("names") names: Int = 1
    ): Response<String>

    @GET("api/maps")
    suspend fun getMaps(): Response<String>
}

class ServerRepository(private val settingsManager: SettingsManager, private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .apply {
            if (ApiConfig.ENABLE_LOGGING) {
                addInterceptor(HttpLoggingInterceptor { message ->
                    Log.d("ServerRepository", message)
                }.apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
            addInterceptor(AcceptImageInterceptor())
        }
        .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .build()

    // 动态获取API基础URL
    private val baseUrl: String
        get() = settingsManager.apiBaseUrl

    // 动态创建Retrofit实例
    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

    private val apiService: GameApiService
        get() = retrofit.create(GameApiService::class.java)

    // 服务器数据缓存
    private var cachedServers: List<GameServer> = emptyList()
    private var serversLastUpdated: Long = 0
    private val SERVERS_CACHE_DURATION = 5 * 1000L // 5秒缓存（仅用于记录时间戳，不再作为返回缓存的条件）

    // 内存中的地图数据缓存（会话级别），实际数据由全局共享
    private var cachedMaps: List<MapInfo> = emptyList()
    private var mapsLastUpdated: Long = 0

    // SharedPreferences 用于持久化地图数据缓存
    private val mapsCachePrefs: SharedPreferences = context.getSharedPreferences("maps_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    // 地图数据持久化缓存配置
    private val PERSISTENT_CACHE_DURATION = 24 * 60 * 60 * 1000L // 24小时持久化缓存
    private val KEY_MAPS_DATA = "maps_data"
    private val KEY_MAPS_TIMESTAMP = "maps_timestamp"

    /**
     * 从持久化缓存加载地图数据
     */
    private fun loadMapsFromPersistentCache(): List<MapInfo>? {
        return try {
            val timestamp = mapsCachePrefs.getLong(KEY_MAPS_TIMESTAMP, 0)
            val currentTime = System.currentTimeMillis()

            // 检查缓存是否过期
            if (timestamp > 0 && (currentTime - timestamp) < PERSISTENT_CACHE_DURATION) {
                val mapsJson = mapsCachePrefs.getString(KEY_MAPS_DATA, null)
                if (mapsJson != null) {
                    Log.d("ServerRepository", "Loading maps from persistent cache")
                    val mapDataArray = gson.fromJson(mapsJson, Array<MapInfo>::class.java)
                    mapDataArray?.toList()
                } else null
            } else null
        } catch (e: Exception) {
            Log.e("ServerRepository", "Error loading maps from persistent cache: ${e.message}", e)
            null
        }
    }

    /**
     * 将地图数据保存到持久化缓存
     */
    private fun saveMapsToPersistentCache(maps: List<MapInfo>) {
        try {
            val mapsJson = gson.toJson(maps)
            mapsCachePrefs.edit()
                .putString(KEY_MAPS_DATA, mapsJson)
                .putLong(KEY_MAPS_TIMESTAMP, System.currentTimeMillis())
                .apply()
            Log.d("ServerRepository", "Saved ${maps.size} maps to persistent cache")
        } catch (e: Exception) {
            Log.e("ServerRepository", "Error saving maps to persistent cache: ${e.message}", e)
        }
    }

    /**
     * 获取地图数据（前台激活/启动都会触发一次刷新机会）
     * - 优先返回内存/持久化缓存
     * - 每次前台激活尝试一次网络刷新，失败则保留上一份成功数据
     */
    private suspend fun getMaps(): List<MapInfo> = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()

        // 读取全局缓存
        synchronized(mapsLock) {
            if (globalMapsCache.isNotEmpty()) {
                cachedMaps = globalMapsCache
                mapsLastUpdated = currentTime
            }
        }
        if (cachedMaps.isNotEmpty()) {
            Log.d("ServerRepository", "Using in-memory maps cache")
            return@withContext cachedMaps
        }

        // 持久化兜底
        val persistentMaps = loadMapsFromPersistentCache()
        if (persistentMaps != null) {
            Log.d("ServerRepository", "Using persistent cached maps data")
            updateGlobalMapsCache(persistentMaps, currentTime)
            return@withContext persistentMaps
        }

        // 前台激活时允许的网络刷新
        if (shouldFetchMapsThisForeground()) {
            val fetched = fetchMapsFromApi(currentTime)
            if (fetched != null) {
                return@withContext fetched
            }
        }

        // 回退已有缓存或空
        synchronized(mapsLock) {
            if (globalMapsCache.isNotEmpty()) {
                cachedMaps = globalMapsCache
                mapsLastUpdated = currentTime
            }
        }
        if (cachedMaps.isNotEmpty()) {
            return@withContext cachedMaps
        }

        Log.w("ServerRepository", "No maps available, returning empty list")
        return@withContext emptyList()
    }

    private fun shouldFetchMapsThisForeground(): Boolean = synchronized(mapsLock) {
        if (!hasFetchPendingThisForeground) return false
        hasFetchPendingThisForeground = false
        true
    }

    private fun updateGlobalMapsCache(maps: List<MapInfo>, timestamp: Long) {
        synchronized(mapsLock) {
            globalMapsCache = maps
            globalMapsUpdatedAt = timestamp
        }
        cachedMaps = maps
        mapsLastUpdated = timestamp
        saveMapsToPersistentCache(maps)
    }

    private suspend fun fetchMapsFromApi(currentTime: Long): List<MapInfo>? {
        return try {
            Log.i("ServerRepository", "Fetching maps from API (once per foreground)")
            val response = apiService.getMaps()

            if (response.isSuccessful) {
                val jsonData = response.body() ?: ""
                if (jsonData.isNotEmpty()) {
                    val parsedMaps = MapsJsonParser.parseMapsFromString(jsonData)
                    if (parsedMaps.isNotEmpty()) {
                        updateGlobalMapsCache(parsedMaps, currentTime)
                        Log.i("ServerRepository", "Fetched ${parsedMaps.size} maps from API")
                        return parsedMaps
                    }
                }
            } else {
                Log.w("ServerRepository", "Failed to fetch maps: HTTP ${response.code()}")
            }
            null
        } catch (e: Exception) {
            Log.e("ServerRepository", "Error fetching maps from API: ${e.message}", e)
            null
        }
    }

    suspend fun findMapImage(mapId: String): String? {
        val maps = getMaps()
        return maps.find { it.path == mapId }?.image
    }

    suspend fun getMapName(mapId: String): String {
        val maps = getMaps()
        return maps.find { it.path == mapId }?.name ?: "Unknown Map"
    }

    suspend fun getServers(forceRefresh: Boolean = false): List<GameServer> = withContext(Dispatchers.IO) {
        // 始终尝试拉取最新数据；失败时再回落到缓存（逻辑在 fetchAllServers 内处理）
        return@withContext fetchAllServers(forceRefresh = forceRefresh)
    }

    /**
     * 强制刷新服务器数据（忽略缓存）
     */
    suspend fun refreshServers(): List<GameServer> = withContext(Dispatchers.IO) {
        // 手动刷新也允许在失败时使用缓存
        return@withContext fetchAllServers(forceRefresh = false)
    }

    /**
     * 获取所有服务器数据，类似TypeScript中的listAll函数
     * 一次性获取所有数据，前端处理分页
     * @param forceRefresh 是否强制刷新（忽略失败时的缓存数据）
     */
    private suspend fun fetchAllServers(forceRefresh: Boolean = false): List<GameServer> {
        val totalServerList = mutableListOf<GameServer>()
        val batchSize = 100
        val maxBatches = 10 // 防止无限循环
        var start = 0
        var hasMoreData = true
        var batchCount = 0
        var lastError: Exception? = null

        try {
            Log.i("ServerRepository", "Starting to fetch all servers")

            while (hasMoreData && batchCount < maxBatches) {
                batchCount++

                try {
                    Log.i("ServerRepository", "Fetching batch $batchCount, starting at index $start")

                    val response = apiService.getServerList(
                        start = start,
                        size = batchSize,
                        names = 1
                    )

                    if (response.isSuccessful) {
                        val xmlData = response.body() ?: ""
                        if (xmlData.isEmpty()) {
                            Log.i("ServerRepository", "Batch $batchCount returned empty response, stopping")
                            hasMoreData = false
                            break
                        }

                        val parsedServers = ServerListXmlParser.parseServerListFromString(xmlData)

                        if (parsedServers.isEmpty()) {
                            Log.i("ServerRepository", "Batch $batchCount returned no servers, stopping")
                            hasMoreData = false
                        } else {
                            Log.i("ServerRepository", "Batch $batchCount returned ${parsedServers.size} servers")

                            // 为每个服务器匹配地图图片
                            val serversWithMaps = parsedServers.map { server ->
                                server.copy(
                                    mapImage = findMapImage(server.mapId),
                                    mapName = getMapName(server.mapId)
                                )
                            }

                            totalServerList.addAll(serversWithMaps)
                            start += batchSize

                            // 如果返回的数据少于请求的数量，说明已经到达末尾
                            if (parsedServers.size < batchSize) {
                                Log.i("ServerRepository", "Batch $batchCount returned fewer than $batchSize servers, stopping")
                                hasMoreData = false
                            }
                        }
                    } else {
                        lastError = Exception("HTTP ${response.code()}: ${response.message()}")
                        Log.e("ServerRepository", "Batch $batchCount failed: ${lastError.message ?: "Unknown error"}")
                        hasMoreData = false
                    }
                } catch (e: Exception) {
                    lastError = e
                    Log.e("ServerRepository", "Error in batch $batchCount: ${e.message}", e)
                    hasMoreData = false
                }
            }

            Log.i("ServerRepository", "Total servers fetched: ${totalServerList.size}")

            // 如果成功获取到数据，更新缓存
            if (totalServerList.isNotEmpty()) {
                cachedServers = totalServerList
                serversLastUpdated = System.currentTimeMillis()
                Log.i("ServerRepository", "Updated servers cache with ${totalServerList.size} servers")
                return totalServerList
            }

            // 如果没有获取到新数据，根据forceRefresh参数决定策略
            if (cachedServers.isNotEmpty() && !forceRefresh) {
                Log.w("ServerRepository", "API failed but using cached servers (${cachedServers.size} servers). Error: ${lastError?.message}")
                return cachedServers
            }

            // 如果是强制刷新或者没有缓存数据，返回空列表
            Log.w("ServerRepository", "No servers available and no cache, returning empty list. Error: ${lastError?.message}")
            return emptyList()
        } catch (e: Exception) {
            Log.e("ServerRepository", "Critical error in fetchAllServers: ${e.message}", e)

            // 发生严重错误时的回退策略
            if (cachedServers.isNotEmpty() && !forceRefresh) {
                Log.w("ServerRepository", "Critical error but returning cached servers (${cachedServers.size} servers)")
                return cachedServers
            }

            // 最后的回退选项：返回空列表
            return emptyList()
        }
    }

    suspend fun searchServers(query: String): List<GameServer> = withContext(Dispatchers.IO) {
        if (query.isEmpty()) {
            return@withContext getServers()
        }

        val searchTerms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }

        if (searchTerms.isEmpty()) {
            return@withContext getServers()
        }

        getServers().filter { server ->
            searchTerms.any { term ->
                val lowerTerm = term.lowercase()

                // 搜索服务器名称
                server.name.lowercase().contains(lowerTerm) ||
                // 搜索IP地址
                server.ipAddress.lowercase().contains(lowerTerm) ||
                // 搜索端口 - 精确匹配或部分匹配
                server.port.toString() == term || server.port.toString().contains(term) ||
                // 搜索机器人数量
                server.bots.toString() == term || server.bots.toString().contains(term) ||
                // 搜索国家/地区
                server.country.lowercase().contains(lowerTerm) ||
                // 搜索游戏模式
                server.mode.lowercase().contains(lowerTerm) ||
                // 搜索地图名称
                server.mapName.lowercase().contains(lowerTerm) ||
                // 搜索地图ID - 特别处理数字部分
                server.mapId.lowercase().contains(lowerTerm) ||
                extractMapNumber(server.mapId)?.contains(term) == true ||
                // 搜索当前玩家数
                server.currentPlayers.toString() == term || server.currentPlayers.toString().contains(term) ||
                // 搜索最大玩家数
                server.maxPlayers.toString() == term || server.maxPlayers.toString().contains(term) ||
                // 搜索玩家列表
                server.playerList.any { player -> player.lowercase().contains(lowerTerm) } ||
                // 搜索服务器注释/描述
                server.comment.lowercase().contains(lowerTerm) ||
                // 搜索版本
                server.version.lowercase().contains(lowerTerm) ||
                // 搜索专用服务器状态
                (if (server.dedicated) "dedicated" else "non-dedicated").contains(lowerTerm) ||
                // 搜索Mod状态
                (if (server.mod) "mod" else "vanilla").contains(lowerTerm)
            }
        }
    }

    /**
     * 从地图ID中提取数字部分
     * 例如：从 "media/packages/vanilla/maps/map3" 提取 "3"
     */
    private fun extractMapNumber(mapId: String): String? {
        return try {
            // 匹配 maps/map3, maps/map10 等格式
            val regex = Regex("maps/map(\\d+)")
            regex.find(mapId.lowercase())?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}

private class AcceptImageInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): OkHttpResponse {
        val original = chain.request()
        val acceptHeader = original.header("Accept")
        val isImage = acceptHeader?.startsWith("image/") == true ||
                original.url.encodedPath.endsWith(".png", true) ||
                original.url.encodedPath.endsWith(".jpg", true) ||
                original.url.encodedPath.endsWith(".jpeg", true) ||
                original.url.encodedPath.endsWith(".webp", true) ||
                original.url.encodedPath.endsWith(".avif", true)

        if (!isImage) {
            return chain.proceed(original)
        }

        val targetAccept = "image/avif,image/webp,*/*"
        val newReq = original.newBuilder()
            .header("Accept", targetAccept)
            .build()

        Log.d("AcceptImageInterceptor", "url=${original.url} accept='${newReq.header("Accept")}'")
        return chain.proceed(newReq)
    }
}

// 全局共享的地图缓存与刷新信号，确保“每次启动/前台激活”可触发一次网络刷新
private val mapsLock = Any()
private var globalMapsCache: List<MapInfo> = emptyList()
private var globalMapsUpdatedAt: Long = 0L
@Volatile private var hasFetchPendingThisForeground: Boolean = true // 启动时默认需要拉取

internal fun notifyMapsNeedRefreshOnForeground() {
    synchronized(mapsLock) {
        hasFetchPendingThisForeground = true
    }
}
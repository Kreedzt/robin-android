package com.kreedzt.rwr

import android.app.Application
import com.kreedzt.rwr.data.SettingsManager
import com.kreedzt.rwr.data.notifyMapsNeedRefreshOnForeground
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient
import okhttp3.Cache
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log
import java.io.File
import android.app.Activity
import android.os.Bundle

class RobinApplication : Application() {

    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()

        // 初始化设置管理器
        settingsManager = SettingsManager.getInstance(this)

        // 每次启动、回到前台时标记 maps 需要刷新一次
        notifyMapsNeedRefreshOnForeground()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                notifyMapsNeedRefreshOnForeground()
            }
            override fun onActivityResumed(activity: Activity) {
                notifyMapsNeedRefreshOnForeground()
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

        // 全局 ImageLoader，统一为图片请求添加 Accept 协商头并输出日志
        val imageLoader = ImageLoader.Builder(this)
            // 提升本地缓存：减少重复下载，缩短预览首帧时间
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 使用最多 25% 可用内存
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "coil_disk_cache"))
                    .maxSizePercent(0.02) // 占用可用磁盘的 2%
                    .build()
            }
            .allowHardware(true)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(Cache(File(cacheDir, "coil_okhttp_cache"), 50L * 1024 * 1024)) // 50MB HTTP 缓存
                    .addInterceptor { chain ->
                        val req = chain.request().newBuilder()
                            .header("Accept", "image/avif,image/webp,*/*")
                            .build()
                        Log.d("CoilAccept", "url=${req.url} accept=${req.header("Accept")}")
                        chain.proceed(req)
                    }
                    .apply {
                        // 继承已有日志级别设置；仅在需要时打开
                        val logging = HttpLoggingInterceptor { msg -> Log.d("CoilHttp", msg) }
                        logging.level = HttpLoggingInterceptor.Level.BASIC
                        addInterceptor(logging)
                    }
                    .build()
            }
            .build()
        Coil.setImageLoader(imageLoader)
    }
}
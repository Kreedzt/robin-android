package com.kreedzt.rwr

import android.app.Application
import com.kreedzt.rwr.data.SettingsManager
import coil.Coil
import coil.ImageLoader
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log

class RobinApplication : Application() {

    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()

        // 初始化设置管理器
        settingsManager = SettingsManager.getInstance(this)

        // 全局 ImageLoader，统一为图片请求添加 Accept 协商头并输出日志
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
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
package com.kreedzt.robin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import com.kreedzt.robin.data.SettingsManager
import com.kreedzt.robin.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 在设置内容之前应用保存的语言设置
        val settingsManager = SettingsManager.getInstance(this)
        AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(
                if (settingsManager.language == "zh") "zh-CN" else settingsManager.language
            )
        )

        setContent {
            App()
        }
    }
}

package com.kreedzt.rwr.ui

import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.kreedzt.rwr.data.SettingsManager
import java.util.Locale

@Composable
fun App() {
    val baseContext = LocalContext.current
    val configuration = LocalConfiguration.current
    val settingsManager = remember { SettingsManager.getInstance(baseContext) }

    // 监听语言状态变化，强制重建UI
    val currentLanguage by settingsManager.languageState
    val currentTheme by settingsManager.themeState

    // 构建对应语言的 Locale 与配置上下文
    val locale = remember(currentLanguage) {
        when (currentLanguage) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            else -> Locale.forLanguageTag(currentLanguage)
        }
    }
    val localizedContext = remember(currentLanguage, configuration) {
        val newConfig = Configuration(configuration).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setLocales(LocaleList(locale))
            } else {
                setLocale(locale)
            }
        }
        baseContext.createConfigurationContext(newConfig)
    }

    // 同步系统/应用级语言设置
    LaunchedEffect(locale) {
        settingsManager.applyLanguage(currentLanguage)
        Locale.setDefault(locale)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        val useDarkTheme = when (currentTheme) {
            SettingsManager.ThemeMode.DARK -> true
            SettingsManager.ThemeMode.LIGHT -> false
            SettingsManager.ThemeMode.SYSTEM, SettingsManager.ThemeMode.DYNAMIC -> isSystemInDarkTheme()
        }

        val supportDynamic = currentTheme == SettingsManager.ThemeMode.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val colorScheme =
            if (supportDynamic) {
                if (useDarkTheme) dynamicDarkColorScheme(localizedContext) else dynamicLightColorScheme(localizedContext)
            } else {
                if (useDarkTheme) darkColorScheme(
                    primary = Color(0xFF6750A4),
                    onPrimary = Color(0xFFEADDFF),
                    primaryContainer = Color(0xFF4F378B),
                    onPrimaryContainer = Color(0xFFEADDFF),
                    secondary = Color(0xFF625B71),
                    onSecondary = Color(0xFFE8DEF8),
                    secondaryContainer = Color(0xFF4A4458),
                    onSecondaryContainer = Color(0xFFE8DEF8),
                    background = Color(0xFF1C1B1F),
                    onBackground = Color(0xFFE6E1E5),
                    surface = Color(0xFF1C1B1F),
                    onSurface = Color(0xFFE6E1E5)
                ) else lightColorScheme(
                    primary = Color(0xFF6750A4),
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFFEADDFF),
                    onPrimaryContainer = Color(0xFF21005D),
                    secondary = Color(0xFF625B71),
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFE8DEF8),
                    onSecondaryContainer = Color(0xFF1D192B),
                    background = Color(0xFFFFFBFE),
                    onBackground = Color(0xFF1C1B1F),
                    surface = Color(0xFFFFFBFE),
                    onSurface = Color(0xFF1C1B1F)
                )
            }

        MaterialTheme(colorScheme = colorScheme) {
            key(currentLanguage) {
                val navController = rememberNavController()
                RobinNavHost(navController = navController)
            }
        }
    }
}

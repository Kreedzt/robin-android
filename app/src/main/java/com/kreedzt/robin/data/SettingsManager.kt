package com.kreedzt.robin.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf
import com.kreedzt.robin.BuildConfig
import java.util.*

/**
 * 应用设置管理器
 * 处理语言、API区域等设置
 */
class SettingsManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val PREFS_NAME = "robin_settings"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_API_REGION = "api_region"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_LAST_SEARCH_QUERY = "last_search_query"
        private const val KEY_LAST_QUICK_FILTERS = "last_quick_filters"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_QUICK_FILTER_MULTI_SELECT_MODE = "quick_filter_multi_select_mode"

        private const val DEFAULT_LANGUAGE = "en"
        val SUPPORTED_LANGUAGES = listOf("en", "zh")

        // API Regions Configuration
        // Loaded from BuildConfig (configured in build.gradle.kts)
        // Can be overridden via gradle.properties or environment variables
        val API_REGIONS: List<ApiRegionConfig> by lazy {
            ApiRegionConfig.parseFromString(BuildConfig.API_REGIONS_CONFIG)
        }
    }

    // 全局语言状态，用于触发UI重新组合
    val languageState = mutableStateOf(DEFAULT_LANGUAGE)
    val themeState = mutableStateOf(ThemeMode.SYSTEM)

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 初始化语言状态
    init {
        // 从SharedPreferences中读取保存的语言设置
        val savedLanguage = sharedPrefs.getString(KEY_LANGUAGE, Locale.getDefault().language)
            ?: Locale.getDefault().language
        languageState.value = if (SUPPORTED_LANGUAGES.contains(savedLanguage)) savedLanguage else DEFAULT_LANGUAGE

        val savedTheme = sharedPrefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        themeState.value = ThemeMode.valueOf(savedTheme ?: ThemeMode.SYSTEM.name)
    }

    // 首次使用标记
    var isFirstLaunch: Boolean
        get() = sharedPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) {
            sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
        }

    // 自动检测并设置默认值
    fun applyDefaultSettings() {
        if (isFirstLaunch) {
            // 根据系统语言自动设置
            val systemLanguage = Locale.getDefault().language
            val autoLanguage = if (systemLanguage == "zh") "zh" else "en"
            language = autoLanguage

            // 根据系统语言自动设置API区域
            val autoApiRegionId = if (systemLanguage == "zh") {
                "china"  // 默认中文用户使用中国区域
            } else {
                "global"  // 默认英文用户使用全球区域
            }
            apiRegionId = autoApiRegionId

            // 标记不是首次启动
            isFirstLaunch = false
        }
    }

    // API 区域设置 (使用区域ID字符串)
    var apiRegionId: String
        get() {
            val regionId = sharedPrefs.getString(KEY_API_REGION, null)
            // 如果未设置或无效，返回第一个可用区域的ID
            return if (regionId != null && API_REGIONS.any { it.id == regionId }) {
                regionId
            } else {
                API_REGIONS.firstOrNull()?.id ?: "global"
            }
        }
        set(value) {
            sharedPrefs.edit().putString(KEY_API_REGION, value).apply()
        }

    // 获取当前API区域配置
    val currentApiRegion: ApiRegionConfig
        get() = API_REGIONS.firstOrNull { it.id == apiRegionId }
            ?: API_REGIONS.firstOrNull()
            ?: ApiRegionConfig.DEFAULT_REGIONS.first()

    // 获取API基础URL
    val apiBaseUrl: String
        get() = currentApiRegion.url

    // 语言设置
    var language: String
        get() = sharedPrefs.getString(KEY_LANGUAGE, Locale.getDefault().language) ?: Locale.getDefault().language
        set(value) {
            sharedPrefs.edit().putString(KEY_LANGUAGE, value).apply()
            applyLanguage(value)
            // 更新全局语言状态以触发UI重新组合
            languageState.value = value
        }

    // 获取当前语言代码
    val currentLanguageCode: String
        get() = if (SUPPORTED_LANGUAGES.contains(language)) language else DEFAULT_LANGUAGE

    // 上次搜索内容
    var lastSearchQuery: String
        get() = sharedPrefs.getString(KEY_LAST_SEARCH_QUERY, "") ?: ""
        set(value) {
            sharedPrefs.edit().putString(KEY_LAST_SEARCH_QUERY, value).apply()
        }

    // 上次快捷筛选（存储ID集合）
    var lastQuickFilters: Set<String>
        get() = sharedPrefs.getStringSet(KEY_LAST_QUICK_FILTERS, emptySet()) ?: emptySet()
        set(value) {
            sharedPrefs.edit().putStringSet(KEY_LAST_QUICK_FILTERS, value.toSet()).apply()
        }

    // 快捷筛选多选模式（默认关闭，即单选模式）
    var isQuickFilterMultiSelectMode: Boolean
        get() = sharedPrefs.getBoolean(KEY_QUICK_FILTER_MULTI_SELECT_MODE, false)
        set(value) {
            sharedPrefs.edit().putBoolean(KEY_QUICK_FILTER_MULTI_SELECT_MODE, value).apply()
        }

    // 应用语言设置
    fun applyLanguage(languageCode: String) {
        // 确保使用正确的语言代码
        val localeCode = when (languageCode) {
            "zh" -> "zh-CN"  // 使用zh-CN来匹配values-zh-rCN文件夹
            else -> languageCode
        }

        // 更新默认Locale，避免在非Compose环境下获取到旧语言
        val locale = Locale.forLanguageTag(localeCode)
        Locale.setDefault(locale)

        // 对于Android 13+，使用AppCompatDelegate的Per-app language setting
        AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(localeCode)
        )
    }

    // 主题模式
    var themeMode: ThemeMode
        get() {
            val value = sharedPrefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
            return ThemeMode.valueOf(value ?: ThemeMode.SYSTEM.name)
        }
        set(value) {
            sharedPrefs.edit().putString(KEY_THEME_MODE, value.name).apply()
            themeState.value = value
        }

    enum class ThemeMode {
        SYSTEM,
        LIGHT,
        DARK,
        DYNAMIC
    }
}
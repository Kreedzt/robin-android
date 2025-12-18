package com.kreedzt.robin.data

object ApiConfig {
    // 请求超时时间（秒）
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L

    // 是否启用调试日志
    const val ENABLE_LOGGING = true

    // 搜索配置
    const val SEARCH_DEBOUNCE_DELAY = 300L  // 搜索防抖延迟（毫秒）
}
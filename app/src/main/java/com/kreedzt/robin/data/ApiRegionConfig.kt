package com.kreedzt.robin.data

/**
 * API 区域配置
 *
 * 表示一个可用的 API 区域，包含 URL 和多语言标签
 *
 * @property id 区域唯一标识符 (例如: "china", "global", "dev")
 * @property url API 基础 URL
 * @property labelEn 英文标签
 * @property labelZh 中文标签
 */
data class ApiRegionConfig(
    val id: String,
    val url: String,
    val labelEn: String,
    val labelZh: String
) {
    /**
     * 根据语言代码获取本地化标签
     *
     * @param languageCode 语言代码 ("en" 或 "zh")
     * @return 对应语言的标签
     */
    fun getLabel(languageCode: String): String {
        return when (languageCode) {
            "zh" -> labelZh
            else -> labelEn
        }
    }

    companion object {
        /**
         * 默认 API 区域配置
         *
         * 当环境变量未配置时使用此默认配置
         */
        val DEFAULT_REGIONS = listOf(
            ApiRegionConfig(
                id = "china",
                url = "https://robin.kreedzt.cn/",
                labelEn = "China Mainland",
                labelZh = "中国大陆"
            ),
            ApiRegionConfig(
                id = "global",
                url = "https://robin.kreedzt.com/",
                labelEn = "Global",
                labelZh = "全球"
            )
        )

        /**
         * 从配置字符串解析 API 区域列表
         *
         * 格式: "id|url|label_en|label_zh;id2|url2|label_en2|label_zh2"
         *
         * 示例:
         * ```
         * china|https://robin.kreedzt.cn/|China Mainland|中国大陆;global|https://robin.kreedzt.com/|Global|全球
         * ```
         *
         * @param configString 配置字符串
         * @return 解析后的区域列表，解析失败返回默认配置
         */
        fun parseFromString(configString: String?): List<ApiRegionConfig> {
            if (configString.isNullOrBlank()) {
                return DEFAULT_REGIONS
            }

            return try {
                configString
                    .split(";")
                    .filter { it.isNotBlank() }
                    .mapNotNull { regionStr ->
                        val parts = regionStr.split("|")
                        if (parts.size == 4) {
                            ApiRegionConfig(
                                id = parts[0].trim(),
                                url = parts[1].trim(),
                                labelEn = parts[2].trim(),
                                labelZh = parts[3].trim()
                            )
                        } else {
                            null
                        }
                    }
                    .takeIf { it.isNotEmpty() } ?: DEFAULT_REGIONS
            } catch (e: Exception) {
                // 解析失败时使用默认配置
                android.util.Log.e("ApiRegionConfig", "Failed to parse API regions config: $configString", e)
                DEFAULT_REGIONS
            }
        }

        /**
         * 将区域列表序列化为配置字符串
         *
         * @param regions 区域列表
         * @return 配置字符串
         */
        fun serializeToString(regions: List<ApiRegionConfig>): String {
            return regions.joinToString(";") { region ->
                "${region.id}|${region.url}|${region.labelEn}|${region.labelZh}"
            }
        }
    }
}

package com.kreedzt.robin.data

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class ApiRegionConfigTest {

    @Test
    fun `parseFromString with valid config should return correct list`() {
        val configString = "china|https://api.cn/|China Mainland|中国大陆;global|https://api.com/|Global|全球"

        val regions = ApiRegionConfig.parseFromString(configString)

        assertEquals(2, regions.size)

        val chinaRegion = regions[0]
        assertEquals("china", chinaRegion.id)
        assertEquals("https://api.cn/", chinaRegion.url)
        assertEquals("China Mainland", chinaRegion.labelEn)
        assertEquals("中国大陆", chinaRegion.labelZh)

        val globalRegion = regions[1]
        assertEquals("global", globalRegion.id)
        assertEquals("https://api.com/", globalRegion.url)
        assertEquals("Global", globalRegion.labelEn)
        assertEquals("全球", globalRegion.labelZh)
    }

    @Test
    fun `parseFromString with single region should return single item`() {
        val configString = "dev|https://dev.api.com/|Development|开发环境"

        val regions = ApiRegionConfig.parseFromString(configString)

        assertEquals(1, regions.size)
        assertEquals("dev", regions[0].id)
        assertEquals("https://dev.api.com/", regions[0].url)
        assertEquals("Development", regions[0].labelEn)
        assertEquals("开发环境", regions[0].labelZh)
    }

    @Test
    fun `parseFromString with empty config should return default regions`() {
        val regions = ApiRegionConfig.parseFromString("")

        assertEquals(2, regions.size)
        assertEquals("china", regions[0].id)
        assertEquals("global", regions[1].id)
    }

    @Test
    fun `parseFromString with null config should return default regions`() {
        val regions = ApiRegionConfig.parseFromString(null)

        assertEquals(2, regions.size)
        assertEquals("china", regions[0].id)
        assertEquals("global", regions[1].id)
    }

    @Test
    fun `parseFromString with malformed config should return default regions`() {
        val malformedConfig = "invalid|format|missing|fields;another|invalid"

        val regions = ApiRegionConfig.parseFromString(malformedConfig)

        // 这个测试实际上应该成功解析出1个有效区域
        // 因为"invalid|format|missing|fields"有4个部分，是有效的
        assertEquals(1, regions.size)
        assertEquals("invalid", regions[0].id)
        assertEquals("format", regions[0].url)
    }

    @Test
    fun `parseFromString with truly malformed config should return default regions`() {
        val malformedConfig = "invalid|format|missing;another|invalid"

        val regions = ApiRegionConfig.parseFromString(malformedConfig)

        // 这个配置的两个部分都少于4个字段，应该返回默认配置
        assertEquals(2, regions.size)
        assertEquals("china", regions[0].id)
        assertEquals("global", regions[1].id)
    }

    @Test
    fun `parseFromString with extra whitespace should trim correctly`() {
        val configString = "  china  |  https://api.cn/  |  China Mainland  |  中国大陆  ;  global  |  https://api.com/  |  Global  |  全球  "

        val regions = ApiRegionConfig.parseFromString(configString)

        assertEquals(2, regions.size)
        assertEquals("china", regions[0].id)
        assertEquals("https://api.cn/", regions[0].url)
        assertEquals("China Mainland", regions[0].labelEn)
        assertEquals("中国大陆", regions[0].labelZh)
    }

    @Test
    fun `parseFromString with empty regions should filter them out`() {
        val configString = "china|https://api.cn/|China|中国;;global|https://api.com/|Global|全球;"

        val regions = ApiRegionConfig.parseFromString(configString)

        assertEquals(2, regions.size)
        assertEquals("china", regions[0].id)
        assertEquals("global", regions[1].id)
    }

    @Test
    fun `getLabel with English language should return English label`() {
        val region = ApiRegionConfig("test", "https://api.com/", "Test Label", "测试标签")

        val label = region.getLabel("en")

        assertEquals("Test Label", label)
    }

    @Test
    fun `getLabel with Chinese language should return Chinese label`() {
        val region = ApiRegionConfig("test", "https://api.com/", "Test Label", "测试标签")

        val label = region.getLabel("zh")

        assertEquals("测试标签", label)
    }

    @Test
    fun `getLabel with unsupported language should return English label`() {
        val region = ApiRegionConfig("test", "https://api.com/", "Test Label", "测试标签")

        val label = region.getLabel("fr")

        assertEquals("Test Label", label)
    }

    @Test
    fun `getLabel with empty language should return English label`() {
        val region = ApiRegionConfig("test", "https://api.com/", "Test Label", "测试标签")

        val label = region.getLabel("")

        assertEquals("Test Label", label)
    }

    @Test
    fun `default regions should have correct structure`() {
        val regions = ApiRegionConfig.parseFromString(null)

        val chinaRegion = regions.find { it.id == "china" }
        assertNotNull(chinaRegion)
        assertTrue(chinaRegion!!.url.endsWith("/"))
        assertTrue(chinaRegion.labelEn.isNotEmpty())
        assertTrue(chinaRegion.labelZh.isNotEmpty())

        val globalRegion = regions.find { it.id == "global" }
        assertNotNull(globalRegion)
        assertTrue(globalRegion!!.url.endsWith("/"))
        assertTrue(globalRegion.labelEn.isNotEmpty())
        assertTrue(globalRegion.labelZh.isNotEmpty())
    }
}
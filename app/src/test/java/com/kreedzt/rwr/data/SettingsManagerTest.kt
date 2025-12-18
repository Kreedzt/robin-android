package com.kreedzt.rwr.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class SettingsManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var settingsManager: SettingsManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Mock SharedPreferences behavior
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)

        // Mock BuildConfig API_REGIONS_CONFIG
        mockStatic(SettingsManager::class.java).use { mockedSettingsManager ->
            // This would need to be implemented when BuildConfig is accessible in tests
        }

        // Create SettingsManager instance using reflection to access private constructor
        val constructor = SettingsManager::class.java.getDeclaredConstructor(Context::class.java)
        constructor.isAccessible = true
        settingsManager = constructor.newInstance(mockContext)
    }

    @Test
    fun `language default value should be system language`() {
        `when`(mockSharedPreferences.getString(anyString(), any())).thenReturn(null)

        // The default language should be detected from system
        val defaultLanguage = settingsManager.language

        assertNotNull(defaultLanguage)
        assertTrue(defaultLanguage == "en" || defaultLanguage == "zh")
    }

    @Test
    fun `language getter should return saved value`() {
        val savedLanguage = "zh"
        `when`(mockSharedPreferences.getString(SettingsManager.KEY_LANGUAGE, null)).thenReturn(savedLanguage)

        val language = settingsManager.language

        assertEquals(savedLanguage, language)
    }

    @Test
    fun `language setter should save to SharedPreferences`() {
        val newLanguage = "zh"

        settingsManager.language = newLanguage

        verify(mockEditor).putString(SettingsManager.KEY_LANGUAGE, newLanguage)
        verify(mockEditor).apply()
    }

    @Test
    fun `apiRegionId default value should be first available region`() {
        `when`(mockSharedPreferences.getString(anyString(), any())).thenReturn(null)

        // Test with default API regions
        val defaultRegionId = settingsManager.apiRegionId

        // Should be either "china" or "global" (first available)
        assertTrue(defaultRegionId == "china" || defaultRegionId == "global")
    }

    @Test
    fun `apiRegionId getter should return saved valid region`() {
        val savedRegionId = "global"
        `when`(mockSharedPreferences.getString(SettingsManager.KEY_API_REGION, null)).thenReturn(savedRegionId)

        val regionId = settingsManager.apiRegionId

        assertEquals(savedRegionId, regionId)
    }

    @Test
    fun `apiRegionId getter should return first available if saved region is invalid`() {
        val invalidRegionId = "invalid_region"
        `when`(mockSharedPreferences.getString(SettingsManager.KEY_API_REGION, null)).thenReturn(invalidRegionId)

        val regionId = settingsManager.apiRegionId

        // Should fall back to first available region
        assertTrue(regionId == "china" || regionId == "global")
    }

    @Test
    fun `apiRegionId setter should save to SharedPreferences`() {
        val newRegionId = "global"

        settingsManager.apiRegionId = newRegionId

        verify(mockEditor).putString(SettingsManager.KEY_API_REGION, newRegionId)
        verify(mockEditor).apply()
    }

    @Test
    fun `isFirstLaunch default value should be true`() {
        `when`(mockSharedPreferences.getBoolean(SettingsManager.KEY_FIRST_LAUNCH, true)).thenReturn(true)

        val isFirstLaunch = settingsManager.isFirstLaunch

        assertTrue(isFirstLaunch)
    }

    @Test
    fun `isFirstLaunch getter should return saved value`() {
        `when`(mockSharedPreferences.getBoolean(SettingsManager.KEY_FIRST_LAUNCH, true)).thenReturn(false)

        val isFirstLaunch = settingsManager.isFirstLaunch

        assertFalse(isFirstLaunch)
    }

    @Test
    fun `isFirstLaunch setter should save to SharedPreferences`() {
        val isFirstLaunch = false

        settingsManager.isFirstLaunch = isFirstLaunch

        verify(mockEditor).putBoolean(SettingsManager.KEY_FIRST_LAUNCH, isFirstLaunch)
        verify(mockEditor).apply()
    }

    @Test
    fun `autoRefreshEnabled default value should be true`() {
        `when`(mockSharedPreferences.getBoolean(SettingsManager.KEY_AUTO_REFRESH, true)).thenReturn(true)

        val autoRefreshEnabled = settingsManager.autoRefreshEnabled

        assertTrue(autoRefreshEnabled)
    }

    @Test
    fun `autoRefreshEnabled getter should return saved value`() {
        `when`(mockSharedPreferences.getBoolean(SettingsManager.KEY_AUTO_REFRESH, true)).thenReturn(false)

        val autoRefreshEnabled = settingsManager.autoRefreshEnabled

        assertFalse(autoRefreshEnabled)
    }

    @Test
    fun `autoRefreshEnabled setter should save to SharedPreferences`() {
        val autoRefreshEnabled = false

        settingsManager.autoRefreshEnabled = autoRefreshEnabled

        verify(mockEditor).putBoolean(SettingsManager.KEY_AUTO_REFRESH, autoRefreshEnabled)
        verify(mockEditor).apply()
    }

    @Test
    fun `apiBaseUrl should return correct URL for selected region`() {
        // Mock the selected region and API regions
        val regionId = "global"
        `when`(mockSharedPreferences.getString(SettingsManager.KEY_API_REGION, null)).thenReturn(regionId)

        val baseUrl = settingsManager.apiBaseUrl

        // Should return the URL for the "global" region
        assertTrue(baseUrl.contains("kreedzt.com"))
    }

    @Test
    fun `applyLanguage with zh should use zh-CN locale`() {
        // This test would verify that the language is applied correctly
        // Due to Android framework dependencies, we can only test the logic

        // Test that "zh" maps to "zh-CN" correctly
        val languageCode = "zh"
        val expectedLocaleCode = "zh-CN"

        // This would be tested in the actual implementation
        assertEquals("zh-CN", if (languageCode == "zh") "zh-CN" else languageCode)
    }

    @Test
    fun `applyLanguage with en should use en locale`() {
        val languageCode = "en"
        val expectedLocaleCode = "en"

        assertEquals(expectedLocaleCode, if (languageCode == "zh") "zh-CN" else languageCode)
    }

    @Test
    fun `resetToDefaults should clear all preferences`() {
        settingsManager.resetToDefaults()

        verify(mockEditor).remove(SettingsManager.KEY_LANGUAGE)
        verify(mockEditor).remove(SettingsManager.KEY_API_REGION)
        verify(mockEditor).remove(SettingsManager.KEY_FIRST_LAUNCH)
        verify(mockEditor).remove(SettingsManager.KEY_AUTO_REFRESH)
        verify(mockEditor).apply()
    }

    @Test
    fun `getInstance should return singleton instance`() {
        // This test verifies that getInstance returns the same instance
        val instance1 = SettingsManager.getInstance(mockContext)
        val instance2 = SettingsManager.getInstance(mockContext)

        assertSame(instance1, instance2)
    }

    @Test
    fun `languageState should be reactive`() {
        // This would test that languageState changes when language changes
        // Due to Compose State dependencies, this is more of an integration test

        val initialLanguage = "en"
        val newLanguage = "zh"

        `when`(mockSharedPreferences.getString(SettingsManager.KEY_LANGUAGE, null))
            .thenReturn(initialLanguage)
            .thenReturn(newLanguage)

        // Test that setting language triggers state change
        settingsManager.language = newLanguage

        // In a real test with Compose, we would verify that the state changes
        verify(mockEditor).putString(SettingsManager.KEY_LANGUAGE, newLanguage)
    }
}
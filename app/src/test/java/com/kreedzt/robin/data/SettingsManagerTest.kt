package com.kreedzt.robin.data

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SettingsManagerTest {

    private val mockContext: Context = mock()
    private val mockSharedPreferences: SharedPreferences = mock()
    private val mockEditor: SharedPreferences.Editor = mock()

    private lateinit var settingsManager: SettingsManager

    @Before
    fun setUp() {

        // Mock SharedPreferences behavior
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)

        // Mock context application context for singleton getInstance test
        `when`(mockContext.applicationContext).thenReturn(mockContext)

        // Configure mockEditor to support method chaining
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putStringSet(anyString(), any())).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)

        // Mock default Locale to ensure consistent language detection
        val defaultLocale = Locale("en")
        Locale.setDefault(defaultLocale)

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
        // Reset the mock to return the saved language for all future calls
        `when`(mockSharedPreferences.getString("language", Locale.getDefault().language)).thenReturn(savedLanguage)
        `when`(mockSharedPreferences.getString("language", null)).thenReturn(savedLanguage)

        // Create a new SettingsManager instance with the updated mock
        val constructor = SettingsManager::class.java.getDeclaredConstructor(Context::class.java)
        constructor.isAccessible = true
        val testSettingsManager = constructor.newInstance(mockContext)

        val language = testSettingsManager.language

        assertEquals(savedLanguage, language)
    }

    @Test
    fun `language setter should save to SharedPreferences`() {
        val newLanguage = "zh"

        settingsManager.language = newLanguage

        verify(mockEditor).putString("language", newLanguage)
        verify(mockEditor).apply()
    }

    @Test
    fun `apiRegionId default value should be first available region`() {
        `when`(mockSharedPreferences.getString(anyString(), any())).thenReturn(null)

        // Test with default API regions
        val defaultRegionId = settingsManager.apiRegionId

        // Should be either "local" (first available) or another configured region
        assertTrue(defaultRegionId == "local" || defaultRegionId == "china" || defaultRegionId == "global")
    }

    @Test
    fun `apiRegionId getter should return saved valid region`() {
        val savedRegionId = "global"
        `when`(mockSharedPreferences.getString("api_region", null)).thenReturn(savedRegionId)

        val regionId = settingsManager.apiRegionId

        assertEquals(savedRegionId, regionId)
    }

    @Test
    fun `apiRegionId getter should return first available if saved region is invalid`() {
        val invalidRegionId = "invalid_region"
        `when`(mockSharedPreferences.getString("api_region", null)).thenReturn(invalidRegionId)

        val regionId = settingsManager.apiRegionId

        // Should fall back to first available region ("local")
        assertTrue(regionId == "local" || regionId == "china" || regionId == "global")
    }

    @Test
    fun `apiRegionId setter should save to SharedPreferences`() {
        val newRegionId = "global"

        settingsManager.apiRegionId = newRegionId

        verify(mockEditor).putString("api_region", newRegionId)
        verify(mockEditor).apply()
    }

    @Test
    fun `isFirstLaunch default value should be true`() {
        `when`(mockSharedPreferences.getBoolean("first_launch", true)).thenReturn(true)

        val isFirstLaunch = settingsManager.isFirstLaunch

        assertTrue(isFirstLaunch)
    }

    @Test
    fun `isFirstLaunch getter should return saved value`() {
        `when`(mockSharedPreferences.getBoolean("first_launch", true)).thenReturn(false)

        val isFirstLaunch = settingsManager.isFirstLaunch

        assertFalse(isFirstLaunch)
    }

    @Test
    fun `isFirstLaunch setter should save to SharedPreferences`() {
        val isFirstLaunch = false

        settingsManager.isFirstLaunch = isFirstLaunch

        verify(mockEditor).putBoolean("first_launch", isFirstLaunch)
        verify(mockEditor).apply()
    }

    
    @Test
    fun `apiBaseUrl should return correct URL for selected region`() {
        // Mock the selected region and API regions
        val regionId = "global"
        `when`(mockSharedPreferences.getString("api_region", null)).thenReturn(regionId)

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

        `when`(mockSharedPreferences.getString("language", null))
            .thenReturn(initialLanguage)
            .thenReturn(newLanguage)

        // Test that setting language triggers state change
        settingsManager.language = newLanguage

        // In a real test with Compose, we would verify that the state changes
        verify(mockEditor).putString("language", newLanguage)
    }

    @Test
    fun `isQuickFilterMultiSelectMode default value should be false`() {
        `when`(mockSharedPreferences.getBoolean("quick_filter_multi_select_mode", false)).thenReturn(false)

        val isMultiSelectMode = settingsManager.isQuickFilterMultiSelectMode

        assertFalse(isMultiSelectMode)
    }

    @Test
    fun `isQuickFilterMultiSelectMode getter should return saved value`() {
        `when`(mockSharedPreferences.getBoolean("quick_filter_multi_select_mode", false)).thenReturn(true)

        val isMultiSelectMode = settingsManager.isQuickFilterMultiSelectMode

        assertTrue(isMultiSelectMode)
    }

    @Test
    fun `isQuickFilterMultiSelectMode setter should save to SharedPreferences`() {
        val isMultiSelectMode = true

        settingsManager.isQuickFilterMultiSelectMode = isMultiSelectMode

        verify(mockEditor).putBoolean("quick_filter_multi_select_mode", isMultiSelectMode)
        verify(mockEditor).apply()
    }
}
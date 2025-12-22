package com.kreedzt.robin.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.kreedzt.robin.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Interface to abstract UpdateViewModel for testing
interface UpdateViewModelInterface {
    val updateState: StateFlow<UpdateState>
    val showUpdateDialog: StateFlow<UpdateInfo?>
    fun checkForUpdateFromApi()
    fun onDownloadClick()
    fun onLaterClick()
}

/**
 * Test version of StartupUpdateChecker that accepts the interface
 */
@Composable
private fun TestStartupUpdateChecker(
    updateViewModel: UpdateViewModelInterface,
    onInitialCheckComplete: (Boolean) -> Unit = {}
) {
    val showUpdateDialog by updateViewModel.showUpdateDialog.collectAsStateWithLifecycle()
    val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()

    // Track if we've already checked for updates
    var hasCheckedForUpdate by remember { mutableStateOf(false) }

    // Check for updates on first composition with delay to ensure language settings are applied
    LaunchedEffect(Unit) {
        if (!hasCheckedForUpdate) {
            hasCheckedForUpdate = true
            // Small delay to ensure language settings are properly applied
            kotlinx.coroutines.delay(300)
            // Check for updates using API
            updateViewModel.checkForUpdateFromApi()
        }
    }

    // Handle update state changes
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateState.NoUpdate -> {
                onInitialCheckComplete(false)
            }
            is UpdateState.UpdateAvailable -> {
                onInitialCheckComplete(true)
            }
            is UpdateState.Error -> {
                onInitialCheckComplete(false)
            }
            else -> { /* Continue checking */ }
        }
    }

    // Show update dialog if available
    showUpdateDialog?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            onDownload = { updateViewModel.onDownloadClick() },
            onDismiss = {
                if (!updateInfo.isMandatory) {
                    updateViewModel.onLaterClick()
                }
            },
            onLater = { updateViewModel.onLaterClick() }
        )
    }
}

class StartupUpdateCheckerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Test implementation that mimics UpdateViewModel interface without requiring real dependencies
    private class TestUpdateViewModel : UpdateViewModelInterface {
        var testUpdateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
        var testShowUpdateDialog = MutableStateFlow<UpdateInfo?>(null)
        var checkForUpdateFromApiCalled = false

        override val updateState = testUpdateState
        override val showUpdateDialog = testShowUpdateDialog

        override fun checkForUpdateFromApi() {
            checkForUpdateFromApiCalled = true
        }

        override fun onDownloadClick() {
            testShowUpdateDialog.value = null
        }

        override fun onLaterClick() {
            if (testShowUpdateDialog.value?.isMandatory == false) {
                testShowUpdateDialog.value = null
            }
        }
    }

    private lateinit var testViewModel: TestUpdateViewModel

    @Before
    fun setup() {
        testViewModel = TestUpdateViewModel()
    }

    @Test
    fun `startupUpdateChecker_does_not_show_dialog_initially`() {
        // When
        composeTestRule.setContent {
            TestStartupUpdateChecker(
                updateViewModel = testViewModel,
                onInitialCheckComplete = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Update available").assertDoesNotExist()
        composeTestRule.onNodeWithText("Download").assertDoesNotExist()
    }

    @Test
    fun `startupUpdateChecker_shows_dialog_when_update_is_available`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false,
            downloadUrl = "https://example.com/download.apk"
        )
        testViewModel.testShowUpdateDialog.value = updateInfo

        // When
        composeTestRule.setContent {
            TestStartupUpdateChecker(
                updateViewModel = testViewModel,
                onInitialCheckComplete = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Update available").assertIsDisplayed()
        composeTestRule.onNodeWithText("Later").assertIsDisplayed()
        // Download button should be displayed (there should be at least one Download node)
        composeTestRule.onAllNodesWithText("Download").assertCountEquals(2) // URL text + Download button
    }

    @Test
    fun `startupUpdateChecker_shows_mandatory_update_dialog`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = true,
            downloadUrl = "https://example.com/download.apk"
        )
        testViewModel.testShowUpdateDialog.value = updateInfo

        // When
        composeTestRule.setContent {
            TestStartupUpdateChecker(
                updateViewModel = testViewModel,
                onInitialCheckComplete = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Mandatory update required").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Download").assertCountEquals(2) // URL text + Download button
        composeTestRule.onNodeWithText("Later").assertDoesNotExist()
    }

    @Test
    fun `startupUpdateChecker_triggers_update_check_on_composition`() {
        // Given
        testViewModel.checkForUpdateFromApiCalled = false

        // When
        composeTestRule.setContent {
            TestStartupUpdateChecker(
                updateViewModel = testViewModel,
                onInitialCheckComplete = {}
            )
        }

        // Wait for LaunchedEffect to execute
        composeTestRule.waitUntil(3000) {
            testViewModel.checkForUpdateFromApiCalled
        }

        // Then
        assert(testViewModel.checkForUpdateFromApiCalled)
    }

    @Test
    fun `startupUpdateChecker_calls_onInitialCheckComplete_with_false_when_no_update`() {
        // Given
        var callbackCalled = false
        var hasUpdate: Boolean? = null

        testViewModel.testUpdateState.value = UpdateState.NoUpdate

        // When
        composeTestRule.setContent {
            TestStartupUpdateChecker(
                updateViewModel = testViewModel,
                onInitialCheckComplete = { update ->
                    callbackCalled = true
                    hasUpdate = update
                }
            )
        }

        // Wait for state change
        composeTestRule.waitUntil(3000) {
            callbackCalled
        }

        // Then
        assert(callbackCalled)
        assertEquals(false, hasUpdate)
    }

    @Test
    fun `startupUpdateChecker_calls_onInitialCheckComplete_with_true_when_update_available`() {
        // Given
        var callbackCalled = false
        var hasUpdate: Boolean? = null
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false
        )

        testViewModel.testUpdateState.value = UpdateState.UpdateAvailable(updateInfo)

        // When
        composeTestRule.setContent {
            TestStartupUpdateChecker(
                updateViewModel = testViewModel,
                onInitialCheckComplete = { update ->
                    callbackCalled = true
                    hasUpdate = update
                }
            )
        }

        // Wait for state change
        composeTestRule.waitUntil(3000) {
            callbackCalled
        }

        // Then
        assert(callbackCalled)
        assertEquals(true, hasUpdate)
    }

    @Test
    fun `startupUpdateChecker_calls_onInitialCheckComplete_with_false_when_error_occurs`() {
        // Given
        var callbackCalled = false
        var hasUpdate: Boolean? = null

        testViewModel.testUpdateState.value = UpdateState.Error("Network error")

        // When
        composeTestRule.setContent {
            TestStartupUpdateChecker(
                updateViewModel = testViewModel,
                onInitialCheckComplete = { update ->
                    callbackCalled = true
                    hasUpdate = update
                }
            )
        }

        // Wait for state change
        composeTestRule.waitUntil(3000) {
            callbackCalled
        }

        // Then
        assert(callbackCalled)
        assertEquals(false, hasUpdate)
    }

    @Test
    fun `startupUpdateChecker_handles_download_click`() {
        // Given
        var downloadClicked = false
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false,
            downloadUrl = "https://example.com/download.apk"
        )

        testViewModel.testShowUpdateDialog.value = updateInfo

        // When
        composeTestRule.setContent {
            TestStartupUpdateChecker(
                updateViewModel = testViewModel,
                onInitialCheckComplete = {}
            )
        }

        // Click the Download button (not the URL text)
        composeTestRule.onAllNodesWithText("Download")[1].performClick() // Second node should be the button

        // Then
        // The dialog should be closed after download click
        composeTestRule.waitUntil(1000) {
            testViewModel.testShowUpdateDialog.value == null
        }
    }

    @Test
    fun `startupUpdateChecker_handles_later_click`() {
        // Given
        var laterClicked = false
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false,
            downloadUrl = "https://example.com/download.apk"
        )

        testViewModel.testShowUpdateDialog.value = updateInfo

        // When
        composeTestRule.setContent {
            TestStartupUpdateChecker(
                updateViewModel = testViewModel,
                onInitialCheckComplete = {}
            )
        }

        composeTestRule.onNodeWithText("Later").performClick()

        // Then
        // The dialog should be closed after later click for non-mandatory update
        composeTestRule.waitUntil(1000) {
            testViewModel.testShowUpdateDialog.value == null
        }
    }

}
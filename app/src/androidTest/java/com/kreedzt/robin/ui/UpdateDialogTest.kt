package com.kreedzt.robin.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.input.key.Key
import com.kreedzt.robin.data.UpdateInfo
import com.kreedzt.robin.data.VersionInfo
import org.junit.Rule
import org.junit.Test

class UpdateDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `updateDialog_does_not_show_when_update_is_not_available`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            isUpdateAvailable = false
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Update available").assertDoesNotExist()
    }

    @Test
    fun `updateDialog_shows_correct_title_for_optional_update`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Update available").assertIsDisplayed()
    }

    @Test
    fun `updateDialog_shows_correct_title_for_mandatory_update`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = true
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Mandatory update required").assertIsDisplayed()
    }

    @Test
    fun `updateDialog_shows_update_message_with_correct_versions`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Update available: 1.0.0 → 1.1.0").assertIsDisplayed()
    }

    @Test
    fun `updateDialog_shows_mandatory_update_message`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = true
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("A mandatory update is required (1.0.0 → 1.1.0)").assertIsDisplayed()
    }

    @Test
    fun `updateDialog_shows_download_URL_when_available`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            downloadUrl = "https://example.com/download.apk"
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        // Download text should appear in content when URL is available (in addition to button)
        // So we expect 2 "Download" nodes: one in content, one as button
        composeTestRule.onAllNodesWithText("Download").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("Download")[0].assertIsDisplayed() // First node (content text)
        composeTestRule.onAllNodesWithText("Download")[1].assertIsDisplayed() // Second node (button)
    }

    @Test
    fun `updateDialog_does_not_show_download_URL_when_not_available`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            downloadUrl = null
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        // Download text should NOT appear in content when URL is null
        // But Download button should still exist
        // So we expect only 1 "Download" node: the button
        composeTestRule.onAllNodesWithText("Download").assertCountEquals(1) // Only the button
        composeTestRule.onNodeWithText("Download").assertExists() // Download button exists
    }

    @Test
    fun `updateDialog_shows_changelog_when_available`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            changelog = "- Fixed bugs\n- Added new features"
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("- Fixed bugs\n- Added new features").assertIsDisplayed()
    }

    @Test
    fun `updateDialog_shows_release_notes_when_available`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            releaseNotes = "This release includes important security updates."
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("This release includes important security updates.").assertIsDisplayed()
    }

    @Test
    fun `updateDialog_shows_both_buttons_for_non_mandatory_update`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Download").assertIsDisplayed()
        composeTestRule.onNodeWithText("Later").assertIsDisplayed()
    }

    @Test
    fun `updateDialog_shows_only_download_button_for_mandatory_update`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = true
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Download").assertIsDisplayed()
        composeTestRule.onNodeWithText("Later").assertDoesNotExist()
    }

    @Test
    fun `clicking_download_button_triggers_onDownload_callback`() {
        // Given
        var downloadClicked = false
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = { downloadClicked = true },
                onDismiss = {},
                onLater = {}
            )
        }

        // This test case has no downloadUrl, so there's only 1 Download node (the button)
        composeTestRule.onNodeWithText("Download").performClick()

        // Then
        assert(downloadClicked)
    }

    @Test
    fun `clicking_later_button_triggers_onLater_callback`() {
        // Given
        var laterClicked = false
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false
        )

        // When
        composeTestRule.setContent {
            UpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = { laterClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Later").performClick()

        // Then
        assert(laterClicked)
    }

    
    @Test
    fun `customUpdateDialog_displays_correctly`() {
        // Given
        val updateInfo = UpdateInfo(
            currentVersion = VersionInfo(1, 0, 0, 100, "1.0.0"),
            latestVersion = VersionInfo(1, 1, 0, 110, "1.1.0"),
            isUpdateAvailable = true,
            isMandatory = false
        )

        // When
        composeTestRule.setContent {
            CustomUpdateDialog(
                updateInfo = updateInfo,
                onDownload = {},
                onDismiss = {},
                onLater = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Update available").assertIsDisplayed()
        composeTestRule.onNodeWithText("Download").assertIsDisplayed()
        composeTestRule.onNodeWithText("Later").assertIsDisplayed()
    }
}
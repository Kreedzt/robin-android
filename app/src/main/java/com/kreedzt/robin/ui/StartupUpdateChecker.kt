package com.kreedzt.robin.ui

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kreedzt.robin.data.UpdateInfo
import com.kreedzt.robin.data.UpdateState

/**
 * Component that checks for updates on app startup
 * Should be added to the root of your app to automatically check for updates
 */
@Composable
fun StartupUpdateChecker(
    updateViewModel: UpdateViewModel = viewModel(),
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

    // Notify when initial check is complete
    LaunchedEffect(updateState) {
        if (hasCheckedForUpdate && updateState !is com.kreedzt.robin.data.UpdateState.Checking) {
            val hasUpdate = when (updateState) {
                is com.kreedzt.robin.data.UpdateState.UpdateAvailable -> true
                else -> false
            }
            onInitialCheckComplete(hasUpdate)
        }
    }

    // Show update dialog if available
    showUpdateDialog?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            onDownload = updateViewModel::onDownloadClick,
            onDismiss = updateViewModel::onDialogDismiss,
            onLater = updateViewModel::onLaterClick
        )
    }
}
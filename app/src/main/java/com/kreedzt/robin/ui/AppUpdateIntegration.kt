package com.kreedzt.robin.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kreedzt.robin.data.UpdateInfo
import com.kreedzt.robin.data.VersionChecker

/**
 * Component to integrate app update checking into the main application
 * This should be added to your main screen or navigation
 *
 * Usage:
 * ```kotlin
 * @Composable
 * fun MyApp() {
 *     AppUpdateIntegration()
 *
 *     // Rest of your app UI
 * }
 * ```
 */
@Composable
fun AppUpdateIntegration(
    updateViewModel: UpdateViewModel = viewModel()
) {
    val showUpdateDialog by updateViewModel.showUpdateDialog.collectAsStateWithLifecycle()

    // Show update dialog when available
    showUpdateDialog?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            onDownload = updateViewModel::onDownloadClick,
            onDismiss = updateViewModel::onDialogDismiss,
            onLater = updateViewModel::onLaterClick
        )
    }
}

/**
 * Alternative: Manual integration in any Composable
 * Use this when you need more control over when to check for updates
 */
@Composable
fun ManualUpdateCheck(
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    // Check for update when composable is first composed
    LaunchedEffect(Unit) {
        val versionChecker = VersionChecker.getInstance(context)

        // Get current API region from settings
        // You might need to inject or get SettingsManager here
        val apiUrl = "https://robin.kreedzt.com" // or your API URL

        try {
            updateInfo = versionChecker.checkForUpdateFromApi(apiUrl)
        } catch (e: Exception) {
            // Handle error
        }
    }

    // Show dialog if update is available
    updateInfo?.let {
        UpdateDialog(
            updateInfo = it,
            onDownload = { /* Handle download */ },
            onDismiss = { /* Handle dismiss */ }
        )
    }
}

/**
 * Integration example in MainScreen
 * Add this to your existing MainScreen.kt
 */
@Composable
private fun MainScreenWithUpdateCheck() {
    // App update integration
    AppUpdateIntegration()

    // Your existing MainScreen content
    // ...
}

/**
 * Integration example in Navigation
 * Add this to your Navigation setup
 */
@Composable
private fun NavigationWithUpdateCheck() {
    // Wrap your navigation with update checking
    AppUpdateIntegration()

    // Your existing navigation setup
    // RobinNavHost(navController = navController)
}
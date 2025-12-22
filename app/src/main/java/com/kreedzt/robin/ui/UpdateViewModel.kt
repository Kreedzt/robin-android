package com.kreedzt.robin.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kreedzt.robin.R
import com.kreedzt.robin.data.UpdateInfo
import com.kreedzt.robin.data.UpdateState
import com.kreedzt.robin.data.VersionChecker
import com.kreedzt.robin.data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing app version updates
 * Handles checking for updates and managing update dialog state
 */
open class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    protected val context = application
    protected val versionChecker = VersionChecker.getInstance(context)
    protected val settingsManager = SettingsManager.getInstance(context)

    protected val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    open val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    protected val _showUpdateDialog = MutableStateFlow<UpdateInfo?>(null)
    open val showUpdateDialog: StateFlow<UpdateInfo?> = _showUpdateDialog.asStateFlow()

  
    /**
     * Check for available update
     * @param remoteVersionString Latest version string from remote API
     * @param downloadUrl Optional download URL for the update
     */
    fun checkForUpdate(
        remoteVersionString: String? = null,
        downloadUrl: String? = null
    ) {
        _updateState.value = UpdateState.Checking

        viewModelScope.launch {
            try {
                if (remoteVersionString != null) {
                    // Check with provided version
                    val updateInfo = versionChecker.checkForUpdate(remoteVersionString, downloadUrl)
                    handleUpdateResult(updateInfo)
                } else {
                    // Just get current version
                    val currentVersion = versionChecker.getCurrentVersion()
                    _updateState.value = currentVersion?.let {
                        UpdateState.CurrentVersion(it.displayName)
                    } ?: UpdateState.Error("Could not get version info")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Check for update from real API using current API region
     */
    open fun checkForUpdateFromApi() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking

            try {
                // Get current API region URL
                val currentApiRegion = SettingsManager.API_REGIONS
                    .find { it.id == settingsManager.apiRegionId }
                    ?: SettingsManager.API_REGIONS.firstOrNull()

                val apiUrl = currentApiRegion?.url
                    ?: return@launch

                // Check for update using API
                val result = versionChecker.checkForUpdateFromApiSafely(apiUrl)

                result.fold(
                    onSuccess = { updateInfo ->
                        handleUpdateResult(updateInfo)
                    },
                    onFailure = { error ->
                        _updateState.value = UpdateState.Error(error.message ?: "API request failed")
                    }
                )
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Simulate checking update from remote API
     * This is just for demonstration - replace with actual API call
     */
    fun simulateRemoteUpdateCheck() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking

            // Simulate network delay
            kotlinx.coroutines.delay(1000)

            // Simulate API response - change version to test update flow
            val simulatedRemoteVersion = "1.0.1" // Change this to test different scenarios
            val simulatedDownloadUrl = "https://github.com/your-repo/releases/latest"

            val updateInfo = versionChecker.checkForUpdate(
                remoteVersionString = simulatedRemoteVersion,
                downloadUrl = simulatedDownloadUrl
            )

            handleUpdateResult(updateInfo)
        }
    }

    private fun handleUpdateResult(updateInfo: UpdateInfo?) {
        if (updateInfo != null && updateInfo.isUpdateAvailable) {
            _updateState.value = UpdateState.UpdateAvailable(updateInfo)
            _showUpdateDialog.value = updateInfo
        } else {
            _updateState.value = UpdateState.NoUpdate
        }
    }

    /**
     * Handle download button click
     */
    open fun onDownloadClick() {
        val updateInfo = _showUpdateDialog.value
        if (updateInfo?.downloadUrl != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Could not open download link")
            }
        }
        _showUpdateDialog.value = null
    }

    /**
     * Handle later button click (only for non-mandatory updates)
     */
    open fun onLaterClick() {
        if (_showUpdateDialog.value?.isMandatory == false) {
            _showUpdateDialog.value = null
        }
    }

    /**
     * Handle dialog dismiss (only for non-mandatory updates)
     */
    open fun onDialogDismiss() {
        if (_showUpdateDialog.value?.isMandatory == false) {
            _showUpdateDialog.value = null
        }
    }
}
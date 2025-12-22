package com.kreedzt.robin.data

/**
 * State of update checking process
 */
sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class CurrentVersion(val version: String) : UpdateState()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateState()
    object NoUpdate : UpdateState()
    data class Error(val message: String) : UpdateState()
}
package com.kreedzt.robin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kreedzt.robin.R
import com.kreedzt.robin.data.UpdateInfo

/**
 * Dialog for displaying app update information
 * @param updateInfo Information about the available update
 * @param onDownload Callback when user clicks download button
 * @param onDismiss Callback when user dismisses the dialog (only for non-mandatory updates)
 * @param onLater Callback when user clicks later button (only for non-mandatory updates)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit = {},
    onLater: () -> Unit = {}
) {
    if (!updateInfo.isUpdateAvailable) return

    // Pre-compute string resources for efficiency
    val titleString = if (updateInfo.isMandatory) {
        stringResource(R.string.mandatory_update)
    } else {
        stringResource(R.string.update_available)
    }

    val updateMessage = stringResource(
        if (updateInfo.isMandatory)
            R.string.mandatory_update_message
        else
            R.string.optional_update_message,
        updateInfo.currentVersion.displayName,
        updateInfo.latestVersion.displayName
    )

    val downloadString = stringResource(R.string.download)
    val laterString = stringResource(R.string.later)

    AlertDialog(
        onDismissRequest = {
            if (!updateInfo.isMandatory) {
                onDismiss()
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = titleString)
        },
        text = {
          Column {
                Text(
                    text = updateMessage,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (!updateInfo.downloadUrl.isNullOrEmpty()) {
                    Text(
                        text = downloadString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                updateInfo.changelog?.let { changelog ->
                    Text(
                        text = changelog,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                updateInfo.releaseNotes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDownload
            ) {
                Text(downloadString)
            }
        },
        dismissButton = if (!updateInfo.isMandatory) {
            {
                TextButton(
                    onClick = onLater
                ) {
                    Text(laterString)
                }
            }
        } else null,
        modifier = Modifier
    )
}

/**
 * Alternative dialog implementation with custom layout
 * Use when you need more control over the dialog appearance
 */
@Composable
fun CustomUpdateDialog(
    updateInfo: UpdateInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit = {},
    onLater: () -> Unit = {}
) {
    if (!updateInfo.isUpdateAvailable) return

    Dialog(onDismissRequest = {
        if (!updateInfo.isMandatory) {
            onDismiss()
        }
    }) {
        Card(
            modifier = Modifier,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (updateInfo.isMandatory) {
                        stringResource(R.string.mandatory_update)
                    } else {
                        stringResource(R.string.update_available)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val updateMessage = stringResource(
                    if (updateInfo.isMandatory)
                        R.string.mandatory_update_message
                    else
                        R.string.optional_update_message,
                    updateInfo.currentVersion.displayName,
                    updateInfo.latestVersion.displayName
                )

                Text(
                    text = updateMessage,
                    style = MaterialTheme.typography.bodyMedium
                )

                updateInfo.changelog?.let { changelog ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = changelog,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    if (!updateInfo.isMandatory) {
                        OutlinedButton(
                            onClick = onLater,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.later))
                        }
                    }

                    Button(
                        onClick = onDownload,
                        modifier = Modifier.weight(if (!updateInfo.isMandatory) 1f else 2f)
                    ) {
                        Text(stringResource(R.string.download))
                    }
                }
            }
        }
    }
}
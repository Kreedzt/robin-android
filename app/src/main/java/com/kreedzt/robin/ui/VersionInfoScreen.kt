package com.kreedzt.robin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kreedzt.robin.R
import com.kreedzt.robin.data.SettingsManager
import com.kreedzt.robin.data.UpdateState
import com.kreedzt.robin.data.VersionChecker
import com.kreedzt.robin.data.VersionManager

/**
 * Screen displaying detailed version information and update checking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionInfoScreen(
    navController: NavController,
    updateViewModel: UpdateViewModel = viewModel()
) {
    val context = LocalContext.current
    val versionManager = VersionManager.getInstance(context)
    val settingsManager = SettingsManager.getInstance(context)

    val updateState by updateViewModel.updateState.collectAsStateWithLifecycle()
    val showUpdateDialog by updateViewModel.showUpdateDialog.collectAsStateWithLifecycle()

    var currentVersion by remember { mutableStateOf<String?>(null) }
    var versionCode by remember { mutableStateOf<Int?>(null) }

    // Fetch current version
    LaunchedEffect(Unit) {
        val version = versionManager.currentVersion
        currentVersion = version?.displayName
        versionCode = version?.versionCode
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_version)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Version Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.current_version),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = currentVersion ?: stringResource(R.string.loading),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    versionCode?.let {
                        Text(
                            text = "Version Code: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // API Region Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.api_region),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val currentApiRegion = SettingsManager.API_REGIONS
                        .find { it.id == settingsManager.apiRegionId }

                    Text(
                        text = currentApiRegion?.getLabel(settingsManager.language) ?: "Unknown",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = currentApiRegion?.url ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Update Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.update_status),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Button(
                            onClick = { updateViewModel.checkForUpdateFromApi() },
                            enabled = updateState !is UpdateState.Checking
                        ) {
                            if (updateState is UpdateState.Checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(stringResource(R.string.check_for_updates))
                        }
                    }

                    when (val state = updateState) {
                        is UpdateState.Idle -> {
                            Text(
                                text = stringResource(R.string.idle),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        is UpdateState.Checking -> {
                            Text(
                                text = stringResource(R.string.checking_for_update),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        is UpdateState.CurrentVersion -> {
                            Text(
                                text = "Current: ${state.version}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        is UpdateState.UpdateAvailable -> {
                            Text(
                                text = stringResource(R.string.update_available),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is UpdateState.NoUpdate -> {
                            Text(
                                text = stringResource(R.string.no_update_available),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is UpdateState.Error -> {
                            Text(
                                text = "Error: ${state.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // System Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.system_information),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Android: ${android.os.Build.VERSION.RELEASE}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "SDK: ${android.os.Build.VERSION.SDK_INT}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Language: ${settingsManager.language}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Links Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.links),
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedButton(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://github.com/Kreedzt/robin-android")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.view_on_github))
                    }
                }
            }
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
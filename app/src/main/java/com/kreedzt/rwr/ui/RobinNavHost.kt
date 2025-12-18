package com.kreedzt.rwr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kreedzt.rwr.R
import com.kreedzt.rwr.data.ServerRepository
import com.kreedzt.rwr.data.SettingsManager

@Composable
fun RobinNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    var showFirstLaunchSetup by remember { mutableStateOf(settingsManager.isFirstLaunch) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainScreenWithNav(navController, settingsManager)

                    // 首次使用设置弹窗
                    if (showFirstLaunchSetup) {
                        FirstLaunchSetupDialog(
                            settingsManager = settingsManager,
                            onDismiss = {
                                showFirstLaunchSetup = false
                            },
                            onComplete = {
                                showFirstLaunchSetup = false
                            }
                        )
                    }
                }
            }
            composable("settings") {
                SettingsScreen(navController, settingsManager)
            }
            composable("about") {
                AboutScreen(navController)
            }
        }
    }
}

@Composable
fun MainScreenWithNav(navController: NavHostController, settingsManager: SettingsManager) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        MainScreen(
            repository = ServerRepository(settingsManager, context)
        )

        // 浮动操作按钮
        FloatingActionButton(
            onClick = { navController.navigate("settings") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 应用图标
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        // 应用名称
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // 应用描述
                        Text(
                            text = stringResource(R.string.app_description),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 版本信息
                        Text(
                            text = "${stringResource(R.string.app_version)}: 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 版权信息
                        Text(
                            text = "© 2024 Robin Android",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // GitHub 链接
                        OutlinedButton(
                            onClick = {
                                // TODO: 打开 GitHub 页面
                            }
                        ) {
                            Text("View on GitHub")
                        }
                    }
                }
            }

            item {
                DisclaimerCard()
            }
        }
    }
}

@Composable
private fun DisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.disclaimer_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.disclaimer_content),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.disclaimer_data_source),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.disclaimer_trademark),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
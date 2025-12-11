package com.kreedzt.rwr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.os.Build
import com.kreedzt.rwr.R
import com.kreedzt.rwr.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsManager: SettingsManager = SettingsManager.getInstance(LocalContext.current)
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(settingsManager.language) }
    var selectedApiRegion by remember { mutableStateOf(settingsManager.apiRegion) }
    var selectedTheme by remember { mutableStateOf(settingsManager.themeMode) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
            // 语言设置
            item {
                SettingsSection(
                    title = stringResource(R.string.language),
                    icon = Icons.Default.Language
                ) {
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        LanguageOption(
                            text = "English",
                            selected = selectedLanguage == "en",
                            onClick = {
                                selectedLanguage = "en"
                                settingsManager.language = "en"
                                // Android 13+ 会自动处理UI更新
                            }
                        )
                        HorizontalDivider()
                        LanguageOption(
                            text = "简体中文",
                            selected = selectedLanguage == "zh",
                            onClick = {
                                selectedLanguage = "zh"
                                settingsManager.language = "zh"
                                // Android 13+ 会自动处理UI更新
                            }
                        )
                    }
                }
            }

            // 主题设置
            item {
                SettingsSection(
                    title = stringResource(R.string.theme),
                    icon = Icons.Default.DarkMode
                ) {
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        ThemeOption(
                            text = stringResource(R.string.theme_system),
                            selected = selectedTheme == SettingsManager.ThemeMode.SYSTEM,
                            onClick = {
                                selectedTheme = SettingsManager.ThemeMode.SYSTEM
                                settingsManager.themeMode = SettingsManager.ThemeMode.SYSTEM
                            }
                        )
                        HorizontalDivider()
                        ThemeOption(
                            text = stringResource(R.string.theme_light),
                            selected = selectedTheme == SettingsManager.ThemeMode.LIGHT,
                            onClick = {
                                selectedTheme = SettingsManager.ThemeMode.LIGHT
                                settingsManager.themeMode = SettingsManager.ThemeMode.LIGHT
                            }
                        )
                        HorizontalDivider()
                        ThemeOption(
                            text = stringResource(R.string.theme_dark),
                            selected = selectedTheme == SettingsManager.ThemeMode.DARK,
                            onClick = {
                                selectedTheme = SettingsManager.ThemeMode.DARK
                                settingsManager.themeMode = SettingsManager.ThemeMode.DARK
                            }
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            HorizontalDivider()
                            ThemeOption(
                                text = stringResource(R.string.theme_dynamic),
                                selected = selectedTheme == SettingsManager.ThemeMode.DYNAMIC,
                                onClick = {
                                    selectedTheme = SettingsManager.ThemeMode.DYNAMIC
                                    settingsManager.themeMode = SettingsManager.ThemeMode.DYNAMIC
                                }
                            )
                        }
                    }
                }
            }

            // API 区域设置
            item {
                SettingsSection(
                    title = stringResource(R.string.api_region),
                    icon = Icons.Default.Public
                ) {
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        ApiRegionOption(
                            text = stringResource(R.string.api_global),
                            url = "https://robin.kreedzt.com/",
                            selected = selectedApiRegion == SettingsManager.ApiRegion.GLOBAL,
                            onClick = {
                                selectedApiRegion = SettingsManager.ApiRegion.GLOBAL
                                settingsManager.apiRegion = SettingsManager.ApiRegion.GLOBAL
                            }
                        )
                        HorizontalDivider()
                        ApiRegionOption(
                            text = stringResource(R.string.api_china),
                            url = "https://robin.kreedzt.cn/",
                            selected = selectedApiRegion == SettingsManager.ApiRegion.CHINA,
                            onClick = {
                                selectedApiRegion = SettingsManager.ApiRegion.CHINA
                                settingsManager.apiRegion = SettingsManager.ApiRegion.CHINA
                            }
                        )
                    }
                }
            }

            // 关于部分
            item {
                SettingsSection(
                    title = stringResource(R.string.about),
                    icon = Icons.Default.Info
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${stringResource(R.string.app_version)}: 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "© 2025 Robin Android",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun LanguageOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

@Composable
private fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

@Composable
private fun ApiRegionOption(
    text: String,
    url: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}
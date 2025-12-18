package com.kreedzt.rwr.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreedzt.rwr.R
import com.kreedzt.rwr.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstLaunchSetupDialog(
    settingsManager: SettingsManager = SettingsManager.getInstance(LocalContext.current),
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    // 根据系统语言自动选择默认值
    val systemLanguage = remember {
        val lang = java.util.Locale.getDefault().language
        if (lang == "zh") "zh" else "en"
    }

    val defaultApiRegionId = remember {
        if (systemLanguage == "zh") "china" else "global"
    }

    var selectedLanguage by remember { mutableStateOf(systemLanguage) }
    var selectedApiRegionId by remember { mutableStateOf(defaultApiRegionId) }

    // 自动应用默认设置
    LaunchedEffect(Unit) {
        selectedLanguage = settingsManager.language.ifEmpty { systemLanguage }
        selectedApiRegionId = settingsManager.apiRegionId
    }

    if (settingsManager.isFirstLaunch) {
        AlertDialog(
            onDismissRequest = {
                // 应用默认设置然后关闭
                settingsManager.applyDefaultSettings()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(0.95f),
            title = {
                Text(
                    text = stringResource(R.string.first_launch_setup),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 欢迎信息
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.welcome_to_robin),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.first_launch_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    // 语言选择
                    Column {
                        Text(
                            text = stringResource(R.string.select_language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier.selectableGroup()
                        ) {
                            LanguageOption(
                                text = "English",
                                selected = selectedLanguage == "en",
                                onClick = { selectedLanguage = "en" }
                            )
                            HorizontalDivider()
                            LanguageOption(
                                text = "简体中文",
                                selected = selectedLanguage == "zh",
                                onClick = { selectedLanguage = "zh" }
                            )
                        }
                    }

                    // API区域选择
                    Column {
                        Text(
                            text = stringResource(R.string.select_api_region),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier.selectableGroup()
                        ) {
                            ApiRegionOption(
                                text = stringResource(R.string.api_global),
                                description = stringResource(R.string.api_global_description),
                                selected = selectedApiRegionId == "global",
                                onClick = { selectedApiRegionId = "global" }
                            )
                            HorizontalDivider()
                            ApiRegionOption(
                                text = stringResource(R.string.api_china),
                                description = stringResource(R.string.api_china_description),
                                selected = selectedApiRegionId == "china",
                                onClick = { selectedApiRegionId = "china" }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 保存设置
                        settingsManager.language = selectedLanguage
                        settingsManager.apiRegionId = selectedApiRegionId
                        settingsManager.isFirstLaunch = false
                        // 应用语言设置
                        settingsManager.applyLanguage(selectedLanguage)
                        onComplete()
                    }
                ) {
                    Text(stringResource(R.string.complete_setup))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // 应用默认设置然后关闭
                        settingsManager.applyDefaultSettings()
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.skip_setup))
                }
            }
        )
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
            .padding(vertical = 12.dp),
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
    description: String,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}
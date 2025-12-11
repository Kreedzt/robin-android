package com.kreedzt.rwr.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import com.kreedzt.rwr.data.SettingsManager

/**
 * 语言感知内容组件
 * 当语言设置改变时，会重新组合内容
 */
@Composable
fun LanguageAwareContent(
    settingsManager: SettingsManager,
    content: @Composable () -> Unit
) {
    // 监听全局语言状态
    val currentLanguage by settingsManager.languageState

    // 当语言改变时，LaunchedEffect会重新执行
    LaunchedEffect(currentLanguage) {
        // 这里可以添加任何需要在语言改变时执行的逻辑
        settingsManager.applyLanguage(currentLanguage)
    }

    // 使用key来强制重新组合content
    key(currentLanguage) {
        content()
    }
}
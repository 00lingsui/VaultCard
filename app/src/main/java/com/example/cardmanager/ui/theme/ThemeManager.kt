package com.example.cardmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppThemeStyle {
    MATERIAL3,
    MIUIX
}

class ThemeState(initialStyle: AppThemeStyle = AppThemeStyle.MIUIX) {
    var currentStyle by mutableStateOf(initialStyle)
        private set

    fun toggleStyle() {
        currentStyle = if (currentStyle == AppThemeStyle.MIUIX) AppThemeStyle.MATERIAL3 else AppThemeStyle.MIUIX
    }
}

val LocalThemeState = compositionLocalOf { ThemeState() }

@Composable
fun AppDynamicTheme(
    themeState: ThemeState,
    content: @Composable () -> Unit
) {
    when (themeState.currentStyle) {
        AppThemeStyle.MIUIX -> MIUIXTheme(darkTheme = true, content = content)
        AppThemeStyle.MATERIAL3 -> MaterialTheme(colorScheme = darkColorScheme(), content = content)
    }
}

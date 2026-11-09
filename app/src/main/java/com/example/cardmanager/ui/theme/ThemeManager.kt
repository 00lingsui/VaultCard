package com.example.cardmanager.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

enum class AppThemeStyle {
    MATERIAL3,
    MIUIX
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class ThemeState(
    private val prefs: SharedPreferences? = null,
    initialStyle: AppThemeStyle = AppThemeStyle.MIUIX,
    initialMode: AppThemeMode = AppThemeMode.SYSTEM
) {
    var currentStyle by mutableStateOf(
        prefs?.getString("pref_theme_style", null)?.let {
            runCatching { AppThemeStyle.valueOf(it) }.getOrNull()
        } ?: initialStyle
    )
        private set

    var currentMode by mutableStateOf(
        prefs?.getString("pref_theme_mode", null)?.let {
            runCatching { AppThemeMode.valueOf(it) }.getOrNull()
        } ?: initialMode
    )
        private set

    fun setStyle(style: AppThemeStyle) {
        currentStyle = style
        prefs?.edit()?.putString("pref_theme_style", style.name)?.apply()
    }

    fun setMode(mode: AppThemeMode) {
        currentMode = mode
        prefs?.edit()?.putString("pref_theme_mode", mode.name)?.apply()
    }

    fun toggleStyle() {
        val nextStyle = if (currentStyle == AppThemeStyle.MIUIX) AppThemeStyle.MATERIAL3 else AppThemeStyle.MIUIX
        setStyle(nextStyle)
    }
}

val LocalThemeState = compositionLocalOf { ThemeState() }

@Composable
fun rememberThemeState(context: Context = LocalContext.current): ThemeState {
    val prefs = remember(context) {
        context.getSharedPreferences("vaultcard_theme_prefs", Context.MODE_PRIVATE)
    }
    return remember(prefs) {
        ThemeState(prefs = prefs)
    }
}

@Composable
fun AppDynamicTheme(
    themeState: ThemeState,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeState.currentMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    when (themeState.currentStyle) {
        AppThemeStyle.MIUIX -> MIUIXTheme(darkTheme = isDark, content = content)
        AppThemeStyle.MATERIAL3 -> MaterialTheme(
            colorScheme = if (isDark) darkColorScheme() else lightColorScheme(),
            content = content
        )
    }
}

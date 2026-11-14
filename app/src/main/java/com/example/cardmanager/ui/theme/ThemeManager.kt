package com.example.cardmanager.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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
    initialMode: AppThemeMode = AppThemeMode.SYSTEM,
    initialPalette: AppThemePalette = AppThemePalette.OCEAN_BLUE
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

    var currentPalette by mutableStateOf(
        prefs?.getString("pref_theme_palette", null)?.let {
            runCatching { AppThemePalette.valueOf(it) }.getOrNull()
        } ?: initialPalette
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

    fun setPalette(palette: AppThemePalette) {
        currentPalette = palette
        prefs?.edit()?.putString("pref_theme_palette", palette.name)?.apply()
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
fun ThemeState.resolveIsDarkTheme(): Boolean {
    val isSystemDark = isSystemInDarkTheme()
    return when (currentMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
}

@Composable
fun AppDynamicTheme(
    themeState: ThemeState,
    content: @Composable () -> Unit
) {
    val isDark = themeState.resolveIsDarkTheme()

    when (themeState.currentStyle) {
        AppThemeStyle.MIUIX -> MIUIXTheme(
            darkTheme = isDark,
            palette = themeState.currentPalette,
            content = content
        )
        AppThemeStyle.MATERIAL3 -> MaterialTheme(
            colorScheme = buildMaterial3ColorScheme(darkTheme = isDark, palette = themeState.currentPalette),
            shapes = Md3Shapes,
            content = content
        )
    }
}

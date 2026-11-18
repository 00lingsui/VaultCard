package com.example.cardmanager.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

enum class AppThemeStyle {
    MATERIAL3,
    MIUIX
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class ThemePreferences(
    val style: AppThemeStyle = AppThemeStyle.MIUIX,
    val mode: AppThemeMode = AppThemeMode.SYSTEM,
    val palette: AppThemePalette = AppThemePalette.OCEAN_BLUE
)

private const val THEME_PREFS_NAME = "vaultcard_theme_prefs"

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = THEME_PREFS_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, THEME_PREFS_NAME))
    }
)

object ThemeManager {
    private val KEY_THEME_STYLE = stringPreferencesKey("pref_theme_style")
    private val KEY_THEME_MODE = stringPreferencesKey("pref_theme_mode")
    private val KEY_THEME_PALETTE = stringPreferencesKey("pref_theme_palette")

    fun themePreferencesFlow(context: Context): Flow<ThemePreferences> {
        val appContext = context.applicationContext
        return appContext.themeDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                ThemePreferences(
                    style = prefs[KEY_THEME_STYLE]?.let {
                        runCatching { AppThemeStyle.valueOf(it) }.getOrNull()
                    } ?: AppThemeStyle.MIUIX,
                    mode = prefs[KEY_THEME_MODE]?.let {
                        runCatching { AppThemeMode.valueOf(it) }.getOrNull()
                    } ?: AppThemeMode.SYSTEM,
                    palette = prefs[KEY_THEME_PALETTE]?.let {
                        runCatching { AppThemePalette.valueOf(it) }.getOrNull()
                    } ?: AppThemePalette.OCEAN_BLUE
                )
            }
    }

    fun themeModeFlow(context: Context): Flow<AppThemeMode> =
        themePreferencesFlow(context).map { it.mode }

    fun isDarkModeFlow(context: Context, systemIsDark: Boolean): Flow<Boolean> =
        themeModeFlow(context).map { mode ->
            when (mode) {
                AppThemeMode.SYSTEM -> systemIsDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
        }

    suspend fun setStyle(context: Context, style: AppThemeStyle) {
        context.applicationContext.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_STYLE] = style.name
        }
    }

    suspend fun setMode(context: Context, mode: AppThemeMode) {
        context.applicationContext.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setDarkMode(context: Context, isDark: Boolean) {
        setMode(context, if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT)
    }

    suspend fun toggleDarkMode(context: Context, currentlyDark: Boolean) {
        setDarkMode(context, !currentlyDark)
    }

    suspend fun setPalette(context: Context, palette: AppThemePalette) {
        context.applicationContext.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_PALETTE] = palette.name
        }
    }
}

class ThemeState(
    private val appContext: Context? = null,
    private val coroutineScope: CoroutineScope? = null,
    initialStyle: AppThemeStyle = AppThemeStyle.MIUIX,
    initialMode: AppThemeMode = AppThemeMode.SYSTEM,
    initialPalette: AppThemePalette = AppThemePalette.OCEAN_BLUE
) {
    var currentStyle by mutableStateOf(initialStyle)
        private set

    var currentMode by mutableStateOf(initialMode)
        private set

    var currentPalette by mutableStateOf(initialPalette)
        private set

    internal fun applyFromPreferences(prefs: ThemePreferences) {
        currentStyle = prefs.style
        currentMode = prefs.mode
        currentPalette = prefs.palette
    }

    fun setStyle(style: AppThemeStyle) {
        currentStyle = style
        val ctx = appContext ?: return
        coroutineScope?.launch { ThemeManager.setStyle(ctx, style) }
    }

    fun setMode(mode: AppThemeMode) {
        currentMode = mode
        val ctx = appContext ?: return
        coroutineScope?.launch { ThemeManager.setMode(ctx, mode) }
    }

    fun setDarkMode(isDark: Boolean) {
        setMode(if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT)
    }

    fun toggleDarkMode(currentlyDark: Boolean) {
        setDarkMode(!currentlyDark)
    }

    fun setPalette(palette: AppThemePalette) {
        currentPalette = palette
        val ctx = appContext ?: return
        coroutineScope?.launch { ThemeManager.setPalette(ctx, palette) }
    }

    fun toggleStyle() {
        val nextStyle = if (currentStyle == AppThemeStyle.MIUIX) AppThemeStyle.MATERIAL3 else AppThemeStyle.MIUIX
        setStyle(nextStyle)
    }
}

val LocalThemeState = compositionLocalOf { ThemeState() }

@Composable
fun rememberThemeState(context: Context = LocalContext.current): ThemeState {
    val appContext = context.applicationContext
    val coroutineScope = rememberCoroutineScope()
    val storedPrefs by remember(appContext) {
        ThemeManager.themePreferencesFlow(appContext)
    }.collectAsStateWithLifecycle(initialValue = null)

    val state = remember(appContext, coroutineScope) {
        ThemeState(appContext = appContext, coroutineScope = coroutineScope)
    }

    LaunchedEffect(storedPrefs) {
        storedPrefs?.let { state.applyFromPreferences(it) }
    }

    return state
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

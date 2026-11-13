package com.example.cardmanager.core.language

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(
    val code: String,
    val tag: String,
    val displayName: String,
    val nativeName: String
) {
    SYSTEM("system", "", "跟随系统", "System Default"),
    SIMPLIFIED_CHINESE("zh_cn", "zh-CN", "简体中文", "中文（简体）"),
    TRADITIONAL_CHINESE("zh_tw", "zh-TW", "繁體中文", "中文（繁體）"),
    ENGLISH("en", "en", "English", "English"),
    JAPANESE("ja", "ja", "日本語", "日本語"),
    KOREAN("ko", "ko", "한국어", "한국어"),
    FRENCH("fr", "fr", "Français", "Français"),
    GERMAN("de", "de", "Deutsch", "Deutsch"),
    SPANISH("es", "es", "Español", "Español")
}

object LanguageManager {
    private const val PREFS_NAME = "vaultcard_language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun getCurrentLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.code) ?: AppLanguage.SYSTEM.code
        return AppLanguage.entries.find { it.code == code } ?: AppLanguage.SYSTEM
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()

        try {
            val localeList = if (language.tag.isBlank()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(language.tag)
            }
            AppCompatDelegate.setApplicationLocales(localeList)
        } catch (_: Exception) {
            // Fallback gracefully if platform locale service is unavailable
        }
    }

    fun applySavedLanguage(context: Context) {
        try {
            val current = getCurrentLanguage(context)
            if (current == AppLanguage.SYSTEM) {
                if (!AppCompatDelegate.getApplicationLocales().isEmpty) {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
                return
            }
            val targetLocales = LocaleListCompat.forLanguageTags(current.tag)
            if (AppCompatDelegate.getApplicationLocales() != targetLocales) {
                AppCompatDelegate.setApplicationLocales(targetLocales)
            }
        } catch (_: Exception) {
            // Prevent startup crash if locale application fails
        }
    }
}

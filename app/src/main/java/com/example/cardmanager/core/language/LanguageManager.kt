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
    SIMPLIFIED_CHINESE("zh_cn", "zh-CN", "简体中文", "Simplified Chinese"),
    TRADITIONAL_CHINESE("zh_tw", "zh-TW", "繁體中文", "Traditional Chinese"),
    ENGLISH("en", "en", "English", "英语")
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

        val localeList = if (language.tag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun applySavedLanguage(context: Context) {
        val current = getCurrentLanguage(context)
        val localeList = if (current.tag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(current.tag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}

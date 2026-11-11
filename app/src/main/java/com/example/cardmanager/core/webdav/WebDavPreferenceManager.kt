package com.example.cardmanager.core.webdav

import android.content.Context
import android.content.SharedPreferences

object WebDavPreferenceManager {

    private const val PREF_NAME = "vaultcard_webdav_prefs"
    private const val KEY_SERVER_URL = "webdav_server_url"
    private const val KEY_USERNAME = "webdav_username"
    private const val KEY_PASSWORD = "webdav_password"
    private const val KEY_AUTO_SYNC = "webdav_auto_sync"
    private const val KEY_LAST_SYNC = "webdav_last_sync"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getConfig(context: Context): WebDavConfig {
        val prefs = getPrefs(context)
        return WebDavConfig(
            serverUrl = prefs.getString(KEY_SERVER_URL, "") ?: "",
            username = prefs.getString(KEY_USERNAME, "") ?: "",
            password = prefs.getString(KEY_PASSWORD, "") ?: "",
            autoSyncEnabled = prefs.getBoolean(KEY_AUTO_SYNC, false),
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC, 0L)
        )
    }

    fun saveConfig(context: Context, config: WebDavConfig) {
        getPrefs(context).edit()
            .putString(KEY_SERVER_URL, config.serverUrl.trim())
            .putString(KEY_USERNAME, config.username.trim())
            .putString(KEY_PASSWORD, config.password)
            .putBoolean(KEY_AUTO_SYNC, config.autoSyncEnabled)
            .putLong(KEY_LAST_SYNC, config.lastSyncTimestamp)
            .apply()
    }

    fun updateLastSyncTimestamp(context: Context, timestamp: Long = System.currentTimeMillis()) {
        getPrefs(context).edit()
            .putLong(KEY_LAST_SYNC, timestamp)
            .apply()
    }
}

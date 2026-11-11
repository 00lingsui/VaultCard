package com.example.cardmanager.core.webdav

data class WebDavConfig(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val autoSyncEnabled: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)

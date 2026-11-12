package com.example.cardmanager

import android.app.Application
import com.example.cardmanager.core.notification.NotificationHelper
import com.example.cardmanager.core.webdav.WebDavPreferenceManager
import com.example.cardmanager.core.worker.RepaymentWorker
import com.example.cardmanager.core.worker.WebDavSyncWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CardManagerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        com.example.cardmanager.core.language.LanguageManager.applySavedLanguage(this)
        NotificationHelper.createNotificationChannel(this)
        RepaymentWorker.scheduleDailyReminder(this)

        val config = WebDavPreferenceManager.getConfig(this)
        if (config.autoSyncEnabled) {
            WebDavSyncWorker.scheduleDailySync(this, true)
        }
    }
}

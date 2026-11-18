package com.example.cardmanager

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.cardmanager.core.notification.CardReminderNotificationManager
import com.example.cardmanager.core.notification.ReminderScheduler
import com.example.cardmanager.core.webdav.WebDavPreferenceManager
import com.example.cardmanager.core.worker.WebDavSyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CardManagerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        runCatching {
            com.example.cardmanager.core.language.LanguageManager.applySavedLanguage(this)
        }
        runCatching {
            CardReminderNotificationManager.createNotificationChannel(this)
            applicationScope.launch {
                reminderScheduler.scheduleFromPreferences()
            }
        }
        runCatching {
            val config = WebDavPreferenceManager.getConfig(this)
            if (config.autoSyncEnabled) {
                WebDavSyncWorker.scheduleDailySync(this, true)
            }
        }
    }
}

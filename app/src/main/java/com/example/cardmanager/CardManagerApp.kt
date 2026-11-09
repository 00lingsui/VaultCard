package com.example.cardmanager

import android.app.Application
import com.example.cardmanager.core.notification.NotificationHelper
import com.example.cardmanager.core.worker.RepaymentWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CardManagerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        RepaymentWorker.scheduleDailyReminder(this)
    }
}

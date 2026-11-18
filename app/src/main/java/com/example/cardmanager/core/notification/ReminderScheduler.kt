package com.example.cardmanager.core.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: ReminderPreferenceManager
) {

    suspend fun scheduleFromPreferences() {
        val settings = preferenceManager.getSettings()
        syncSchedule(
            reminderHour = settings.reminderHour,
            enabled = settings.enabled
        )
    }

    fun syncSchedule(reminderHour: Int, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LEGACY_WORK_NAME)
        if (!enabled) {
            workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
            return
        }

        val initialDelayMillis = computeInitialDelayMillis(reminderHour)
        val periodicRequest = PeriodicWorkRequestBuilder<RepaymentReminderWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .addTag(TAG_REMINDER_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }

    fun triggerImmediateCheck() {
        val oneTimeRequest = OneTimeWorkRequestBuilder<RepaymentReminderWorker>()
            .addTag(TAG_REMINDER_WORK)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

    private fun computeInitialDelayMillis(targetHour: Int): Long {
        val clampedHour = targetHour.coerceIn(0, 23)
        val now = ZonedDateTime.now()
        var nextTrigger = now
            .withHour(clampedHour)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        if (!nextTrigger.isAfter(now)) {
            nextTrigger = nextTrigger.plusDays(1)
        }
        return Duration.between(now, nextTrigger).toMillis().coerceAtLeast(0L)
    }

    companion object {
        private const val LEGACY_WORK_NAME = "VaultCardRepaymentCheck"
        const val PERIODIC_WORK_NAME = "VaultCardSmartRepaymentReminder"
        const val ONE_TIME_WORK_NAME = "VaultCardSmartRepaymentImmediateCheck"
        const val TAG_REMINDER_WORK = "vaultcard_repayment_reminder"
    }
}

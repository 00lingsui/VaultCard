package com.example.cardmanager.core.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.cardmanager.core.common.util.calculateDaysUntilRepayment
import com.example.cardmanager.core.database.AppDatabase
import com.example.cardmanager.core.notification.NotificationHelper
import java.util.concurrent.TimeUnit

class RepaymentWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getInstance(applicationContext)
            val cards = database.creditCardDao().getAllCardsList()

            for (card in cards) {
                val daysRemaining = card.calculateDaysUntilRepayment()
                if (daysRemaining in 0..3) {
                    NotificationHelper.showRepaymentReminder(
                        context = applicationContext,
                        card = card,
                        daysRemaining = daysRemaining
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "VaultCardRepaymentCheck"

        fun scheduleDailyReminder(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<RepaymentWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}

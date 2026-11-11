package com.example.cardmanager.core.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.cardmanager.core.backup.BackupManager
import com.example.cardmanager.core.webdav.WebDavClient
import com.example.cardmanager.core.webdav.WebDavPreferenceManager
import java.util.concurrent.TimeUnit

class WebDavSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val config = WebDavPreferenceManager.getConfig(applicationContext)

        if (!config.autoSyncEnabled || config.serverUrl.isBlank() || config.username.isBlank()) {
            return Result.success()
        }

        return try {
            val backupManager = BackupManager()
            val jsonString = backupManager.createBackupJsonString(applicationContext)
            val uploadResult = WebDavClient.uploadBackup(config, jsonString)

            if (uploadResult.isSuccess) {
                WebDavPreferenceManager.updateLastSyncTimestamp(applicationContext)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "VaultCardWebDavAutoSync"

        fun scheduleDailySync(context: Context, enabled: Boolean) {
            val workManager = WorkManager.getInstance(context)
            if (!enabled) {
                workManager.cancelUniqueWork(WORK_NAME)
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WebDavSyncWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}

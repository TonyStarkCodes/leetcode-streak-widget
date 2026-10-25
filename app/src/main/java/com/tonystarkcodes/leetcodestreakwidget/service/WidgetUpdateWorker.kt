package com.tonystarkcodes.leetcodestreakwidget.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.tonystarkcodes.leetcodestreakwidget.data.repository.LeetCodeRepository
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val WORK_NAME = "LeetCodeWidgetUpdateWorker"

        fun schedulePeriodicSync(context: Context, intervalHours: Int = 12) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                intervalHours.toLong(), TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
        }

        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val repo = LeetCodeRepository(applicationContext)
            val username = repo.preferences.getUsername()
            if (username.isBlank()) {
                return Result.success()
            }
            val syncResult = repo.refreshCurrentProfile()
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

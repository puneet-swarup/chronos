package com.chronos.app.scheduling

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.chronos.app.ServiceLocator

// Runs one schedule on WorkManager's schedule. Thin by design: the executor does
// the recording, notifying and re-arming, exactly as it does for alarms.
class ScheduleWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val scheduleId = inputData.getString(KEY_SCHEDULE_ID) ?: return Result.failure()
        return try {
            ServiceLocator.scheduleExecutor.execute(scheduleId)
            Result.success()
        } catch (t: Throwable) {
            // The executor has already recorded the failure. Retrying would risk a
            // duplicate run, so this reports success to WorkManager and lets the
            // Activity screen show what went wrong.
            Result.success()
        }
    }

    companion object {
        const val KEY_SCHEDULE_ID = "scheduleId"

        fun inputFor(scheduleId: String): Data =
            Data.Builder().putString(KEY_SCHEDULE_ID, scheduleId).build()
    }
}

package com.chronos.app.scheduling

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// The WorkManager half of the scheduler: flexible and interval work that does not
// need a precise instant. It makes no decisions - the planner has already chosen
// the mechanism and the period.
//
// Unique work per schedule id is what prevents duplicates: re-planning replaces the
// existing request instead of stacking a second one.
class WorkScheduler(private val context: Context) {

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    fun schedule(scheduleId: String, intervalMinutes: Long?) {
        val periodic = intervalMinutes != null && intervalMinutes >= MIN_INTERVAL_MINUTES
        if (periodic) {
            val request = PeriodicWorkRequestBuilder<ScheduleWorker>(intervalMinutes!!, TimeUnit.MINUTES)
                .setInputData(ScheduleWorker.inputFor(scheduleId))
                .addTag(tag(scheduleId))
                .build()
            workManager.enqueueUniquePeriodicWork(
                uniqueName(scheduleId),
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        } else {
            val request = OneTimeWorkRequestBuilder<ScheduleWorker>()
                .setInputData(ScheduleWorker.inputFor(scheduleId))
                .addTag(tag(scheduleId))
                .build()
            workManager.enqueueUniqueWork(
                uniqueName(scheduleId),
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }

    fun cancel(scheduleId: String) {
        workManager.cancelUniqueWork(uniqueName(scheduleId))
        workManager.cancelAllWorkByTag(tag(scheduleId))
    }

    private fun uniqueName(scheduleId: String) = "chronos-schedule-" + scheduleId

    private fun tag(scheduleId: String) = "schedule:" + scheduleId

    private companion object {
        const val MIN_INTERVAL_MINUTES = 15L
    }
}

package com.chronos.app.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.chronos.app.ServiceLocator
import kotlinx.coroutines.launch

// Fires when an armed alarm goes off. The broadcast is short-lived, so the work
// moves to the application scope under goAsync(), which keeps the process alive
// until the database write and the notification are done.
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_FIRE) return
        val scheduleId = intent.getStringExtra(AlarmScheduler.EXTRA_SCHEDULE_ID)
        if (scheduleId.isNullOrBlank()) {
            Log.e(TAG, "alarm fired without a schedule id")
            return
        }

        val pending = goAsync()
        ServiceLocator.appScope.launch {
            try {
                ServiceLocator.scheduleExecutor.execute(scheduleId)
            } catch (t: Throwable) {
                Log.e(TAG, "execution failed for schedule " + scheduleId, t)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "ChronosAlarmReceiver"
    }
}

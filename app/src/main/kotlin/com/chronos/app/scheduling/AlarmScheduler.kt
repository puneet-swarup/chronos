package com.chronos.app.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.chronos.core.scheduling.ArmRequest

// The only place that talks to AlarmManager. It makes no decisions: the planner
// has already decided what to arm and whether exactness is available, and this
// class either performs the request or reports that it could not.
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager? = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return runCatching { alarmManager?.canScheduleExactAlarms() == true }.getOrDefault(false)
    }

    fun arm(request: ArmRequest) {
        val manager = alarmManager ?: return
        val pending = pendingIntent(request.scheduleId, PendingIntent.FLAG_UPDATE_CURRENT)
        if (pending == null) {
            Log.e(TAG, "could not build a PendingIntent for schedule " + request.scheduleId)
            return
        }
        try {
            if (request.exact && canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, request.triggerAtMillis, pending)
            } else {
                // setAndAllowWhileIdle still fires through Doze, just not to the
                // second. That is the honest fallback when exact access is denied.
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, request.triggerAtMillis, pending)
            }
        } catch (e: SecurityException) {
            // Exact-alarm access can be revoked between the check and the call.
            Log.e(TAG, "alarm rejected for schedule " + request.scheduleId, e)
            runCatching {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, request.triggerAtMillis, pending)
            }
        }
    }

    fun cancel(scheduleId: String) {
        val manager = alarmManager ?: return
        // FLAG_NO_CREATE means an alarm that was never armed is not created just
        // to be cancelled.
        val pending = pendingIntent(scheduleId, PendingIntent.FLAG_NO_CREATE) ?: return
        manager.cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(scheduleId: String, extraFlags: Int): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(ACTION_FIRE)
            .putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        return PendingIntent.getBroadcast(
            context,
            requestCode(scheduleId),
            intent,
            extraFlags or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val TAG = "ChronosAlarmScheduler"
        const val ACTION_FIRE = "com.chronos.app.action.FIRE_SCHEDULE"
        const val EXTRA_SCHEDULE_ID = "com.chronos.app.extra.SCHEDULE_ID"

        // A stable per-schedule request code. String.hashCode is specified by the
        // JVM, so the same id resolves to the same alarm across restarts and
        // upgrades. A hash collision between two schedules would make one replace
        // the other; with a UUID-shaped id the odds are negligible, and the
        // reconciler re-arms on every pass anyway.
        fun requestCode(scheduleId: String): Int = scheduleId.hashCode() and 0x7fffffff
    }
}

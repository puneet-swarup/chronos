package com.chronos.app.scheduling

import android.content.Context
import android.util.Log

// Re-arms schedules after boot / upgrade / permission changes. This is the
// seam where persistent schedules (Room) are read and AlarmManager alarms are
// recreated. Until the Room store is wired it is a safe no-op that logs.
object ScheduleReconciler {
 private const val TAG = "ChronosReconciler"

 fun reconcile(context: Context) {
 Log.i(TAG, "Reconciling schedules for package " + context.packageName)
 }
}

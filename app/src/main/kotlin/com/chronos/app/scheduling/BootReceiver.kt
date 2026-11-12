package com.chronos.app.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

// Restores schedules after device boot or app upgrade. AlarmManager alarms are
// cancelled on shutdown, so schedules must be re-armed from persistent storage
// (PRD M5: reboot recovery). The actual re-arming is delegated to
// ScheduleReconciler, wired in a later phase once the Room store exists.
class BootReceiver : BroadcastReceiver() {
 override fun onReceive(context: Context, intent: Intent) {
 when (intent.action) {
 Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
 Log.i(TAG, "Boot/upgrade received; schedule reconciliation required")
 ScheduleReconciler.reconcile(context)
 }
 }
 }

 companion object {
 private const val TAG = "ChronosBootReceiver"
 }
}

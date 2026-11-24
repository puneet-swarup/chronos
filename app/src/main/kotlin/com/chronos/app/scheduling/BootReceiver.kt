package com.chronos.app.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.chronos.app.ServiceLocator
import kotlinx.coroutines.launch

// AlarmManager alarms do not survive a reboot, and an app upgrade cancels them,
// so schedules are re-armed from the database (PRD M5: reboot recovery).
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> Unit
            else -> return
        }

        val pending = goAsync()
        ServiceLocator.appScope.launch {
            try {
                ServiceLocator.scheduleReconciler.reconcile()
            } catch (t: Throwable) {
                Log.e(TAG, "reconciliation after boot failed", t)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "ChronosBootReceiver"
    }
}

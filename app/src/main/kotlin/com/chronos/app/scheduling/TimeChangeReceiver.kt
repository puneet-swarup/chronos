package com.chronos.app.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.chronos.app.ServiceLocator
import kotlinx.coroutines.launch

// A clock or timezone change invalidates every absolute trigger time, so the
// whole schedule set is re-planned. Without this, a phone that flies to another
// timezone fires yesterday's alarms.
class TimeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> Unit
            else -> return
        }

        val pending = goAsync()
        ServiceLocator.appScope.launch {
            try {
                ServiceLocator.scheduleReconciler.reconcile()
            } catch (t: Throwable) {
                Log.e(TAG, "reconciliation after a time change failed", t)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "ChronosTimeChange"
    }
}

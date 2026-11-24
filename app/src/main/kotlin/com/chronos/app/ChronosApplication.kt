package com.chronos.app

import android.app.Application
import com.chronos.app.notifications.NotificationChannels
import kotlinx.coroutines.launch

// Application entry point. Initialises the manual dependency graph once, creates
// the notification channels, and re-arms schedules on every app start - the
// cheapest defence against an OEM that dropped an alarm while the app was dead.
class ChronosApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        NotificationChannels.create(this)
        ServiceLocator.appScope.launch {
            runCatching { ServiceLocator.scheduleReconciler.reconcile() }
        }
    }
}

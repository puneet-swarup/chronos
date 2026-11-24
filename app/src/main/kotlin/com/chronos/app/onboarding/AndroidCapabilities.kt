package com.chronos.app.onboarding

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat

// Reads the real permission state. Every check is defensive: an OEM that throws
// on a system service must not crash the first run, so failures degrade to "not
// granted", which is the honest answer.
class AndroidCapabilityProbe(private val context: Context) : CapabilityProbe {

    override fun read(): DeviceCapabilities = DeviceCapabilities(
        notificationsEnabled = notificationsEnabled(),
        exactAlarmsAllowed = exactAlarmsAllowed(),
        batteryOptimisationIgnored = batteryOptimisationIgnored(),
        dndAccessGranted = dndAccessGranted(),
    )

    private fun notificationsEnabled(): Boolean = runCatching {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }.getOrDefault(false)

    private fun exactAlarmsAllowed(): Boolean = runCatching {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // Before Android 12 exact alarms need no special access.
            true
        } else {
            val alarms = context.getSystemService(AlarmManager::class.java)
            alarms?.canScheduleExactAlarms() ?: false
        }
    }.getOrDefault(false)

    private fun batteryOptimisationIgnored(): Boolean = runCatching {
        val power = context.getSystemService(PowerManager::class.java)
        power?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }.getOrDefault(false)

    private fun dndAccessGranted(): Boolean = runCatching {
        val notifications = context.getSystemService(NotificationManager::class.java)
        notifications?.isNotificationPolicyAccessGranted ?: false
    }.getOrDefault(false)
}

// SharedPreferences is enough for one boolean; adding DataStore for this would
// be a dependency for its own sake.
class SharedPreferencesOnboardingStore(context: Context) : OnboardingStore {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun isCompleted(): Boolean = prefs.getBoolean(KEY_COMPLETED, false)

    override fun markCompleted() {
        prefs.edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    override fun reset() {
        prefs.edit().putBoolean(KEY_COMPLETED, false).apply()
    }

    private companion object {
        const val PREFS = "chronos.onboarding"
        const val KEY_COMPLETED = "completed"
    }
}

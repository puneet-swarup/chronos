package com.chronos.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chronos.app.MainActivity
import com.chronos.app.R
import com.chronos.app.data.ScheduleEntity

// Creates the notification channels Chronos posts on. Must run before anything
// tries to notify, so it is called from Application.onCreate.
object NotificationChannels {

    const val SCHEDULES = "schedules"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            SCHEDULES,
            "Scheduled alarms",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Fires when a Chronos schedule runs"
        }
        manager.createNotificationChannel(channel)
    }
}

// Posts the user-visible result of a schedule. Deliberately plain: the alarm is
// the feature, and a custom layout would only add surface area to break.
class ExecutionNotifier(private val context: Context) {

    fun notifyScheduleFired(schedule: ScheduleEntity) {
        val openApp = PendingIntent.getActivity(
            context,
            schedule.id.hashCode(),
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.SCHEDULES)
            .setSmallIcon(R.drawable.ic_stat_chronos)
            .setContentTitle(schedule.displayName())
            .setContentText("Scheduled by Chronos")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        // On Android 13+ this throws SecurityException without POST_NOTIFICATIONS.
        // The executor turns that into a FAILED execution row rather than a
        // notification that quietly never appeared.
        NotificationManagerCompat.from(context).notify(schedule.id.hashCode(), notification)
    }
}

private fun ScheduleEntity.displayName(): String = name.ifBlank { id }

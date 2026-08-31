package com.swagatmali.remider.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Builds and posts reminder notifications using the framework Notification API
 * (deliberately not NotificationCompat, to avoid adding an androidx.core
 * dependency). The channel is created lazily on API 26+.
 *
 * Each notification carries three action buttons:
 *   [10 min] [1 hour] -> [ReminderActionReceiver] (quick relative snooze)
 *   [More…]           -> [SnoozePickerActivity]   (full duration picker)
 */
internal object ReminderNotifications {

    const val CHANNEL_ID = "reminders"
    private const val CHANNEL_NAME = "Reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply { description = "Scheduled reminder alerts" }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun show(context: Context, notificationId: Int, reminderId: String, title: String, text: String) {
        ensureChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH)
        }

        val notification = builder
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setAutoCancel(true)
            .setContentIntent(launchAppIntent(context, notificationId))
            .addAction(snoozeAction(context, reminderId, label = "10 min", minutes = 10))
            .addAction(snoozeAction(context, reminderId, label = "1 hour", minutes = 60))
            .addAction(moreAction(context, reminderId))
            .build()

        manager.notify(notificationId, notification)
    }

    /** Tapping the notification body reopens the app's launcher activity. */
    private fun launchAppIntent(context: Context, requestCode: Int): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return null
        return PendingIntent.getActivity(
            context,
            requestCode,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** Quick relative-snooze button -> [ReminderActionReceiver]. */
    @Suppress("DEPRECATION") // int-icon Action.Builder; the Icon overload adds nothing here
    private fun snoozeAction(
        context: Context,
        reminderId: String,
        label: String,
        minutes: Int,
    ): Notification.Action {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_SNOOZE
            putExtra(ReminderActionReceiver.EXTRA_ID, reminderId)
            putExtra(ReminderActionReceiver.EXTRA_SNOOZE_MINUTES, minutes)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            "$reminderId#snooze#$minutes".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Action.Builder(android.R.drawable.ic_menu_recent_history, label, pending).build()
    }

    /** "More…" button -> [SnoozePickerActivity] (full picker). */
    @Suppress("DEPRECATION")
    private fun moreAction(context: Context, reminderId: String): Notification.Action {
        val intent = Intent(context, SnoozePickerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(SnoozePickerActivity.EXTRA_ID, reminderId)
        }
        val pending = PendingIntent.getActivity(
            context,
            "$reminderId#more".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Action.Builder(android.R.drawable.ic_menu_recent_history, "More…", pending).build()
    }
}

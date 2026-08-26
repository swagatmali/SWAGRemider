package com.swagatmali.remider.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fires when an AlarmManager alarm scheduled by [AndroidReminderScheduler] goes
 * off. Reads the reminder details from the intent extras and posts the
 * notification. Kept intentionally light — no DB access on the main thread.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder"
        val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
        ReminderNotifications.show(context, notificationId = id.hashCode(), title = title, text = text)
    }

    companion object {
        const val ACTION_FIRE = "com.swagatmali.remider.action.FIRE_REMINDER"
        const val EXTRA_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_reminder_title"
        const val EXTRA_TEXT = "extra_reminder_text"
    }
}

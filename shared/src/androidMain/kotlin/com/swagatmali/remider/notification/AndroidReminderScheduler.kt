package com.swagatmali.remider.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import kotlinx.datetime.toInstant

/**
 * Android [ReminderScheduler] backed by AlarmManager. Each reminder maps to a
 * PendingIntent (request code derived from its id) targeting [ReminderAlarmReceiver],
 * which posts the notification when the alarm fires.
 *
 * Exact alarms are used when permitted (API 31+ gates them behind
 * canScheduleExactAlarms); otherwise we fall back to an inexact allow-while-idle
 * alarm so scheduling never throws.
 */
class AndroidReminderScheduler(
    private val context: Context,
) : ReminderScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(reminder: Reminder) {
        // Never schedule for finished or deleted reminders — clear any prior alarm.
        if (reminder.isCompleted || reminder.isDeleted) {
            cancel(reminder.id)
            return
        }
        val triggerAtMillis = reminder.dueDateTime
            .toInstant(reminder.timeZone)
            .toEpochMilliseconds()
        // Don't fire stale alarms for times already in the past.
        if (triggerAtMillis <= System.currentTimeMillis()) {
            cancel(reminder.id)
            return
        }
        val pending = buildPendingIntent(reminder.id, reminder.title, reminder.notes, mutableCreate = true)!!
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }

    override fun cancel(id: ReminderId) {
        buildPendingIntent(id, mutableCreate = false)?.let { alarmManager.cancel(it) }
    }

    private fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    /**
     * Builds the PendingIntent for [id]. With [mutableCreate] true a new one is
     * created/updated (for scheduling); false uses FLAG_NO_CREATE so we only
     * match an existing alarm (for cancellation) — returns null if none exists.
     */
    private fun buildPendingIntent(
        id: ReminderId,
        title: String? = null,
        notes: String? = null,
        mutableCreate: Boolean,
    ): PendingIntent? {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_FIRE
            putExtra(ReminderAlarmReceiver.EXTRA_ID, id.value)
            title?.let { putExtra(ReminderAlarmReceiver.EXTRA_TITLE, it) }
            putExtra(ReminderAlarmReceiver.EXTRA_TEXT, notes.orEmpty())
        }
        val flags = if (mutableCreate) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, id.value.hashCode(), intent, flags)
    }
}

package com.swagatmali.remider.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.model.RepeatInterval
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import kotlinx.datetime.toInstant

/**
 * Android [ReminderScheduler] backed by AlarmManager. Each reminder maps to a
 * PendingIntent (request code derived from its id) targeting [ReminderAlarmReceiver],
 * which posts the notification when the alarm fires.
 *
 * The firing instant is computed by [computeTriggerMillis]:
 *   - a future [Reminder.snoozedUntil] wins (a one-off snooze override);
 *   - otherwise the base is the stored [Reminder.dueDateTime] + timeZone;
 *   - for a repeating reminder the base is rolled forward to the first cadence
 *     slot after "now", and the series stops once that slot passes
 *     [Reminder.repeatUntil].
 * This makes scheduling idempotent and reboot-safe: re-scheduling a long-running
 * recurring reminder always lands on its next future occurrence rather than a
 * stale past one.
 *
 * AlarmManager one-shot alarms don't repeat themselves, so recurrence is driven
 * by [ReminderAlarmReceiver] re-scheduling after each firing (self-rescheduling).
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
        val triggerAtMillis = computeTriggerMillis(reminder)
        if (triggerAtMillis == null) {
            // Nothing more to fire (past one-off, or recurrence has ended).
            cancel(reminder.id)
            return
        }
        val pending = buildPendingIntent(
            id = reminder.id,
            title = reminder.title,
            notes = reminder.notes,
            repeat = reminder.repeat,
            mutableCreate = true,
        )!!
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }

    override fun cancel(id: ReminderId) {
        buildPendingIntent(id, mutableCreate = false)?.let { alarmManager.cancel(it) }
    }

    /**
     * Absolute epoch-millis of the next firing, or null if there is nothing left
     * to fire. See the class doc for the ordering (snooze → due → cadence).
     */
    private fun computeTriggerMillis(reminder: Reminder): Long? {
        val now = System.currentTimeMillis()
        // A future snooze overrides everything: fire once at the snoozed instant.
        reminder.snoozedUntil?.toEpochMilliseconds()?.let { if (it > now) return it }

        val base = reminder.dueDateTime.toInstant(reminder.timeZone).toEpochMilliseconds()
        if (!reminder.repeat.repeats) {
            return base.takeIf { it > now }
        }
        // Recurring: roll forward to the first cadence slot strictly after now.
        val step = reminder.repeat.stepMillis
        var next = base
        if (next <= now) {
            val steps = (now - base) / step + 1
            next += steps * step
        }
        val until = reminder.repeatUntil?.toEpochMilliseconds()
        return if (until != null && next > until) null else next
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
     *
     * [repeat] is carried in the extras so [ReminderAlarmReceiver] can tell a
     * one-off firing (do nothing extra) from a recurring one (re-arm the next).
     */
    private fun buildPendingIntent(
        id: ReminderId,
        title: String? = null,
        notes: String? = null,
        repeat: RepeatInterval = RepeatInterval.NONE,
        mutableCreate: Boolean,
    ): PendingIntent? {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_FIRE
            putExtra(ReminderAlarmReceiver.EXTRA_ID, id.value)
            title?.let { putExtra(ReminderAlarmReceiver.EXTRA_TITLE, it) }
            putExtra(ReminderAlarmReceiver.EXTRA_TEXT, notes.orEmpty())
            putExtra(ReminderAlarmReceiver.EXTRA_REPEAT, repeat.name)
        }
        val flags = if (mutableCreate) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, id.value.hashCode(), intent, flags)
    }
}

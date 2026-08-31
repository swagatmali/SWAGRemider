package com.swagatmali.remider.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.model.RepeatInterval
import com.swagatmali.remider.domain.usecase.RescheduleReminderUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Fires when an AlarmManager alarm scheduled by [AndroidReminderScheduler] goes
 * off. Reads the reminder details from the intent extras and posts the
 * notification.
 *
 * For a recurring reminder ([EXTRA_REPEAT] != NONE) it then re-arms the next
 * occurrence via [RescheduleReminderUseCase] (reloading from the DB so a series
 * the user marked done is not re-armed). One-off reminders stay light — no DB
 * access, no Koin lookup on the firing path.
 */
class ReminderAlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleReminder: RescheduleReminderUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder"
        val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
        ReminderNotifications.show(
            context,
            notificationId = id.hashCode(),
            reminderId = id,
            title = title,
            text = text,
        )

        val repeats = intent.getStringExtra(EXTRA_REPEAT)
            ?.let { runCatching { RepeatInterval.valueOf(it) }.getOrNull() }
            ?.repeats == true
        if (repeats) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    rescheduleReminder(ReminderId(id))
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "com.swagatmali.remider.action.FIRE_REMINDER"
        const val EXTRA_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_reminder_title"
        const val EXTRA_TEXT = "extra_reminder_text"
        const val EXTRA_REPEAT = "extra_reminder_repeat"
    }
}

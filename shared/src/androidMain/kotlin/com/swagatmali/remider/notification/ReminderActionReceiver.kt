package com.swagatmali.remider.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.usecase.SnoozeReminderUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Instant

/**
 * Handles the notification's quick-snooze buttons ("10 min", "1 hour"). Computes
 * the absolute snooze instant, dismisses the acted-on notification, and delegates
 * to [SnoozeReminderUseCase] (which persists the override and reschedules).
 *
 * Uses goAsync() so the suspend use case can complete off the main thread; Koin
 * dependencies come from the global graph started in the Application.
 */
class ReminderActionReceiver : BroadcastReceiver(), KoinComponent {

    private val snoozeReminder: SnoozeReminderUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SNOOZE) return
        val reminderId = intent.getStringExtra(EXTRA_ID) ?: return
        val minutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 0)
        if (minutes <= 0) return

        // Dismiss the notification the user tapped the action on.
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(reminderId.hashCode())

        val until = Instant.fromEpochMilliseconds(System.currentTimeMillis() + minutes * 60_000L)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                snoozeReminder(ReminderId(reminderId), until)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SNOOZE = "com.swagatmali.remider.action.SNOOZE"
        const val EXTRA_ID = "extra_reminder_id"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    }
}

package com.swagatmali.remider.notification

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.usecase.SnoozeReminderUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import kotlin.time.Instant

/**
 * Transparent activity opened by the notification's "More…" action. Presents a
 * framework dialog of snooze durations (no Compose / no extra dependencies), then
 * delegates to [SnoozeReminderUseCase]. Finishes as soon as the user picks an
 * option or cancels.
 *
 * Lives in androidMain so it can reach the shared domain + Koin graph directly;
 * it is declared in the app manifest by fully-qualified name.
 */
class SnoozePickerActivity : Activity(), KoinComponent {

    private val snoozeReminder: SnoozeReminderUseCase by inject()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val reminderId = intent.getStringExtra(EXTRA_ID)
        if (reminderId == null) {
            finish()
            return
        }
        showOptions(reminderId)
    }

    private fun showOptions(reminderId: String) {
        val labels = arrayOf(
            "10 minutes",
            "30 minutes",
            "1 hour",
            "3 hours",
            "Tomorrow, 9:00 AM",
            "Custom time…",
        )
        AlertDialog.Builder(this, DIALOG_THEME)
            .setTitle("Snooze until")
            .setItems(labels) { _, which ->
                when (which) {
                    0 -> snoozeUntil(reminderId, inMinutes(10))
                    1 -> snoozeUntil(reminderId, inMinutes(30))
                    2 -> snoozeUntil(reminderId, inMinutes(60))
                    3 -> snoozeUntil(reminderId, inMinutes(180))
                    4 -> snoozeUntil(reminderId, tomorrowAt(hour = 9, minute = 0))
                    5 -> pickCustom(reminderId)
                }
            }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun pickCustom(reminderId: String) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            this,
            DIALOG_THEME,
            { _, year, month, day ->
                TimePickerDialog(
                    this,
                    DIALOG_THEME,
                    { _, hour, minute ->
                        val target = Calendar.getInstance().apply {
                            set(year, month, day, hour, minute, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        snoozeUntil(reminderId, Instant.fromEpochMilliseconds(target.timeInMillis))
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true,
                ).apply { setOnCancelListener { finish() } }.show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH),
        ).apply { setOnCancelListener { finish() } }.show()
    }

    private fun snoozeUntil(reminderId: String, until: Instant) {
        // Dismiss the notification the user acted on.
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(reminderId.hashCode())
        scope.launch {
            try {
                snoozeReminder(ReminderId(reminderId), until)
            } finally {
                finish()
            }
        }
    }

    private fun inMinutes(minutes: Int): Instant =
        Instant.fromEpochMilliseconds(System.currentTimeMillis() + minutes * 60_000L)

    private fun tomorrowAt(hour: Int, minute: Int): Instant {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Instant.fromEpochMilliseconds(cal.timeInMillis)
    }

    companion object {
        const val EXTRA_ID = "extra_reminder_id"
        private const val DIALOG_THEME = android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
    }
}

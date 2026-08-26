package com.swagatmali.remider.notification

import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.scheduler.ReminderScheduler

/**
 * iOS notification scheduling. Stubbed for Phase 4 (Android-first). The real
 * UNUserNotificationCenter implementation lands in the iOS phase; kept as a
 * no-op so the shared Koin graph and use cases compile and run on iOS today.
 */
class IosReminderScheduler : ReminderScheduler {

    override fun schedule(reminder: Reminder) {
        // TODO(iOS phase): build a UNTimeIntervalNotificationTrigger / calendar
        // trigger from reminder.dueDateTime + timeZone and add a UNNotificationRequest.
    }

    override fun cancel(id: ReminderId) {
        // TODO(iOS phase): removePendingNotificationRequests(withIdentifiers: [id.value]).
    }
}

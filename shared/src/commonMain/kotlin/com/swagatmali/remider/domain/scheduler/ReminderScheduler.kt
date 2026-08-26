package com.swagatmali.remider.domain.scheduler

import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId

/**
 * Platform bridge for OS-level notification scheduling. Declared in the domain
 * layer (like [com.swagatmali.remider.domain.repository.ReminderRepository]) so
 * use cases depend on this abstraction, never on AlarmManager /
 * UNUserNotificationCenter. Each platform binds its own implementation in
 * `platformModule()`.
 */
interface ReminderScheduler {

    /**
     * Schedule — or reschedule — the OS notification for [reminder]. Must be
     * idempotent per [Reminder.id]; implementations should no-op (and cancel any
     * existing alarm) for reminders that are completed, tombstoned, or past due.
     */
    fun schedule(reminder: Reminder)

    /** Cancel any pending notification for [id]. Safe to call when none exists. */
    fun cancel(id: ReminderId)
}

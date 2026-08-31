package com.swagatmali.remider.domain.usecase

import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.repository.ReminderRepository
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import kotlin.time.Clock

/**
 * Persists edits to an existing reminder. Re-validates the title, refreshes
 * [Reminder.updatedAt] so the change wins during backup merge, clears any active
 * snooze (an explicit edit of the due time should win over a prior snooze), and
 * reschedules the OS notification for the (possibly new) due time.
 */
class UpdateReminderUseCase(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(reminder: Reminder): Result<Unit> {
        val cleanTitle = reminder.title.trim()
        if (cleanTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Reminder title must not be blank"))
        }
        val updated = reminder.copy(
            title = cleanTitle,
            notes = reminder.notes?.trim()?.ifEmpty { null },
            snoozedUntil = null,
            updatedAt = clock.now(),
        )
        repository.upsert(updated)
        scheduler.schedule(updated)
        return Result.success(Unit)
    }
}

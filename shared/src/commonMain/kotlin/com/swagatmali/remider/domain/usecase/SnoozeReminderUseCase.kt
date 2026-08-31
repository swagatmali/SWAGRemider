package com.swagatmali.remider.domain.usecase

import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.repository.ReminderRepository
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Snoozes a reminder so it fires again at [snoozedUntil]. Persists the override
 * (so it survives process death / reboot — the boot re-scheduler reads it back)
 * and reschedules the OS notification for the new instant.
 *
 * The override wins over the stored due time until the reminder is edited, which
 * clears it (see [UpdateReminderUseCase]). Callers pass an absolute instant so
 * both relative snoozes ("+10 min") and absolute ones ("tomorrow 9 AM") funnel
 * through one path. [Clock] is injected for deterministic tests.
 */
class SnoozeReminderUseCase(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(id: ReminderId, snoozedUntil: Instant): Result<Unit> {
        val reminder = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("Reminder not found: ${id.value}"))
        val updated = reminder.copy(
            snoozedUntil = snoozedUntil,
            updatedAt = clock.now(),
        )
        repository.upsert(updated)
        scheduler.schedule(updated)
        return Result.success(Unit)
    }
}

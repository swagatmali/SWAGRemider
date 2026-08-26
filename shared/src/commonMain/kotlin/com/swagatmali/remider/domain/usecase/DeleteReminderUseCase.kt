package com.swagatmali.remider.domain.usecase

import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.repository.ReminderRepository
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import kotlin.time.Clock

/**
 * Soft-deletes a reminder (sets the tombstone) so the deletion is preserved for
 * the Drive backup instead of vanishing locally and resurrecting on restore, and
 * cancels any pending OS notification.
 */
class DeleteReminderUseCase(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(id: ReminderId) {
        repository.softDelete(id = id, updatedAt = clock.now())
        scheduler.cancel(id)
    }
}

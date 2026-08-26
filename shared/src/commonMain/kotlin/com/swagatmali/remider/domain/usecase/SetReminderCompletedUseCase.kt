package com.swagatmali.remider.domain.usecase

import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.repository.ReminderRepository
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import kotlin.time.Clock

/**
 * Marks a reminder complete/incomplete (the "mark completed" feature). Completing
 * cancels its pending notification; un-completing reschedules it from the stored
 * due time.
 */
class SetReminderCompletedUseCase(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(id: ReminderId, isCompleted: Boolean) {
        repository.setCompleted(id = id, isCompleted = isCompleted, updatedAt = clock.now())
        if (isCompleted) {
            scheduler.cancel(id)
        } else {
            repository.getById(id)?.let { scheduler.schedule(it) }
        }
    }
}

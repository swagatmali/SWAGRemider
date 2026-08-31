package com.swagatmali.remider.domain.usecase

import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.repository.ReminderRepository
import com.swagatmali.remider.domain.scheduler.ReminderScheduler

/**
 * Re-arms the OS alarm for a single reminder from its current persisted state.
 *
 * Used by the alarm receiver after a *recurring* reminder fires: reloading from
 * the repository is the source of truth (so a reminder the user marked done
 * between firings is not re-armed), and [ReminderScheduler.schedule] computes the
 * next future occurrence — honouring the repeat cadence and end bound.
 */
class RescheduleReminderUseCase(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend operator fun invoke(id: ReminderId) {
        repository.getById(id)?.let { scheduler.schedule(it) }
    }
}

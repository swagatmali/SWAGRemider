package com.swagatmali.remider.domain.usecase

import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.repository.ReminderRepository

/** Loads a single reminder, e.g. to populate the edit screen. */
class GetReminderByIdUseCase(
    private val repository: ReminderRepository,
) {
    suspend operator fun invoke(id: ReminderId): Reminder? = repository.getById(id)
}

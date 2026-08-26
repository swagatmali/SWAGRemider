package com.swagatmali.remider.domain.usecase

import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

/** Streams the active reminder list for the UI to render. */
class GetRemindersUseCase(
    private val repository: ReminderRepository,
) {
    operator fun invoke(): Flow<List<Reminder>> = repository.observeReminders()
}

package com.swagatmali.remider.domain.usecase

import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.repository.ReminderRepository
import com.swagatmali.remider.domain.scheduler.ReminderScheduler
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Creates a new reminder: validates the title, mints a client UUID, and stamps
 * matching created/updated instants. On success the OS notification is scheduled
 * via [ReminderScheduler]. [Clock] is injected for deterministic tests.
 */
class CreateReminderUseCase(
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(
        title: String,
        notes: String?,
        dueDateTime: LocalDateTime,
        timeZone: TimeZone,
    ): Result<ReminderId> {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("Reminder title must not be blank"))
        }
        val now = clock.now()
        val reminder = Reminder(
            id = ReminderId(Uuid.random().toString()),
            title = cleanTitle,
            notes = notes?.trim()?.ifEmpty { null },
            dueDateTime = dueDateTime,
            timeZone = timeZone,
            isCompleted = false,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
        )
        repository.upsert(reminder)
        scheduler.schedule(reminder)
        return Result.success(reminder.id)
    }
}

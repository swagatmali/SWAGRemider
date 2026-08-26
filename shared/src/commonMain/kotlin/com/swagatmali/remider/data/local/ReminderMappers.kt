package com.swagatmali.remider.data.local

import com.swagatmali.remider.db.ReminderEntity
import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/**
 * Maps a persisted [ReminderEntity] row to the domain [Reminder].
 * ISO-8601 text columns are parsed back into their typed forms here, so the
 * domain layer never sees a raw string.
 */
internal fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = ReminderId(id),
    title = title,
    notes = notes,
    dueDateTime = LocalDateTime.parse(dueDateTime),
    timeZone = TimeZone.of(timeZone),
    isCompleted = isCompleted != 0L,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt),
    isDeleted = isDeleted != 0L,
)

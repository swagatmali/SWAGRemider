package com.swagatmali.remider.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.swagatmali.remider.db.ReminderDatabase
import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId
import com.swagatmali.remider.domain.repository.ReminderRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant

/**
 * SQLDelight-backed [ReminderRepository]. Reads are reactive via SQLDelight's
 * coroutine extensions; writes hop onto [dispatcher] (defaults to Default —
 * commonMain has no Dispatchers.IO; this can be refined per platform later).
 */
class ReminderRepositoryImpl(
    database: ReminderDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ReminderRepository {

    private val queries = database.reminderQueries

    override fun observeReminders(): Flow<List<Reminder>> =
        queries.selectAllActive()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: ReminderId): Reminder? = withContext(dispatcher) {
        queries.selectById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun upsert(reminder: Reminder): Unit = withContext(dispatcher) {
        queries.upsert(
            id = reminder.id.value,
            title = reminder.title,
            notes = reminder.notes,
            dueDateTime = reminder.dueDateTime.toString(),
            timeZone = reminder.timeZone.id,
            isCompleted = if (reminder.isCompleted) 1L else 0L,
            createdAt = reminder.createdAt.toString(),
            updatedAt = reminder.updatedAt.toString(),
            isDeleted = if (reminder.isDeleted) 1L else 0L,
            snoozedUntil = reminder.snoozedUntil?.toString(),
            repeatInterval = if (reminder.repeat.repeats) reminder.repeat.name else null,
            repeatUntil = reminder.repeatUntil?.toString(),
        )
    }

    override suspend fun setCompleted(
        id: ReminderId,
        isCompleted: Boolean,
        updatedAt: Instant,
    ): Unit = withContext(dispatcher) {
        queries.setCompleted(
            isCompleted = if (isCompleted) 1L else 0L,
            updatedAt = updatedAt.toString(),
            id = id.value,
        )
    }

    override suspend fun softDelete(id: ReminderId, updatedAt: Instant): Unit =
        withContext(dispatcher) {
            queries.softDelete(updatedAt = updatedAt.toString(), id = id.value)
        }
}

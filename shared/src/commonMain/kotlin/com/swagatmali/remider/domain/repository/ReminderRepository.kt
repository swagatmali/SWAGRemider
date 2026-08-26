package com.swagatmali.remider.domain.repository

import com.swagatmali.remider.domain.model.Reminder
import com.swagatmali.remider.domain.model.ReminderId
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/**
 * Persistence boundary for reminders. Declared in the domain layer so use cases
 * depend on this abstraction, never on SQLDelight. The concrete implementation
 * lives in the data layer.
 *
 * Reads expose only active (non-deleted) rows; deletion is a soft tombstone so
 * the row survives for the next backup sync.
 */
interface ReminderRepository {

    /** Reactive stream of active reminders, ordered by due time. Emits on every change. */
    fun observeReminders(): Flow<List<Reminder>>

    /** One-shot lookup. Returns null if the id is unknown or the row is tombstoned. */
    suspend fun getById(id: ReminderId): Reminder?

    /** Insert or replace. Callers own [Reminder.updatedAt]. */
    suspend fun upsert(reminder: Reminder)

    /** Flip completion and bump [updatedAt] atomically. */
    suspend fun setCompleted(id: ReminderId, isCompleted: Boolean, updatedAt: Instant)

    /** Soft-delete: set the tombstone and bump [updatedAt] so the change syncs. */
    suspend fun softDelete(id: ReminderId, updatedAt: Instant)
}

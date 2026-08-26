package com.swagatmali.remider.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.jvm.JvmInline
import kotlin.time.Instant

/**
 * Stable, client-generated identity for a reminder.
 *
 * IDs are UUID strings minted on-device so records can be created offline and
 * merged with the Google Drive backup across devices without key collisions.
 */
@JvmInline
value class ReminderId(val value: String)

/**
 * Core domain entity. Pure Kotlin — no Android/iOS framework types — so the
 * whole model compiles unchanged for every target.
 *
 * Time is stored as the user's intended wall-clock [dueDateTime] together with
 * the [timeZone] captured at creation. The absolute firing [Instant] is derived
 * at schedule time (Phase 4), which keeps "9 AM means 9 AM" and stays correct
 * across DST changes.
 */
data class Reminder(
    val id: ReminderId,
    val title: String,
    val notes: String? = null,
    val dueDateTime: LocalDateTime,
    val timeZone: TimeZone,
    val isCompleted: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    /** Soft-delete tombstone: kept locally so deletions propagate to the Drive backup. */
    val isDeleted: Boolean = false,
)

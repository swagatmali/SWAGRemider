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
    /**
     * Optional one-off override of the firing time, set by "snooze". When
     * non-null the scheduler fires at this absolute instant instead of deriving
     * one from [dueDateTime]; it is cleared when the reminder is edited. Persisted
     * so a snooze survives process death and device reboot.
     */
    val snoozedUntil: Instant? = null,
    /**
     * Repeat cadence. [RepeatInterval.NONE] (default) = fires once. When it
     * repeats, each firing arms the next occurrence until [repeatUntil] passes or
     * the reminder is marked completed — whichever comes first.
     */
    val repeat: RepeatInterval = RepeatInterval.NONE,
    /**
     * Optional end bound for a repeating reminder: the series stops once the next
     * occurrence would fall after this instant. Null = no time limit (repeats
     * until marked done). Ignored when [repeat] is [RepeatInterval.NONE].
     */
    val repeatUntil: Instant? = null,
    val isCompleted: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    /** Soft-delete tombstone: kept locally so deletions propagate to the Drive backup. */
    val isDeleted: Boolean = false,
)

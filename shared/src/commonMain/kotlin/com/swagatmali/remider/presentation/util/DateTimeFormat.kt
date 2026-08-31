package com.swagatmali.remider.presentation.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import com.swagatmali.remider.domain.model.RepeatInterval

/**
 * Formats a due date-time as "YYYY-MM-DD · HH:mm". Derived from the ISO-8601
 * string rather than field accessors (year/monthNumber/…) so it doesn't break
 * across kotlinx-datetime versions that rename or remove those accessors.
 */
fun formatDueDateTime(dateTime: LocalDateTime): String {
    val iso = dateTime.toString()               // e.g. "2026-08-26T14:30"
    val datePart = iso.substringBefore('T')
    val timePart = iso.substringAfter('T').take(5) // "HH:mm"
    return "$datePart · $timePart"
}

/** "HH:mm" for a time-only value. */
fun formatTime(time: LocalTime): String = time.toString().take(5)

/** Short label for a repeat cadence, shown on chips and in the list row. */
fun RepeatInterval.label(): String = when (this) {
    RepeatInterval.NONE -> "Does not repeat"
    RepeatInterval.MINUTES_15 -> "Every 15 min"
    RepeatInterval.MINUTES_30 -> "Every 30 min"
    RepeatInterval.HOURLY -> "Hourly"
    RepeatInterval.DAILY -> "Daily"
    RepeatInterval.WEEKLY -> "Weekly"
}

package com.swagatmali.remider.presentation.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

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

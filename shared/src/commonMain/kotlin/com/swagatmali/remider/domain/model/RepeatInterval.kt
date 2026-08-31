package com.swagatmali.remider.domain.model

/**
 * Fixed set of repeat cadences a reminder can use. Presets only (no free-form
 * "every N units") — this keeps the create form simple and the scheduling math
 * a single multiply.
 *
 * [stepMillis] is the gap between occurrences. [NONE] means the reminder fires
 * exactly once (the historical behaviour) and has a zero step. DAILY/WEEKLY use
 * fixed 24h/7d spans; across a DST boundary the wall-clock time can shift by an
 * hour, which is an acceptable trade for preset simplicity.
 */
enum class RepeatInterval(val stepMillis: Long) {
    NONE(0L),
    MINUTES_15(15L * 60_000L),
    MINUTES_30(30L * 60_000L),
    HOURLY(60L * 60_000L),
    DAILY(24L * 60L * 60_000L),
    WEEKLY(7L * 24L * 60L * 60_000L);

    val repeats: Boolean get() = this != NONE
}

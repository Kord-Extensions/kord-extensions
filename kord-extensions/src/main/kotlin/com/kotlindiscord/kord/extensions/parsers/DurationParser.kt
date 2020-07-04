package com.kotlindiscord.kord.extensions.parsers

import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Mapping character to its actual unit.
 */
val unitMap = mapOf(
    "s" to ChronoUnit.SECONDS,
    "m" to ChronoUnit.MINUTES,
    "h" to ChronoUnit.HOURS,
    "d" to ChronoUnit.DAYS,
    "w" to ChronoUnit.WEEKS,
    "M" to ChronoUnit.MONTHS,
    "y" to ChronoUnit.YEARS
)

/**
 * Parse the provided string to a [Duration] object.
 * Units are determined as in [unitMap].
 *
 * @param s the string to parse.
 */
@Suppress("MagicNumber")
fun parseDuration(s: String): Duration {
    var buf = 0L
    var unit: ChronoUnit? = null
    val duration = Duration.ofSeconds(0)

    for (c in s) {
        if (c.isDigit()) {
            buf = buf * 10 + c.toLong()
        } else {
            if (unit != null) {
                duration.plus(buf, unit)
            }

            buf = 0L
            unit = unitMap[c.toString()]
        }
    }

    return duration
}

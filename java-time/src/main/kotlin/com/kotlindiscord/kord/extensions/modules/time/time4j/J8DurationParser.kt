package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.utils.splitOn
import java.time.temporal.ChronoUnit

/**
 * Mapping character to its actual unit.
 */
public val unitMapJ8: Map<String, ChronoUnit> = mapOf(
    "s" to ChronoUnit.SECONDS,
    "sec" to ChronoUnit.SECONDS,
    "second" to ChronoUnit.SECONDS,
    "seconds" to ChronoUnit.SECONDS,

    "m" to ChronoUnit.MINUTES,  // It's what everyone expects
    "mi" to ChronoUnit.MINUTES,
    "min" to ChronoUnit.MINUTES,
    "minute" to ChronoUnit.MINUTES,
    "minutes" to ChronoUnit.MINUTES,

    "h" to ChronoUnit.HOURS,
    "hour" to ChronoUnit.HOURS,
    "hours" to ChronoUnit.HOURS,

    "d" to ChronoUnit.DAYS,
    "day" to ChronoUnit.DAYS,
    "days" to ChronoUnit.DAYS,

    "w" to ChronoUnit.WEEKS,
    "week" to ChronoUnit.WEEKS,
    "weeks" to ChronoUnit.WEEKS,

    "mo" to ChronoUnit.MONTHS,
    "month" to ChronoUnit.MONTHS,
    "months" to ChronoUnit.MONTHS,

    "y" to ChronoUnit.YEARS,
    "year" to ChronoUnit.YEARS,
    "years" to ChronoUnit.YEARS
)

/**
 * Parse the provided string to a [java.time.Duration] object.
 * Units are determined as in [unitMapJ8].
 *
 * @param s the string to parse.
 */
@Suppress("MagicNumber")
internal fun parseDurationJ8(s: String): java.time.Duration {
    var buffer = s.replace(",", "")
    var duration = java.time.Duration.ZERO

    while (buffer.isNotEmpty()) {
        val r1 = buffer.splitOn { it.isLetter() } // Thanks Kotlin : https://youtrack.jetbrains.com/issue/KT-11362
        val num = r1.first.toLong()
        buffer = r1.second

        val r2 = buffer.splitOn { it.isDigit() || it == '-' }
        val unit = r2.first
        buffer = r2.second

        val chronoUnit = unitMapJ8[unit.toLowerCase()] ?: throw InvalidTimeUnitException(unit.toLowerCase())

        duration = if (chronoUnit.duration.seconds > ChronoUnit.SECONDS.duration.seconds) {
            if (num >= 0) {
                duration.plus(num * chronoUnit.duration.seconds, ChronoUnit.SECONDS)
            } else {
                duration.plus(num * chronoUnit.duration.seconds, ChronoUnit.SECONDS)
            }
        } else {
            if (num >= 0) {
                duration.plus(num, chronoUnit)
            } else {
                duration.minus(num, chronoUnit)
            }
        }
    }

    return duration
}

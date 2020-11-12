package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.utils.splitOn
import net.time4j.CalendarUnit
import net.time4j.ClockUnit
import net.time4j.Duration
import net.time4j.IsoUnit
import java.time.temporal.ChronoUnit

/**
 * Mapping character to its actual unit.
 */
val unitMap: Map<String, IsoUnit> = mapOf(
    "s" to ClockUnit.SECONDS,
    "sec" to ClockUnit.SECONDS,
    "second" to ClockUnit.SECONDS,
    "seconds" to ClockUnit.SECONDS,

    "m" to ClockUnit.MINUTES,  // It's what everyone expects
    "mi" to ClockUnit.MINUTES,
    "min" to ClockUnit.MINUTES,
    "minute" to ClockUnit.MINUTES,
    "minutes" to ClockUnit.MINUTES,

    "h" to ClockUnit.HOURS,
    "hour" to ClockUnit.HOURS,
    "hours" to ClockUnit.HOURS,

    "d" to CalendarUnit.DAYS,
    "day" to CalendarUnit.DAYS,
    "days" to CalendarUnit.DAYS,

    "w" to CalendarUnit.WEEKS,
    "week" to CalendarUnit.WEEKS,
    "weeks" to CalendarUnit.WEEKS,

    "mo" to CalendarUnit.MONTHS,
    "month" to CalendarUnit.MONTHS,
    "months" to CalendarUnit.MONTHS,

    "y" to CalendarUnit.YEARS,
    "year" to CalendarUnit.YEARS,
    "years" to CalendarUnit.YEARS
)
/**
 * Mapping character to its actual unit.
 */
val unitMapJ8: Map<String, ChronoUnit> = mapOf(
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
 * Parse the provided string to a [Duration] object.
 * Units are determined as in [unitMap].
 *
 * @param s the string to parse.
 */
@Suppress("MagicNumber")
fun parseDuration(s: String): Duration<IsoUnit> {
    var buffer = s
    var duration = Duration.ofZero<IsoUnit>()

    while (buffer.isNotEmpty()) {
        val r1 = buffer.splitOn { it.isLetter() } // Thanks Kotlin : https://youtrack.jetbrains.com/issue/KT-11362
        val num = r1.first
        buffer = r1.second

        val r2 = buffer.splitOn { it.isDigit() }
        val unit = r2.first
        buffer = r2.second

        val chronoUnit = unitMap[unit.toLowerCase()] ?: throw InvalidTimeUnitException(unit.toLowerCase())

        duration = duration.plus(num.toLong(), chronoUnit)
    }

    return duration
}

/**
 * Parse the provided string to a [java.time.Duration] object.
 * Units are determined as in [unitMapJ8].
 *
 * @param s the string to parse.
 */
@Suppress("MagicNumber")
fun parseDurationJ8(s: String): java.time.Duration {
    var buffer = s
    var duration = java.time.Duration.ZERO

    while (buffer.isNotEmpty()) {
        val r1 = buffer.splitOn { it.isLetter() } // Thanks Kotlin : https://youtrack.jetbrains.com/issue/KT-11362
        val num = r1.first
        buffer = r1.second

        val r2 = buffer.splitOn { it.isDigit() }
        val unit = r2.first
        buffer = r2.second

        val chronoUnit = unitMapJ8[unit.toLowerCase()] ?: throw InvalidTimeUnitException(unit.toLowerCase())

        duration = if (chronoUnit.duration.seconds > ChronoUnit.SECONDS.duration.seconds) {
            duration.plus(num.toLong() * chronoUnit.duration.seconds, ChronoUnit.SECONDS)
        } else {
            duration.plus(num.toLong(), chronoUnit)
        }
    }

    return duration
}

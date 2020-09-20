package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.splitOn
import net.time4j.CalendarUnit
import net.time4j.ClockUnit
import net.time4j.Duration
import net.time4j.IsoUnit

/**
 * Mapping character to its actual unit.
 */
val unitMap: Map<String, IsoUnit> = mapOf(
    "s" to ClockUnit.SECONDS,
    "sec" to ClockUnit.SECONDS,
    "second" to ClockUnit.SECONDS,
    "seconds" to ClockUnit.SECONDS,

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

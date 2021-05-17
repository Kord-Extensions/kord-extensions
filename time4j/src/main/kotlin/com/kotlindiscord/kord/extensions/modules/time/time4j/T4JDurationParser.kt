package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.utils.splitOn
import net.time4j.CalendarUnit
import net.time4j.ClockUnit
import net.time4j.Duration
import net.time4j.IsoUnit

/**
 * Mapping character to its actual unit.
 */
public val unitMap: Map<String, IsoUnit> = mapOf(
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
 * Parse the provided string to a Time4J [Duration] object.
 * Units are determined as in [unitMap].
 *
 * @param s the string to parse.
 */
@Suppress("MagicNumber")
public fun parseT4JDuration(s: String): Duration<IsoUnit> {
    var buffer = s.replace(",", "")
    var duration = Duration.ofZero<IsoUnit>()

    while (buffer.isNotEmpty()) {
        val r1 = buffer.splitOn { it.isLetter() } // Thanks Kotlin : https://youtrack.jetbrains.com/issue/KT-11362
        val num = r1.first
        buffer = r1.second

        val r2 = buffer.splitOn { it.isDigit() || it == '-' }
        val unit = r2.first
        buffer = r2.second

        val chronoUnit = unitMap[unit.toLowerCase()] ?: throw InvalidTimeUnitException(unit.toLowerCase())

        duration = duration.plus(num.toLong(), chronoUnit)
    }

    return duration
}

package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.splitOn
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.NoSuchElementException

/**
 * Mapping character to its actual unit.
 */
val unitMap = mapOf(
    "s" to ChronoUnit.SECONDS,
    "sec" to ChronoUnit.SECONDS,
    "second" to ChronoUnit.SECONDS,
    "seconds" to ChronoUnit.SECONDS,

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
fun parseDuration(s: String): Duration {
    var buffer = s
    var duration = Duration.ofSeconds(0)

    while (buffer.isNotEmpty()) {
        val r1 = buffer.splitOn { it.isLetter() } // Thanks Kotlin : https://youtrack.jetbrains.com/issue/KT-11362
        val num = r1.first
        buffer = r1.second

        val r2 = buffer.splitOn { it.isDigit() }
        val unit = r2.first
        buffer = r2.second
        
        val chronoUnit: ChronoUnit
        try {
            chronoUnit = unitMap.getValue(unit.toLowerCase())
        } catch (e: NoSuchElementException) {
            throw InvalidTimeUnitException(unit.toLowerCase())
        }
        duration = duration.plus(num.toLong(), chronoUnit)
    }

    return duration
}

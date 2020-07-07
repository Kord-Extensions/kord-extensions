package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.splitOn
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
    var buffer = s
    var duration = Duration.ofSeconds(0)

    while (buffer.isNotEmpty()) {
        val r1 = buffer.splitOn { it.isLetter() } // Thanks Kotlin : https://youtrack.jetbrains.com/issue/KT-11362
        val num = r1.first
        buffer = r1.second

        val r2 = buffer.splitOn { it.isDigit() }
        val unit = r2.first
        buffer = r2.second

        val chronoUnit = unitMap[unit]
        duration = duration.plus(num.toLong(), chronoUnit)
    }

    return duration
}

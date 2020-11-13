package com.kotlindiscord.kord.extensions.utils

import net.time4j.Duration
import net.time4j.IsoUnit
import java.time.temporal.ChronoUnit

/**
 * Convert a Time4J Duration object to seconds.
 *
 * @return The duration object folded into a single Long, representing total seconds.
 */
fun Duration<IsoUnit>.toSeconds(): Long {
    val amount = this.toTemporalAmount()
    var seconds = 0L

    seconds += amount.get(ChronoUnit.MILLENNIA) * ChronoUnit.MILLENNIA.duration.seconds
    seconds += amount.get(ChronoUnit.CENTURIES) * ChronoUnit.CENTURIES.duration.seconds
    seconds += amount.get(ChronoUnit.DECADES) * ChronoUnit.DECADES.duration.seconds
    seconds += amount.get(ChronoUnit.YEARS) * ChronoUnit.YEARS.duration.seconds
    seconds += amount.get(ChronoUnit.MONTHS) * ChronoUnit.MONTHS.duration.seconds
    seconds += amount.get(ChronoUnit.WEEKS) * ChronoUnit.WEEKS.duration.seconds
    seconds += amount.get(ChronoUnit.DAYS) * ChronoUnit.DAYS.duration.seconds
    seconds += amount.get(ChronoUnit.HOURS) * ChronoUnit.HOURS.duration.seconds
    seconds += amount.get(ChronoUnit.MINUTES) * ChronoUnit.MINUTES.duration.seconds
    seconds += amount.get(ChronoUnit.SECONDS)

    return seconds
}

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
@Suppress("MagicNumber")  // These are all time units!
fun java.time.Duration.toHuman(): String? {
    val parts = mutableListOf<String>()

    val seconds = this.seconds % 60
    val minutesTotal = this.seconds / 60

    val minutes = minutesTotal % 60
    val hoursTotal = minutesTotal / 60

    val hours = hoursTotal % 24
    val days = hoursTotal / 24

    if (days > 0) {
        parts.add(
            "$days " + if (days > 1) "days" else "day"
        )
    }

    if (hours > 0) {
        parts.add(
            "$hours " + if (hours > 1) "hours" else "hour"
        )
    }

    if (minutes > 0) {
        parts.add(
            "$minutes " + if (minutes > 1) "minutes" else "minute"
        )
    }

    if (seconds > 0) {
        parts.add(
            "$seconds " + if (seconds > 1) "seconds" else "second"
        )
    }

    if (parts.isEmpty()) return null

    var output = ""

    parts.forEachIndexed { i, part ->
        if (i == parts.size - 1 && i > 0) {
            output += " and "  // About to output the last part, and it's not the only one
        } else if (i < parts.size - 1 && i > 0) {
            output += ", "  // Not the last part, but not the first one either
        }

        output += part
    }

    // I have no idea how I should _actually_ do this...
    return parts.joinToString(", ").reversed().replaceFirst(",", "dna ").reversed()
}

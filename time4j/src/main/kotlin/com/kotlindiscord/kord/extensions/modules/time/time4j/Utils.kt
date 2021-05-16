package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.modules.time.time4j.formatters.T4JDurationFormatter
import net.time4j.Duration
import net.time4j.IsoUnit
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Convert a Time4J Duration object to seconds.
 *
 * @return The duration object folded into a single Long, representing total seconds.
 */
public fun Duration<IsoUnit>.toSeconds(): Long {
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
public fun Duration<IsoUnit>.toHuman(locale: Locale): String? = T4JDurationFormatter.format(this, locale)

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
public suspend fun Duration<IsoUnit>.toHuman(context: CommandContext): String? = toHuman(context.getLocale())

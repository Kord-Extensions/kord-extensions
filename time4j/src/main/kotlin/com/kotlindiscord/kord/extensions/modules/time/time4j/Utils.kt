package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.ibm.icu.text.MeasureFormat
import com.ibm.icu.util.Measure
import com.ibm.icu.util.MeasureUnit
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.utils.component6
import net.time4j.*
import java.lang.IllegalStateException
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.jvm.Throws

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
 * Function in charge of formatting Time4J duration objects into human-readable form, taking locales and translations
 * into account.
 */
@Throws(IllegalStateException::class)
public fun formatT4JDuration(duration: Duration<IsoUnit>, locale: Locale): String? {
    // This function is pretty cursed, but then again, Time4J is pretty cursed.
    val formatter = Duration.Formatter.ofPattern("#################Y::#M::#D::#h::#m::#s")

    val now = PlainTimestamp.nowInSystemTime()
    val offsetTime = duration.addTo(PlainTimestamp.nowInSystemTime())

    val newDuration = Duration.`in`(
        CalendarUnit.YEARS,
        CalendarUnit.MONTHS,
        CalendarUnit.DAYS,
        ClockUnit.HOURS,
        ClockUnit.MINUTES,
        ClockUnit.SECONDS
    ).between(now, offsetTime).toTemporalAmount()

    val times = formatter.format(newDuration).split("::")
    val (years, months, days, hours, minutes, seconds) = times.map { it.toLong() }

    val fmt = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.WIDE)
    val measures: MutableList<Measure> = mutableListOf()

    if (years > 0) measures.add(Measure(years, MeasureUnit.YEAR))
    if (months > 0) measures.add(Measure(months, MeasureUnit.MONTH))
    if (days > 0) measures.add(Measure(days, MeasureUnit.DAY))
    if (hours > 0) measures.add(Measure(hours, MeasureUnit.HOUR))
    if (minutes > 0) measures.add(Measure(minutes, MeasureUnit.MINUTE))
    if (seconds > 0) measures.add(Measure(seconds, MeasureUnit.SECOND))

    if (measures.isEmpty()) return null

    @Suppress("SpreadOperator")  // There's no other way, really
    return fmt.formatMeasures(*measures.toTypedArray())
}

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
public fun Duration<IsoUnit>.toHuman(locale: Locale): String? = formatT4JDuration(this, locale)

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
public suspend fun Duration<IsoUnit>.toHuman(context: CommandContext): String? = toHuman(context.getLocale())

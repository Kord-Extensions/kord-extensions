package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.ibm.icu.text.MeasureFormat
import com.ibm.icu.util.Measure
import com.ibm.icu.util.MeasureUnit
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.utils.component6
import net.time4j.*
import java.util.*

private const val DAYS_PER_WEEK = 7L

/**
 * Function in charge of formatting Time4J duration objects into human-readable form, taking locales and translations
 * into account.
 */
@Throws(IllegalStateException::class)
public fun formatT4JDuration(
    duration: Duration<IsoUnit>,
    locale: Locale,
    relativeTo: PlainTimestamp = PlainTimestamp.nowInSystemTime()
): String? {
    // This function is pretty cursed, but then again, Time4J is pretty cursed.
    val formatter = Duration.Formatter.ofPattern("#################Y::#M::#D::#h::#m::#s")
    val offsetTime = duration.addTo(PlainTimestamp.nowInSystemTime())

    val newDuration = Duration.`in`(
        CalendarUnit.YEARS,
        CalendarUnit.MONTHS,
        CalendarUnit.DAYS,
        ClockUnit.HOURS,
        ClockUnit.MINUTES,
        ClockUnit.SECONDS
    ).between(relativeTo, offsetTime).toTemporalAmount()

    val times = formatter.format(newDuration).split("::")
    val (years, months, daysTotal, hours, minutes, seconds) = times.map { it.toLong() }

    val days = daysTotal % DAYS_PER_WEEK
    val weeks = daysTotal / DAYS_PER_WEEK

    val fmt = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.WIDE)
    val measures: MutableList<Measure> = mutableListOf()

    if (years != 0L) measures.add(Measure(years, MeasureUnit.YEAR))
    if (months != 0L) measures.add(Measure(months, MeasureUnit.MONTH))
    if (weeks != 0L) measures.add(Measure(weeks, MeasureUnit.WEEK))
    if (days != 0L) measures.add(Measure(days, MeasureUnit.DAY))
    if (hours != 0L) measures.add(Measure(hours, MeasureUnit.HOUR))
    if (minutes != 0L) measures.add(Measure(minutes, MeasureUnit.MINUTE))
    if (seconds != 0L) measures.add(Measure(seconds, MeasureUnit.SECOND))

    if (measures.isEmpty()) return null

    @Suppress("SpreadOperator")  // There's no other way, really
    return fmt.formatMeasures(*measures.toTypedArray())
}

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
public fun Duration<IsoUnit>.toHuman(
    locale: Locale,
    relativeTo: PlainTimestamp = PlainTimestamp.nowInSystemTime()
): String? = formatT4JDuration(this, locale, relativeTo)

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
public suspend fun Duration<IsoUnit>.toHuman(
    context: CommandContext,
    relativeTo: PlainTimestamp = PlainTimestamp.nowInSystemTime()
): String? = toHuman(context.getLocale(), relativeTo)

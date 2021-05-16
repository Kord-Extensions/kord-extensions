package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.ibm.icu.text.MeasureFormat
import com.ibm.icu.util.Measure
import com.ibm.icu.util.MeasureUnit
import com.kotlindiscord.kord.extensions.commands.CommandContext
import org.apache.commons.lang3.time.DurationFormatUtils
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Function in charge of formatting Java Time duration objects into human-readable form, taking locales and
 * translations into account.
 */
public fun formatJ8Duration(duration: Duration, locale: Locale): String? {
    // This is only slightly less cursed than Time4J.
    val times = DurationFormatUtils.formatPeriod(
        Instant.now().toEpochMilli(),
        Instant.now().toEpochMilli() + duration.toMillis(),
        "y::M::d::H::m::s"
    ).split("::").toMutableList()

    val years = times.removeFirst().toLong()
    val months = times.removeFirst().toLong()
    val days = times.removeFirst().toLong()
    val hours = times.removeFirst().toLong()
    val minutes = times.removeFirst().toLong()
    val seconds = times.removeFirst().toLong()

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
@Suppress("MagicNumber")  // These are all time units!
public fun Duration.toHuman(locale: Locale): String? = formatJ8Duration(this, locale)

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
@Suppress("MagicNumber")  // These are all time units!
public suspend fun Duration.toHuman(context: CommandContext): String? = toHuman(context.getLocale())

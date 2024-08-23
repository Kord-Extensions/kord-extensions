/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.time4j

import com.ibm.icu.text.MeasureFormat
import com.ibm.icu.util.Measure
import com.ibm.icu.util.MeasureUnit
import dev.kordex.core.builders.AboutBuilder
import dev.kordex.core.builders.about.CopyrightType
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.time.TimestampType
import dev.kordex.core.utils.component6
import net.time4j.*
import net.time4j.engine.StartOfDay
import net.time4j.tz.ZonalOffset
import java.util.*

private const val DAYS_PER_WEEK = 7L

private var copyrightAdded = false

internal fun AboutBuilder.addCopyright() {
	if (!copyrightAdded) {
		copyright(
			"Time4J",
			"LGPL-2.1",
			CopyrightType.Library,
			"http://time4j.net/"
		)
	}

	copyrightAdded = true
}

/**
 * Function in charge of formatting Time4J duration objects into human-readable form, taking locales and translations
 * into account.
 */
@Suppress("DestructuringDeclarationWithTooManyEntries")
@Throws(IllegalStateException::class)
public fun formatT4JDuration(
	duration: Duration<IsoUnit>,
	locale: Locale,
	relativeTo: PlainTimestamp = PlainTimestamp.nowInSystemTime(),
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
	relativeTo: PlainTimestamp = PlainTimestamp.nowInSystemTime(),
): String? = formatT4JDuration(this, locale, relativeTo)

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
public suspend fun Duration<IsoUnit>.toHuman(
    context: CommandContext,
    relativeTo: PlainTimestamp = PlainTimestamp.nowInSystemTime(),
): String? = toHuman(context.getLocale(), relativeTo)

/**
 * Format the given `Moment` to Discord's automatically-formatted timestamp format. This will return a String that
 * you can include in your messages, which Discord should automatically format for users based on their locale.
 */
public fun Moment.toDiscord(format: TimestampType = TimestampType.Default): String =
	format.format(posixTime)

/**
 * Format the given `PlainTimestamp` to Discord's automatically-formatted timestamp format. This will return a String
 * that you can include in your messages, which Discord should automatically format for users based on their locale.
 *
 * This will get the timestamp's `Moment` at UTC. If this isn't what you want, use the
 * `PlainTimestamp#at(ZonalOffset)` function and call `.toDiscord(format)` with the result..
 */
public fun PlainTimestamp.toDiscord(format: TimestampType = TimestampType.Default): String =
	atUTC().toDiscord(format)

/**
 * Format the given `PlainTimestamp` to Discord's automatically-formatted timestamp format. This will return a String
 * that you can include in your messages, which Discord should automatically format for users based on their locale.
 *
 * This will get the timestamp's `Moment` at UTC, with the start of day set to `MIDNIGHT`. If this isn't what you
 * want, use the `GeneralTimestamp#at(ZonalOffset, StartOfDay)` function and call `.toDiscord(format)` with the
 * result.
 */
public fun GeneralTimestamp<*>.toDiscord(format: TimestampType = TimestampType.Default): String =
	at(ZonalOffset.UTC, StartOfDay.MIDNIGHT).toDiscord(format)

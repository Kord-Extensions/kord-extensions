/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.java.time

import com.ibm.icu.text.MeasureFormat
import com.ibm.icu.util.Measure
import com.ibm.icu.util.MeasureUnit
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.time.TimestampType
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

private const val DAYS_PER_WEEK = 7L

/**
 * Function in charge of formatting [ChronoContainer] duration objects into human-readable form, taking locales and
 * translations into account.
 */
@Throws(IllegalArgumentException::class)
public fun formatChronoContainer(
	container: ChronoContainer,
	locale: Locale,
	relativeTo: LocalDateTime = LocalDateTime.now(),
): String? {
	container.normalize(relativeTo)

	val years = container.get(ChronoUnit.YEARS)
	val months = container.get(ChronoUnit.MONTHS)
	val days = container.get(ChronoUnit.DAYS) % DAYS_PER_WEEK
	val weeks = container.get(ChronoUnit.DAYS) / DAYS_PER_WEEK

	val hours = container.get(ChronoUnit.HOURS)
	val minutes = container.get(ChronoUnit.MINUTES)
	val seconds = container.get(ChronoUnit.SECONDS)

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
public fun ChronoContainer.toHuman(
	locale: Locale,
	relativeTo: LocalDateTime = LocalDateTime.now(),
): String? = formatChronoContainer(this, locale, relativeTo)

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
public suspend fun ChronoContainer.toHuman(
	context: CommandContext,
	relativeTo: LocalDateTime = LocalDateTime.now(),
): String? = toHuman(context.getLocale(), relativeTo)

/**
 * Format the given `Instant` to Discord's automatically-formatted timestamp format. This will return a String that
 * you can include in your messages, which Discord should automatically format for users based on their locale.
 */
public fun Instant.toDiscord(format: TimestampType = TimestampType.Default): String =
	format.format(epochSecond)

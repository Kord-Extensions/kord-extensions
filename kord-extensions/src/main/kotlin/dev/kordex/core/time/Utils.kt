/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant

/**
 * Format the given `Instant` to Discord's automatically-formatted timestamp format. This will return a String that
 * you can include in your messages, which Discord should automatically format for users based on their locale.
 */
public fun Instant.toDiscord(format: TimestampType): String = format.format(epochSeconds)

/** Retrieve the [DateTimeUnit] for the given pluralized English name. **/
public fun namedDateTimeUnit(name: String): DateTimeUnit = when (name) {
	"nanoseconds" -> DateTimeUnit.NANOSECOND
	"microseconds" -> DateTimeUnit.MICROSECOND
	"milliseconds" -> DateTimeUnit.MILLISECOND
	"seconds" -> DateTimeUnit.SECOND
	"minutes" -> DateTimeUnit.MINUTE
	"hours" -> DateTimeUnit.HOUR
	"days" -> DateTimeUnit.DAY
	"weeks" -> DateTimeUnit.WEEK
	"months" -> DateTimeUnit.MONTH
	"quarters" -> DateTimeUnit.QUARTER
	"years" -> DateTimeUnit.YEAR
	"centuries" -> DateTimeUnit.CENTURY

	else -> error("Unsupported unit name: $name")
}

/** Retrieve the pluralized English name for a given [DateTimeUnit]. **/
public val DateTimeUnit.name: String
	get() = when (this) {
		DateTimeUnit.NANOSECOND -> "nanoseconds"
		DateTimeUnit.MICROSECOND -> "microseconds"
		DateTimeUnit.MILLISECOND -> "milliseconds"
		DateTimeUnit.SECOND -> "seconds"
		DateTimeUnit.MINUTE -> "minutes"
		DateTimeUnit.HOUR -> "hours"
		DateTimeUnit.DAY -> "days"
		DateTimeUnit.WEEK -> "weeks"
		DateTimeUnit.MONTH -> "months"
		DateTimeUnit.QUARTER -> "quarters"
		DateTimeUnit.YEAR -> "years"
		DateTimeUnit.CENTURY -> "centuries"

		else -> error("Unsupported unit: $this")
	}

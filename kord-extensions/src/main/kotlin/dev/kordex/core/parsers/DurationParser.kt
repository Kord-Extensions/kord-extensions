/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.parsers

import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.parsers.caches.TimeUnitCache
import dev.kordex.core.time.name
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.splitOn
import kotlinx.datetime.DateTimePeriod
import java.util.*

private const val DAYS_IN_WEEK = 7

/**
 * Object in charge of parsing strings into [DateTimePeriod]s, using translated locale-aware units.
 */
public object DurationParser : KordExKoinComponent {
	/** Check whether the given character is a valid duration unit character. **/
	public fun charValid(char: Char, locale: Locale): Boolean =
		char.isDigit() ||
			char == ' ' ||
			TimeUnitCache.getUnits(locale).filterKeys { it.startsWith(char) }.isNotEmpty()

	/**
	 * Parse the provided string to a [DateTimePeriod] object, using the strings provided by the given [Locale].
	 */
	public fun parse(input: String, locale: Locale): DateTimePeriod {
		val unitMap = TimeUnitCache.getUnits(locale)

		val units: MutableList<String> = mutableListOf()
		val values: MutableList<String> = mutableListOf()

		var buffer = input.replace(",", "")
			.replace("+", "")
			.replace(" ", "")

		while (buffer.isNotEmpty()) {
			buffer = if (isValueChar(buffer.first())) {
				val (value, remaining) = buffer.splitOn(DurationParser::isNotValueChar)

				values.add(value)
				remaining
			} else {
				val (unit, remaining) = buffer.splitOn(DurationParser::isValueChar)

				units.add(unit)
				remaining
			}
		}

		if (values.size != units.size) {
			throw DurationParserException(
				CoreTranslations.Converters.Duration.Error.badUnitPairs
					.withLocale(locale)
			)
		}

		val allValues: MutableStringKeyedMap<Int> = mutableMapOf()

		while (units.isNotEmpty()) {
			val (unitString, valueString) = units.removeFirst() to values.removeFirst()
			val timeUnit = unitMap[unitString.lowercase()] ?: throw InvalidTimeUnitException(unitString)

			allValues[timeUnit.name] = allValues[timeUnit.name] ?: 0
			allValues[timeUnit.name] = allValues[timeUnit.name]!!.plus(valueString.toInt())
		}

		return DateTimePeriod(
			years = allValues["years"] ?: 0,
			months = allValues["months"] ?: 0,
			days = (allValues["days"] ?: 0) + (allValues["weeks"]?.times(DAYS_IN_WEEK) ?: 0),
			hours = allValues["hours"] ?: 0,
			minutes = allValues["minutes"] ?: 0,
			seconds = allValues["seconds"] ?: 0
		)
	}

	private fun isValueChar(char: Char) = char.isDigit() || char == '-'
	private fun isNotValueChar(char: Char) = !isValueChar(char)
}

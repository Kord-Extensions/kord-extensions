/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.java.time

import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.parsers.DurationParserException
import dev.kordex.core.parsers.InvalidTimeUnitException
import dev.kordex.core.utils.splitOn
import java.util.*

/**
 * Object in charge of parsing strings into [ChronoContainer]s, using translated locale-aware units.
 */
public object J8DurationParser : KordExKoinComponent {
	/** Check whether the given character is a valid duration unit character. **/
	public fun charValid(char: Char, locale: Locale): Boolean =
		char.isDigit() ||
			char == ' ' ||
			J8TimeUnitCache.getUnits(locale).filterKeys { it.startsWith(char) }.isNotEmpty()

	/**
	 * Parse the provided string to a [ChronoContainer] object, using the strings provided by the given [Locale].
	 */
	public fun parse(input: String, locale: Locale): ChronoContainer {
		val unitMap = J8TimeUnitCache.getUnits(locale)

		val units: MutableList<String> = mutableListOf()
		val values: MutableList<String> = mutableListOf()

		var buffer = input.replace(",", "")
			.replace("+", "")
			.replace(" ", "")

		val container = ChronoContainer()

		while (buffer.isNotEmpty()) {
			if (isValueChar(buffer.first())) {
				val (value, remaining) = buffer.splitOn(J8DurationParser::isNotValueChar)

				values.add(value)
				buffer = remaining
			} else {
				val (unit, remaining) = buffer.splitOn(J8DurationParser::isValueChar)

				units.add(unit)
				buffer = remaining
			}
		}

		if (values.size != units.size) {
			throw DurationParserException(
				CoreTranslations.Converters.Duration.Error.badUnitPairs
					.withLocale(locale)
			)
		}

		while (units.isNotEmpty()) {
			val (unitString, valueString) = units.removeFirst() to values.removeFirst()
			val timeUnit = unitMap[unitString.lowercase()] ?: throw InvalidTimeUnitException(unitString)

			container.plus(valueString.toLong(), timeUnit)
		}

		return container
	}

	private fun isValueChar(char: Char) = char.isDigit() || char == '-'
	private fun isNotValueChar(char: Char) = !isValueChar(char)
}

/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.time4j

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.parsers.DurationParserException
import dev.kordex.core.parsers.InvalidTimeUnitException
import dev.kordex.core.utils.splitOn
import net.time4j.Duration
import net.time4j.IsoUnit
import org.koin.core.component.inject
import java.util.*

/**
 * Object in charge of parsing strings into [Duration]s, using translated locale-aware units.
 */
public object T4JDurationParser : KordExKoinComponent {
	private val settings: ExtensibleBotBuilder by inject()

	init {
		settings.aboutBuilder.addCopyright()
	}

	/** Check whether the given character is a valid duration unit character. **/
	public fun charValid(char: Char, locale: Locale): Boolean =
		char.isDigit() ||
			char == ' ' ||
			T4JTimeUnitCache.getUnits(locale).filterKeys { it.startsWith(char) }.isNotEmpty()

	/**
	 * Parse the provided string to a [Duration] object, using the strings provided by the given [Locale].
	 */
	public fun parse(input: String, locale: Locale): Duration<IsoUnit> {
		if ("-" in input) {
			throw DurationParserException(
				CoreTranslations.Converters.Duration.Error.negativeUnsupported
					.withLocale(locale)
			)
		}

		val unitMap = T4JTimeUnitCache.getUnits(locale)

		val units: MutableList<String> = mutableListOf()
		val values: MutableList<String> = mutableListOf()

		var buffer = input.replace(",", "")
			.replace("+", "")
			.replace(" ", "")

		var duration = Duration.ofZero<IsoUnit>()

		while (buffer.isNotEmpty()) {
			if (isValueChar(buffer.first())) {
				val (value, remaining) = buffer.splitOn(T4JDurationParser::isNotValueChar)

				values.add(value)
				buffer = remaining
			} else {
				val (unit, remaining) = buffer.splitOn(T4JDurationParser::isValueChar)

				units.add(unit)
				buffer = remaining
			}
		}

		if (values.size != units.size) {
			throw DurationParserException(
				CoreTranslations.Converters.Duration.Error.negativeUnsupported
					.withLocale(locale)
			)
		}

		while (units.isNotEmpty()) {
			val (unitString, valueString) = units.removeFirst() to values.removeFirst()
			val timeUnit = unitMap[unitString.lowercase()] ?: throw InvalidTimeUnitException(unitString)

			duration = duration.plus(valueString.toLong(), timeUnit)
		}

		return duration
	}

	private fun isValueChar(char: Char) = char.isDigit() || char == '-'
	private fun isNotValueChar(char: Char) = !isValueChar(char)
}

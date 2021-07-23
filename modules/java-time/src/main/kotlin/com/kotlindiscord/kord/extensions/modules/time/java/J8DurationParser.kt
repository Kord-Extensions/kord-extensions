package com.kotlindiscord.kord.extensions.modules.time.java

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.parsers.DurationParserException
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.utils.splitOn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Object in charge of parsing strings into [ChronoContainer]s, using translated locale-aware units.
 */
public object J8DurationParser : KoinComponent {
    private val translations: TranslationsProvider by inject()

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
            throw DurationParserException(translations.translate("converters.duration.error.badUnitPairs", locale))
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

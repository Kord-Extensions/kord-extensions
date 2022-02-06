/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.parsers.DurationParserException
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.utils.splitOn
import net.time4j.Duration
import net.time4j.IsoUnit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Object in charge of parsing strings into [Duration]s, using translated locale-aware units.
 */
public object T4JDurationParser : KoinComponent {
    private val translations: TranslationsProvider by inject()

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
                translations.translate("converters.duration.error.negativeUnsupported", locale)
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
                val (value, remaining) = buffer.splitOn(::isNotValueChar)

                values.add(value)
                buffer = remaining
            } else {
                val (unit, remaining) = buffer.splitOn(::isValueChar)

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

            duration = duration.plus(valueString.toLong(), timeUnit)
        }

        return duration
    }

    private fun isValueChar(char: Char) = char.isDigit() || char == '-'
    private fun isNotValueChar(char: Char) = !isValueChar(char)
}

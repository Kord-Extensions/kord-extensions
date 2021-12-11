/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(ExperimentalTime::class)

package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.parsers.caches.TimeUnitCache
import com.kotlindiscord.kord.extensions.time.name
import com.kotlindiscord.kord.extensions.utils.splitOn
import kotlinx.datetime.DateTimePeriod
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import kotlin.time.ExperimentalTime

private const val DAYS_IN_WEEK = 7

/**
 * Object in charge of parsing strings into [DateTimePeriod]s, using translated locale-aware units.
 */
public object DurationParser : KoinComponent {
    private val translations: TranslationsProvider by inject()

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
            if (isValueChar(buffer.first())) {
                val (value, remaining) = buffer.splitOn(DurationParser::isNotValueChar)

                values.add(value)
                buffer = remaining
            } else {
                val (unit, remaining) = buffer.splitOn(DurationParser::isValueChar)

                units.add(unit)
                buffer = remaining
            }
        }

        if (values.size != units.size) {
            throw DurationParserException(translations.translate("converters.duration.error.badUnitPairs", locale))
        }

        val allValues: MutableMap<String, Int> = mutableMapOf()

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

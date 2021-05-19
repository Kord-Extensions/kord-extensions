package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.utils.splitOn
import org.koin.core.component.KoinComponent
import java.util.*

/**
 * Object in charge of parsing strings into [ChronoContainer]s, using translated locale-aware units.
 */
public object J8DurationParser : KoinComponent {
    /**
     * Parse the provided string to a [ChronoContainer] object, using the strings provided by the given [Locale].
     */
    public fun parse(input: String, locale: Locale): ChronoContainer {
        val unitMap = TimeUnitCache.getUnits(locale)

        var buffer = input.replace(",", "").replace(" ", "")
        val container = ChronoContainer()

        while (buffer.isNotEmpty()) {
            val numberPair = buffer.splitOn(::isNotValueChar)
            val value = numberPair.first.toLong()

            buffer = numberPair.second

            val unitPair = buffer.splitOn(::isValueChar)
            val unit = unitPair.first

            buffer = unitPair.second

            val timeUnit = unitMap[unit.toLowerCase()] ?: throw InvalidTimeUnitException(unit)

            container.plus(value, timeUnit)
        }

        return container
    }

    private fun isValueChar(char: Char) = char.isDigit() || char == '-'
    private fun isNotValueChar(char: Char) = !isValueChar(char)
}

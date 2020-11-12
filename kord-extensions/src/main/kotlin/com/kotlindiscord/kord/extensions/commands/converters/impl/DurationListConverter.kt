package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDurationJ8
import java.time.Duration

class DurationListConverter(required: Boolean = true) : MultiConverter<Duration>(required) {
    override val signatureTypeString = "durations"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val durations = mutableListOf<Duration>()

        for (arg in args) {
            try {
                durations.add(parseDurationJ8(arg))
            } catch (e: InvalidTimeUnitException) {
                break
            }
        }

        parsed = durations.toList()

        return parsed.size
    }
}

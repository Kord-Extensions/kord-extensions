package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDurationJ8
import java.time.Duration

class DurationCoalescingConverter(required: Boolean = true) : CoalescingConverter<Duration>(required) {
    override val signatureTypeString = "duration"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val durations = mutableListOf<String>()

        for (arg in args) {
            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                parseDurationJ8(arg)
                durations.add(arg)
            } catch (e: InvalidTimeUnitException) {
                break
            }
        }

        parsed = parseDurationJ8(
            durations.joinToString()
        )

        return durations.size
    }
}

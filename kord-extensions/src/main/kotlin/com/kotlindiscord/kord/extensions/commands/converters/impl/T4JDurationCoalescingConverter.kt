package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDuration
import net.time4j.Duration
import net.time4j.IsoUnit

class T4JDurationCoalescingConverter(required: Boolean = true) : CoalescingConverter<Duration<IsoUnit>>(required) {
    override val signatureTypeString = "duration"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val durations = mutableListOf<String>()

        for (arg in args) {
            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                parseDuration(arg)
                durations.add(arg)
            } catch (e: InvalidTimeUnitException) {
                break
            }
        }

        parsed = parseDuration(
            durations.joinToString()
        )

        return durations.size
    }
}

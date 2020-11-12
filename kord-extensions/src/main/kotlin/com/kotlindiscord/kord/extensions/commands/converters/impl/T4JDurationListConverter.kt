package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDuration
import net.time4j.Duration
import net.time4j.IsoUnit

class T4JDurationListConverter(required: Boolean = true) : MultiConverter<Duration<IsoUnit>>(required) {
    override val signatureTypeString = "durations"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val durations = mutableListOf<Duration<IsoUnit>>()

        for (arg in args) {
            try {
                durations.add(parseDuration(arg))
            } catch (e: InvalidTimeUnitException) {
                break
            }
        }

        parsed = durations.toList()

        return parsed.size
    }
}

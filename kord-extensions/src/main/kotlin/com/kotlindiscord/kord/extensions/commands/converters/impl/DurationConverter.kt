package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDurationJ8
import java.time.Duration

class DurationConverter(required: Boolean = true) : SingleConverter<Duration>(required) {
    override val signatureTypeString = "duration"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = parseDurationJ8(arg)
        } catch (e: InvalidTimeUnitException) {
            throw ParseException("Invalid duration unit specified: ${e.unit}")
        }

        return true
    }
}

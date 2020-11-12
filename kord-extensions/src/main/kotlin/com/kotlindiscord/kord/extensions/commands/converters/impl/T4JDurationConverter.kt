package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDuration
import net.time4j.Duration
import net.time4j.IsoUnit

class T4JDurationConverter(required: Boolean = true) : SingleConverter<Duration<IsoUnit>>(required) {
    override val signatureTypeString = "duration"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = parseDuration(arg)
        } catch (e: InvalidTimeUnitException) {
            throw ParseException("Invalid duration unit specified: ${e.unit}")
        }

        return true
    }
}

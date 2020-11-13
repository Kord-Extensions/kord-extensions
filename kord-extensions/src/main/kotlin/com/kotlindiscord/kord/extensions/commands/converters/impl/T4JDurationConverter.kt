package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.t4jDuration
import com.kotlindiscord.kord.extensions.commands.converters.t4jDurationList
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDuration
import net.time4j.Duration
import net.time4j.IsoUnit

/**
 * Argument converter for Time4J [Duration] arguments.
 *
 * For a coalescing version of this converter, see [T4JDurationCoalescingConverter].
 * If you're using Java 8 durations instead, see [DurationConverter].
 *
 * @see t4jDuration
 * @see t4jDurationList
 * @see parseDuration
 */
class T4JDurationConverter : SingleConverter<Duration<IsoUnit>>() {
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

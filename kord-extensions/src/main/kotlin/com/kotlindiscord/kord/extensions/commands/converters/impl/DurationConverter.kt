package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.duration
import com.kotlindiscord.kord.extensions.commands.converters.durationList
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDurationJ8
import java.time.Duration

/**
 * Argument converter for Java 8 [Duration] arguments.
 *
 * For a coalescing version of this converter, see [DurationCoalescingConverter].
 * If you're using Time4J instead, see [T4JDurationConverter].
 *
 * @see duration
 * @see durationList
 * @see parseDurationJ8
 */
public class DurationConverter : SingleConverter<Duration>() {
    override val signatureTypeString: String = "duration"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = parseDurationJ8(arg)
        } catch (e: InvalidTimeUnitException) {
            throw ParseException("Invalid duration unit specified: ${e.unit}")
        }

        return true
    }
}

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.coalescedDuration
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDurationJ8
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import java.time.Duration

/**
 * Coalescing argument converter for Java 8 [Duration] arguments.
 *
 * This converter will take individual duration specifiers ("1w", "2y", "3d" etc) until it no longer can, and then
 * combine them into a single [Duration].
 *
 * @see coalescedDuration
 * @see parseDurationJ8
 */
public class DurationCoalescingConverter(
    shouldThrow: Boolean = false
) : CoalescingConverter<Duration>(shouldThrow) {
    override val signatureTypeString: String = "duration"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val durations = mutableListOf<String>()

        for (arg in args) {
            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                parseDurationJ8(arg)
                durations.add(arg)
            } catch (e: InvalidTimeUnitException) {
                if (this.shouldThrow) {
                    throw ParseException("Invalid duration unit specified: ${e.unit}")
                }

                break
            }
        }

        parsed = parseDurationJ8(
            durations.joinToString()
        )

        return durations.size
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

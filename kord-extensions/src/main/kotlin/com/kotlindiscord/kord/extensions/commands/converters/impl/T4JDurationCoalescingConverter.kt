package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.coalescedT4jDuration
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDuration
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import net.time4j.Duration
import net.time4j.IsoUnit

/**
 * Coalescing argument converter for Time4J [Duration] arguments.
 *
 * This converter will take individual duration specifiers ("1w", "2y", "3d" etc) until it no longer can, and then
 * combine them into a single [Duration].
 *
 * @see coalescedT4jDuration
 * @see parseDuration
 */
public class T4JDurationCoalescingConverter(
    shouldThrow: Boolean = false
) :  CoalescingConverter<Duration<IsoUnit>>(shouldThrow) {
    override val signatureTypeString: String = "duration"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val durations = mutableListOf<String>()

        for (arg in args) {
            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                parseDuration(arg)
                durations.add(arg)
            } catch (e: InvalidTimeUnitException) {
                if (this.shouldThrow) {
                    throw ParseException("Invalid duration unit specified: ${e.unit}")
                }

                break
            }
        }

        parsed = parseDuration(
            durations.joinToString()
        )

        return durations.size
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

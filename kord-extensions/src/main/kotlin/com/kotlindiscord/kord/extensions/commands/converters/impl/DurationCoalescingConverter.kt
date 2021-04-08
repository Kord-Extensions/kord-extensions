package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.coalescedDuration
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDurationJ8
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import java.time.Duration

private const val HELP_MESSAGE = "__How to use durations__\n\n" +
    "Durations are specified in pairs of amounts and units - for example, `12d` would be 12 days. " +
    "Compound durations are supported - for example, `2d 12h` would be 2 days and 12 hours.\n\n" +
    "The following units are supported:\n\n" +

    "**Seconds:** `s`, `sec`, `second`, `seconds`\n" +
    "**Minutes:** `m`, `mi`, `min`, `minute`, `minutes`\n" +
    "**Hours:** `h`, `hour`, `hours`\n" +
    "**Days:** `d`, `day`, `days`\n" +
    "**Weeks:** `w`, `week`, `weeks`\n" +
    "**Months:** `mo`, `month`, `months`\n" +
    "**Years:** `y`, `year`, `years`"

/**
 * Coalescing argument converter for Java 8 [Duration] arguments.
 *
 * This converter will take individual duration specifiers ("1w", "2y", "3d" etc) until it no longer can, and then
 * combine them into a single [Duration].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 *
 * @see coalescedDuration
 * @see parseDurationJ8
 */
public class DurationCoalescingConverter(
    public val longHelp: Boolean = true,
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
                    val message = if (e.unit.isEmpty()) {
                        "Please specify a unit - bare numbers are not supported."
                    } else {
                        "Invalid duration unit specified: `${e.unit}`"
                    } + if (longHelp) {
                        "\n\n$HELP_MESSAGE"
                    } else {
                        ""
                    }

                    throw CommandException(message)
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

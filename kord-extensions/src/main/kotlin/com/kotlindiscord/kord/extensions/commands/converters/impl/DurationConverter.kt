package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.duration
import com.kotlindiscord.kord.extensions.commands.converters.durationList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDurationJ8
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import java.time.Duration

private const val HELP_MESSAGE = "__How to use durations__\n\n" +
    "Durations are specified in pairs of amounts and units - for example, `12d` would be 12 days. " +
    "Compound durations are supported - for example, `2d12h` would be 2 days and 12 hours.\n\n" +
    "The following units are supported:\n\n" +

    "**Seconds:** `s`, `sec`, `second`, `seconds`\n" +
    "**Minutes:** `m`, `mi`, `min`, `minute`, `minutes`\n" +
    "**Hours:** `h`, `hour`, `hours`\n" +
    "**Days:** `d`, `day`, `days`\n" +
    "**Weeks:** `w`, `week`, `weeks`\n" +
    "**Months:** `mo`, `month`, `months`\n" +
    "**Years:** `y`, `year`, `years`"

/**
 * Argument converter for Java 8 [Duration] arguments.
 *
 * For a coalescing version of this converter, see [DurationCoalescingConverter].
 * If you're using Time4J instead, see [T4JDurationConverter].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 *
 * @see duration
 * @see durationList
 * @see parseDurationJ8
 */
@OptIn(KordPreview::class)
public class DurationConverter(
    public val longHelp: Boolean = true,
    override var validator: (suspend Argument<*>.(Duration) -> Unit)? = null
) : SingleConverter<Duration>() {
    override val signatureTypeString: String = "duration"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = parseDurationJ8(arg)
        } catch (e: InvalidTimeUnitException) {
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

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.t4jDuration
import com.kotlindiscord.kord.extensions.commands.converters.t4jDurationList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDuration
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import net.time4j.Duration
import net.time4j.IsoUnit

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
 * Argument converter for Time4J [Duration] arguments.
 *
 * For a coalescing version of this converter, see [T4JDurationCoalescingConverter].
 * If you're using Java 8 durations instead, see [DurationConverter].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 *
 * @see t4jDuration
 * @see t4jDurationList
 * @see parseDuration
 */
@OptIn(KordPreview::class)
public class T4JDurationConverter(
    public val longHelp: Boolean = true,
    override var validator: (suspend (Duration<IsoUnit>) -> Unit)? = null
) : SingleConverter<Duration<IsoUnit>>() {
    override val signatureTypeString: String = "duration"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = parseDuration(arg)
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

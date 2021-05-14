package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
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
    override var validator: (suspend Argument<*>.(Duration<IsoUnit>) -> Unit)? = null
) : SingleConverter<Duration<IsoUnit>>() {
    override val signatureTypeString: String = "converters.duration.error.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            this.parsed = parseDuration(arg)
        } catch (e: InvalidTimeUnitException) {
            val message = if (e.unit.isEmpty()) {
                context.translate("converters.duration.error.missingUnit")
            } else {
                context.translate("converters.duration.error.invalidUnit", replacements = arrayOf(e.unit))
            } + if (longHelp) {
                "\n\n" + context.translate("converters.duration.help")
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

package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import java.time.Duration

/**
 * Argument converter for Java 8 [Duration] arguments.
 *
 * For a coalescing version of this converter, see [J8DurationCoalescingConverter].
 * If you're using Time4J instead, see [T4JDurationConverter].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 *
 * @see duration
 * @see durationList
 * @see parseDurationJ8
 */
@OptIn(KordPreview::class)
public class J8DurationConverter(
    public val longHelp: Boolean = true,
    override var validator: (suspend Argument<*>.(Duration) -> Unit)? = null
) : SingleConverter<Duration>() {
    override val signatureTypeString: String = "converters.duration.error.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            this.parsed = parseDurationJ8(arg)
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

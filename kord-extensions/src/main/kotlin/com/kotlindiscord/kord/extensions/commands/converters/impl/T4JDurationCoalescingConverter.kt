package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
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
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 *
 * @see coalescedT4jDuration
 * @see parseDuration
 */
public class T4JDurationCoalescingConverter(
    public val longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    override var validator: (suspend Argument<*>.(Duration<IsoUnit>) -> Unit)? = null
) : CoalescingConverter<Duration<IsoUnit>>(shouldThrow) {
    override val signatureTypeString: String = "converters.duration.error.signatureType"

    override suspend fun parse(args: List<String>, context: CommandContext): Int {
        val durations = mutableListOf<String>()

        for (arg in args) {
            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                parseDuration(arg)
                durations.add(arg)
            } catch (e: InvalidTimeUnitException) {
                if (this.shouldThrow) {
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

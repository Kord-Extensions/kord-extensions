package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.parsers.DurationParserException
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import mu.KotlinLogging
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
 * @see parseT4JDuration
 */
public class T4JDurationCoalescingConverter(
    public val longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    override var validator: (suspend Argument<*>.(Duration<IsoUnit>) -> Unit)? = null
) : CoalescingConverter<Duration<IsoUnit>>(shouldThrow) {
    override val signatureTypeString: String = "converters.duration.error.signatureType"
    private val logger = KotlinLogging.logger {}

    override suspend fun parse(args: List<String>, context: CommandContext): Int {
        val durations = mutableListOf<String>()

        for (arg in args) {
            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                T4JDurationParser.parseT4JDuration(arg, context.getLocale())
                durations.add(arg)
            } catch (e: InvalidTimeUnitException) {
                throwIfNecessary(e, context)

                break
            } catch (e: DurationParserException) {
                throwIfNecessary(e, context)

                break
            }
        }

        try {
            parsed = T4JDurationParser.parseT4JDuration(
                durations.joinToString(""),
                context.getLocale()
            )
        } catch (e: InvalidTimeUnitException) {
            throwIfNecessary(e, context, true)
        } catch (e: DurationParserException) {
            throwIfNecessary(e, context, true)
        }

        return durations.size
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    private suspend fun throwIfNecessary(
        e: Exception,
        context: CommandContext,
        override: Boolean = false
    ): Unit = if (shouldThrow || override) {
        when (e) {
            is InvalidTimeUnitException -> {
                val message = context.translate(
                    "converters.duration.error.invalidUnit",
                    replacements = arrayOf(e.unit)
                ) + if (longHelp) "\n\n" + context.translate("converters.duration.help") else ""

                throw CommandException(message)
            }

            is DurationParserException -> throw CommandException(e.error)

            else -> throw e
        }
    } else {
        logger.debug(e) { "Error thrown during duration parsing" }
    }
}
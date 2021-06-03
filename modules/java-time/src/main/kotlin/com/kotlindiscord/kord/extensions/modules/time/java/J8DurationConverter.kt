@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.modules.time.java

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.parsers.DurationParserException
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import java.time.Duration
import java.time.LocalDateTime

/**
 * Argument converter for Java 8 [Duration] arguments.
 *
 * For a coalescing version of this converter, see [J8DurationCoalescingConverter].
 * If you're using Time4J instead, see [T4JDurationConverter].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 * @param positiveOnly Whether a positive duration is required - `true` by default.
 *
 * @see duration
 * @see durationList
 * @see parseDurationJ8
 */
@OptIn(KordPreview::class)
public class J8DurationConverter(
    public val longHelp: Boolean = true,
    public val positiveOnly: Boolean = true,
    override var validator: (suspend Argument<*>.(ChronoContainer) -> Unit)? = null
) : SingleConverter<ChronoContainer>() {
    override val signatureTypeString: String = "converters.duration.error.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            val result = J8DurationParser.parse(arg, context.getLocale())

            if (positiveOnly) {
                val normalized = result.clone()

                normalized.normalize(LocalDateTime.now())

                if (!normalized.isPositive()) {
                    throw CommandException(context.translate("converters.duration.error.positiveOnly"))
                }
            }

            parsed = result
        } catch (e: InvalidTimeUnitException) {
            val message = context.translate(
                "converters.duration.error.invalidUnit",
                replacements = arrayOf(e.unit)
            ) + if (longHelp) "\n\n" + context.translate("converters.duration.help") else ""

            throw CommandException(message)
        } catch (e: DurationParserException) {
            throw CommandException(e.error)
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a Java 8 Duration converter, for single arguments.
 *
 * @see J8DurationConverter
 */
public fun Arguments.j8Duration(
    displayName: String,
    description: String,
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    validator: (suspend Argument<*>.(ChronoContainer) -> Unit)? = null,
): SingleConverter<ChronoContainer> =
    arg(
        displayName,
        description,
        J8DurationConverter(
            longHelp = longHelp,
            positiveOnly = requirePositive,
            validator = validator
        )
    )

/**
 * Create an optional Java 8 Duration converter, for single arguments.
 *
 * @see J8DurationConverter
 */
public fun Arguments.optionalJ8Duration(
    displayName: String,
    description: String,
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(ChronoContainer?) -> Unit)? = null,
): OptionalConverter<ChronoContainer?> =
    arg(
        displayName,
        description,
        J8DurationConverter(longHelp = longHelp, positiveOnly = requirePositive)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting Java 8 Duration converter, for single arguments.
 *
 * @see J8DurationConverter
 */
public fun Arguments.defaultingJ8Duration(
    displayName: String,
    description: String,
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    defaultValue: ChronoContainer,
    validator: (suspend Argument<*>.(ChronoContainer) -> Unit)? = null,
): DefaultingConverter<ChronoContainer> =
    arg(
        displayName,
        description,
        J8DurationConverter(longHelp = longHelp, positiveOnly = requirePositive)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a Java 8 Duration converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see J8DurationConverter
 */
public fun Arguments.j8DurationList(
    displayName: String,
    description: String,
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<ChronoContainer>) -> Unit)? = null,
): MultiConverter<ChronoContainer> =
    arg(
        displayName,
        description,
        J8DurationConverter(longHelp = longHelp, positiveOnly = requirePositive)
            .toMulti(required, signatureTypeString = "durations", nestedValidator = validator)
    )

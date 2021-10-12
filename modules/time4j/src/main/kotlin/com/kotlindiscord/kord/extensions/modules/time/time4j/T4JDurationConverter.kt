@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.parsers.DurationParserException
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import net.time4j.Duration
import net.time4j.IsoUnit

/**
 * Argument converter for Time4J [Duration] arguments.
 *
 * For a coalescing version of this converter, see [T4JDurationCoalescingConverter].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 *
 * @see t4jDuration
 * @see t4jDurationList
 * @see parseT4JDuration
 */
@OptIn(KordPreview::class)
public class T4JDurationConverter(
    public val longHelp: Boolean = true,
    override var validator: Validator<Duration<IsoUnit>> = null
) : SingleConverter<Duration<IsoUnit>>() {
    override val signatureTypeString: String = "converters.duration.error.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        try {
            this.parsed = T4JDurationParser.parse(arg, context.getLocale())
        } catch (e: InvalidTimeUnitException) {
            val message = context.translate(
                "converters.duration.error.invalidUnit",
                replacements = arrayOf(e.unit)
            ) + if (longHelp) "\n\n" + context.translate("converters.duration.help") else ""

            throw DiscordRelayedException(message)
        } catch (e: DurationParserException) {
            throw DiscordRelayedException(e.error)
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val arg = (option as? OptionValue.StringOptionValue)?.value ?: return false

        try {
            this.parsed = T4JDurationParser.parse(arg, context.getLocale())
        } catch (e: InvalidTimeUnitException) {
            val message = context.translate(
                "converters.duration.error.invalidUnit",
                replacements = arrayOf(e.unit)
            ) + if (longHelp) "\n\n" + context.translate("converters.duration.help") else ""

            throw DiscordRelayedException(message)
        } catch (e: DurationParserException) {
            throw DiscordRelayedException(e.error)
        }

        return true
    }
}

/**
 * Create a Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.t4jDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    validator: Validator<Duration<IsoUnit>> = null,
): SingleConverter<Duration<IsoUnit>> =
    arg(displayName, description, T4JDurationConverter(longHelp = longHelp, validator = validator))

/**
 * Create an optional Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.optionalT4jDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: Validator<Duration<IsoUnit>?> = null,
): OptionalConverter<Duration<IsoUnit>?> =
    arg(
        displayName,
        description,
        T4JDurationConverter(longHelp = longHelp)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.defaultingT4jDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    defaultValue: Duration<IsoUnit>,
    validator: Validator<Duration<IsoUnit>> = null,
): DefaultingConverter<Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationConverter(longHelp = longHelp)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a Time4J Duration converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.t4jDurationList(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    required: Boolean = true,
    validator: Validator<List<Duration<IsoUnit>>> = null,
): MultiConverter<Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationConverter(longHelp = longHelp)
            .toMulti(required, signatureTypeString = "durations", nestedValidator = validator)
    )

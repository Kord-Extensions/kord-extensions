@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for regular expression arguments, converting them into [Regex] objects.
 *
 * Please note that user-provided regular expressions are not safe - they can take down your entire bot.
 *
 * As there is no way to validate individual segments of regex, the multi version of this converter
 * (via [toMulti]) will consume all remaining arguments.
 *
 * @param options Optional set of [RegexOption]s to pass to the regex parser.
 *
 * @see regex
 * @see regexList
 */

@OptIn(KordPreview::class)
public class RegexConverter(
    private val options: Set<RegexOption> = setOf(),
    override var validator: (suspend Argument<*>.(Regex) -> Unit)? = null
) : SingleConverter<Regex>() {
    override val signatureTypeString: String = "converters.regex.signatureType.singular"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        this.parsed = arg.toRegex(options)

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.regex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex) -> Unit)? = null,
): SingleConverter<Regex> =
    arg(displayName, description, RegexConverter(options, validator))

/**
 * Create an optional regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.optionalRegex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf(),
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Regex?) -> Unit)? = null,
): OptionalConverter<Regex?> =
    arg(
        displayName,
        description,
        RegexConverter(options)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.defaultingRegex(
    displayName: String,
    description: String,
    defaultValue: Regex,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex) -> Unit)? = null,
): DefaultingConverter<Regex> =
    arg(
        displayName,
        description,
        RegexConverter(options)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a regex converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see RegexConverter
 */
public fun Arguments.regexList(
    displayName: String,
    description: String,
    required: Boolean = true,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(List<Regex>) -> Unit)? = null,
): MultiConverter<Regex> =
    arg(
        displayName,
        description,
        RegexConverter(options)
            .toMulti(required, signatureTypeString = "regexes", nestedValidator = validator)
    )

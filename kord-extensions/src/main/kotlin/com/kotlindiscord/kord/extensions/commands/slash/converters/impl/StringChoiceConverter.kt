@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.slash.converters.impl

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.converters.ChoiceConverter
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Choice converter for string arguments. Supports mapping up to 25 choices to string.
 */
@OptIn(KordPreview::class)
public class StringChoiceConverter(
    choices: Map<String, String>,
    override var validator: (suspend Argument<*>.(String) -> Unit)? = null
) : ChoiceConverter<String>(choices) {
    override val signatureTypeString: String = "converters.string.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        this.parsed = arg

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply {
            required = true

            this@StringChoiceConverter.choices.forEach { choice(it.key, it.value) }
        }
}

/**
 * Create a string choice argument converter, for a defined set of single arguments.
 *
 * @see StringChoiceConverter
 */
public fun Arguments.stringChoice(
    displayName: String,
    description: String,
    choices: Map<String, String>,
    validator: (suspend Argument<*>.(String) -> Unit)? = null
): SingleConverter<String> = arg(displayName, description, StringChoiceConverter(choices, validator))

/**
 * Create an optional string choice argument converter, for a defined set of single arguments.
 *
 * @see StringChoiceConverter
 */
public fun Arguments.optionalStringChoice(
    displayName: String,
    description: String,
    choices: Map<String, String>,
    validator: (suspend Argument<*>.(String?) -> Unit)? = null
): OptionalConverter<String?> = arg(
    displayName,
    description,
    StringChoiceConverter(choices)
        .toOptional(nestedValidator = validator)
)

/**
 * Create a defaulting string choice argument converter, for a defined set of single arguments.
 *
 * @see StringChoiceConverter
 */
public fun Arguments.defaultingStringChoice(
    displayName: String,
    description: String,
    defaultValue: String,
    choices: Map<String, String>,
    validator: (suspend Argument<*>.(String) -> Unit)? = null
): DefaultingConverter<String> = arg(
    displayName,
    description,
    StringChoiceConverter(choices)
        .toDefaulting(defaultValue, nestedValidator = validator)
)

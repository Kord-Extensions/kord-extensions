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
 * Coalescing argument that simply returns the argument as it was given.
 *
 * The multi version of this converter (via [toMulti]) will consume all remaining arguments.
 *
 * @see string
 * @see stringList
 */
@OptIn(KordPreview::class)
public class StringConverter(
    override var validator: (suspend Argument<*>.(String) -> Unit)? = null
) : SingleConverter<String>() {
    override val signatureTypeString: String = "converters.string.signatureType"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        this.parsed = arg

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.string(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): SingleConverter<String> =
    arg(displayName, description, StringConverter(validator))

/**
 * Create an optional string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.optionalString(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(String?) -> Unit)? = null,
): OptionalConverter<String?> =
    arg(
        displayName,
        description,
        StringConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.defaultingString(
    displayName: String,
    description: String,
    defaultValue: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): DefaultingConverter<String> =
    arg(
        displayName,
        description,
        StringConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a string converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see StringConverter
 */
public fun Arguments.stringList(
    displayName: String,
    description: String,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<String>) -> Unit)? = null,
): MultiConverter<String> =
    arg(
        displayName,
        description,
        StringConverter()
            .toMulti(required, nestedValidator = validator)
    )

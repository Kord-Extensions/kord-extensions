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
import com.kotlindiscord.kord.extensions.utils.parseBoolean
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.BooleanBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

/**
 * Argument converter for [Boolean] arguments.
 *
 * Truthiness is determined by the [parseBoolean] function.
 *
 * @see boolean
 * @see booleanList
 */
@OptIn(KordPreview::class)
public class BooleanConverter(
    override var validator: (suspend Argument<*>.(Boolean) -> Unit)? = null
) : SingleConverter<Boolean>() {
    public override val signatureTypeString: String = "converters.boolean.signatureType"
    public override val errorTypeString: String = "converters.boolean.errorType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val bool = arg.parseBoolean(context) ?: return false

        this.parsed = bool

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        BooleanBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.boolean(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(Boolean) -> Unit)? = null
): SingleConverter<Boolean> =
    arg(displayName, description, BooleanConverter(validator))

/**
 * Create an optional boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.optionalBoolean(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Boolean?) -> Unit)? = null,
): OptionalConverter<Boolean?> =
    arg(
        displayName,
        description,
        BooleanConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.defaultingBoolean(
    displayName: String,
    description: String,
    defaultValue: Boolean,
    validator: (suspend Argument<*>.(Boolean) -> Unit)? = null,
): DefaultingConverter<Boolean> =
    arg(
        displayName,
        description,
        BooleanConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a boolean argument converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see BooleanConverter
 */
public fun Arguments.booleanList(
    displayName: String,
    description: String,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<Boolean>) -> Unit)? = null,
): MultiConverter<Boolean> =
    arg(
        displayName,
        description,
        BooleanConverter()
            .toMulti(required, errorTypeString = "multiple `yes` or `no` values", nestedValidator = validator)
    )

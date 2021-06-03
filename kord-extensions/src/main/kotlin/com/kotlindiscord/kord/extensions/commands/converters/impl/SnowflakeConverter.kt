@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for long arguments, converting them into [Long].
 *
 * @see long
 * @see longList
 */
@OptIn(KordPreview::class)
public class SnowflakeConverter(
    override var validator: (suspend Argument<*>.(Snowflake) -> Unit)? = null
) : SingleConverter<Snowflake>() {
    override val signatureTypeString: String = "converters.snowflake.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            this.parsed = Snowflake(arg)
        } catch (e: NumberFormatException) {
            throw CommandException(
                context.translate("converters.snowflake.error.invalid", replacements = arrayOf(arg))
            )
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a snowflake converter, for single arguments.
 *
 * @see SnowflakeConverter
 */
public fun Arguments.snowflake(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(Snowflake) -> Unit)? = null,
): SingleConverter<Snowflake> =
    arg(displayName, description, SnowflakeConverter(validator))

/**
 * Create an optional snowflake converter, for single arguments.
 *
 * @see SnowflakeConverter
 */
public fun Arguments.optionalSnowflake(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Snowflake?) -> Unit)? = null,
): OptionalConverter<Snowflake?> =
    arg(
        displayName,
        description,
        SnowflakeConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting snowflake converter, for single arguments.
 *
 * @see SnowflakeConverter
 */
public fun Arguments.defaultingString(
    displayName: String,
    description: String,
    defaultValue: Snowflake,
    validator: (suspend Argument<*>.(Snowflake) -> Unit)? = null,
): DefaultingConverter<Snowflake> =
    arg(
        displayName,
        description,
        SnowflakeConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a snowflake converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see SnowflakeConverter
 */
public fun Arguments.snowflakeList(
    displayName: String,
    description: String,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<Snowflake>) -> Unit)? = null,
): MultiConverter<Snowflake> =
    arg(
        displayName,
        description,
        SnowflakeConverter()
            .toMulti(required, nestedValidator = validator)
    )

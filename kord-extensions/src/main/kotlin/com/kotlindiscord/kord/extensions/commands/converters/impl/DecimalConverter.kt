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
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for decimal arguments, converting them into [Double].
 *
 * @see decimal
 * @see decimalList
 */
@OptIn(KordPreview::class)
public class DecimalConverter(
    override var validator: (suspend Argument<*>.(Double) -> Unit)? = null
) : SingleConverter<Double>() {
    override val signatureTypeString: String = "converters.decimal.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            this.parsed = arg.toDouble()
        } catch (e: NumberFormatException) {
            throw CommandException(
                context.translate("converters.decimal.error.invalid", replacements = arrayOf(arg))
            )
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.decimal(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(Double) -> Unit)? = null,
): SingleConverter<Double> =
    arg(displayName, description, DecimalConverter(validator))

/**
 * Create an optional decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.optionalDecimal(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Double?) -> Unit)? = null,
): OptionalConverter<Double?> =
    arg(
        displayName,
        description,
        DecimalConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.defaultingDecimal(
    displayName: String,
    description: String,
    defaultValue: Double,
    validator: (suspend Argument<*>.(Double) -> Unit)? = null,
): DefaultingConverter<Double> =
    arg(
        displayName,
        description,
        DecimalConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a decimal converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see DecimalConverter
 */
public fun Arguments.decimalList(
    displayName: String,
    description: String,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<Double>) -> Unit)? = null,
): MultiConverter<Double> =
    arg(
        displayName,
        description,
        DecimalConverter().toMulti(required, signatureTypeString = "decimals", nestedValidator = validator)
    )

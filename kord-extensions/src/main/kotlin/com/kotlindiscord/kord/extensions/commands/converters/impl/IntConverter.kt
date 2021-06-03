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
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for integer arguments, converting them into [Int].
 *
 * @see int
 * @see intList
 */
@OptIn(KordPreview::class)
public class IntConverter(
    private val radix: Int = DEFAULT_RADIX,
    override var validator: (suspend Argument<*>.(Int) -> Unit)? = null
) : SingleConverter<Int>() {
    override val signatureTypeString: String = "converters.number.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            this.parsed = arg.toInt(radix)
        } catch (e: NumberFormatException) {
            val errorString = if (radix == DEFAULT_RADIX) {
                context.translate("converters.number.error.invalid.defaultBase", replacements = arrayOf(arg))
            } else {
                context.translate("converters.number.error.invalid.otherBase", replacements = arrayOf(arg, radix))
            }

            throw CommandException(errorString)
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        IntChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create an integer converter, for single arguments.
 *
 * @see IntConverter
 */
public fun Arguments.int(
    displayName: String,
    description: String,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Int) -> Unit)? = null,
): SingleConverter<Int> =
    arg(displayName, description, IntConverter(radix, validator))

/**
 * Create an integer converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see IntConverter
 */
public fun Arguments.intList(
    displayName: String,
    description: String,
    required: Boolean = true,
    radix: Int = 10,
    validator: (suspend Argument<*>.(List<Int>) -> Unit)? = null,
): MultiConverter<Int> =
    arg(
        displayName,
        description,
        IntConverter(radix)
            .toMulti(required, signatureTypeString = "numbers", nestedValidator = validator)
    )

/**
 * Create an optional integer converter, for single arguments.
 *
 * @see IntConverter
 */
public fun Arguments.optionalInt(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Int?) -> Unit)? = null,
): OptionalConverter<Int?> =
    arg(
        displayName,
        description,
        IntConverter(radix)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting integer converter, for single arguments.
 *
 * @see IntConverter
 */
public fun Arguments.defaultingInt(
    displayName: String,
    description: String,
    defaultValue: Int,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Int) -> Unit)? = null,
): DefaultingConverter<Int> =
    arg(
        displayName,
        description,
        IntConverter(radix)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

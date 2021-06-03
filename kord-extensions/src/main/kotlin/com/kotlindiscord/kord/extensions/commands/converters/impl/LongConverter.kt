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
 * Argument converter for long arguments, converting them into [Long].
 *
 * @see long
 * @see longList
 */
@OptIn(KordPreview::class)
public class LongConverter(
    private val radix: Int = DEFAULT_RADIX,
    override var validator: (suspend Argument<*>.(Long) -> Unit)? = null
) : SingleConverter<Long>() {
    override val signatureTypeString: String = "converters.number.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            this.parsed = arg.toLong(radix)
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
 * Create a long converter, for single arguments.
 *
 * @see LongConverter
 */
public fun Arguments.long(
    displayName: String,
    description: String,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Long) -> Unit)? = null,
): SingleConverter<Long> =
    arg(displayName, description, LongConverter(radix, validator))

/**
 * Create an optional long converter, for single arguments.
 *
 * @see LongConverter
 */
public fun Arguments.optionalLong(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Long?) -> Unit)? = null,
): OptionalConverter<Long?> =
    arg(
        displayName,
        description,
        LongConverter(radix)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting long converter, for single arguments.
 *
 * @see LongConverter
 */
public fun Arguments.defaultingLong(
    displayName: String,
    description: String,
    defaultValue: Long,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Long) -> Unit)? = null,
): DefaultingConverter<Long> =
    arg(
        displayName,
        description,
        LongConverter(radix)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a long converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see LongConverter
 */
public fun Arguments.longList(
    displayName: String,
    description: String,
    required: Boolean = true,
    radix: Int = 10,
    validator: (suspend Argument<*>.(List<Long>) -> Unit)? = null,
): MultiConverter<Long> =
    arg(
        displayName,
        description,
        LongConverter(radix)
            .toMulti(required, signatureTypeString = "numbers", nestedValidator = validator)
    )

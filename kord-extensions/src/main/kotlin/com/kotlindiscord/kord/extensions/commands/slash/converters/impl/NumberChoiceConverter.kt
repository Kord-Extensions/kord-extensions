@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.slash.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.converters.ChoiceConverter
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Choice converter for integer arguments. Supports mapping up to 25 choices to integers.
 *
 * Discord doesn't support longs or floating point types, so this is the only numeric type you can use directly.
 */
@OptIn(KordPreview::class)
public class NumberChoiceConverter(
    private val radix: Int = DEFAULT_RADIX,
    choices: Map<String, Int>,
    override var validator: Validator<Int> = null
) : ChoiceConverter<Int>(choices) {
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
        IntChoiceBuilder(arg.displayName, arg.description).apply {
            required = true

            this@NumberChoiceConverter.choices.forEach { choice(it.key, it.value) }
        }
}

/**
 * Create a number choice argument converter, for a defined set of single arguments.
 *
 * @see NumberChoiceConverter
 */
public fun Arguments.numberChoice(
    displayName: String,
    description: String,
    choices: Map<String, Int>,
    radix: Int = 10,
    validator: Validator<Int> = null
): SingleConverter<Int> = arg(displayName, description, NumberChoiceConverter(radix, choices, validator))

/**
 * Create an optional number choice argument converter, for a defined set of single arguments.
 *
 * @see NumberChoiceConverter
 */
public fun Arguments.optionalNumberChoice(
    displayName: String,
    description: String,
    choices: Map<String, Int>,
    radix: Int = 10,
    validator: Validator<Int?> = null
): OptionalConverter<Int?> = arg(
    displayName,
    description,
    NumberChoiceConverter(radix, choices)
        .toOptional(nestedValidator = validator)
)

/**
 * Create a defaulting number choice argument converter, for a defined set of single arguments.
 *
 * @see NumberChoiceConverter
 */
public fun Arguments.defaultingNumberChoice(
    displayName: String,
    description: String,
    defaultValue: Int,
    choices: Map<String, Int>,
    radix: Int = 10,
    validator: Validator<Int> = null
): DefaultingConverter<Int> = arg(
    displayName,
    description,
    NumberChoiceConverter(radix, choices)
        .toDefaulting(defaultValue, nestedValidator = validator)
)

@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for integer arguments, converting them into [Int].
 */
@Converter(
    "int",

    types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
    arguments = ["radix: Int = $DEFAULT_RADIX"]
)
@OptIn(KordPreview::class)
public class IntConverter(
    private val radix: Int = DEFAULT_RADIX,
    override var validator: Validator<Int> = null
) : SingleConverter<Int>() {
    override val signatureTypeString: String = "converters.number.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

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

// /**
// * Create an integer converter, for single arguments.
// *
// * @see IntConverter
// */
// public fun Arguments.int(
//    displayName: String,
//    description: String,
//    radix: Int = 10,
//    validator: Validator<Int> = null,
// ): SingleConverter<Int> =
//    arg(displayName, description, IntConverter(radix, validator))
//
// /**
// * Create an optional integer converter, for single arguments.
// *
// * @see IntConverter
// */
// public fun Arguments.optionalInt(
//    displayName: String,
//    description: String,
//    outputError: Boolean = false,
//    radix: Int = 10,
//    validator: Validator<Int?> = null,
// ): OptionalConverter<Int?> =
//    arg(
//        displayName,
//        description,
//        IntConverter(radix)
//            .toOptional(outputError = outputError, nestedValidator = validator)
//    )
//
// /**
// * Create a defaulting integer converter, for single arguments.
// *
// * @see IntConverter
// */
// public fun Arguments.defaultingInt(
//    displayName: String,
//    description: String,
//    defaultValue: Int,
//    radix: Int = 10,
//    validator: Validator<Int> = null,
// ): DefaultingConverter<Int> =
//    arg(
//        displayName,
//        description,
//        IntConverter(radix)
//            .toDefaulting(defaultValue, nestedValidator = validator)
//    )
//
// /**
// * Create an integer converter, for lists of arguments.
// *
// * @param required Whether command parsing should fail if no arguments could be converted.
// *
// * @see IntConverter
// */
// public fun Arguments.intList(
//    displayName: String,
//    description: String,
//    required: Boolean = true,
//    radix: Int = 10,
//    validator: Validator<List<Int>> = null,
// ): MultiConverter<Int> =
//    arg(
//        displayName,
//        description,
//        IntConverter(radix)
//            .toMulti(required, signatureTypeString = "numbers", nestedValidator = validator)
//    )

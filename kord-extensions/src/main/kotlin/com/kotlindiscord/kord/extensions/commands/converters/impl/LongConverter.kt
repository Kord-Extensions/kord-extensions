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
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for long arguments, converting them into [Long].
 */
@Converter(
    "long",

    types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
    arguments = ["radix: Int = $DEFAULT_RADIX"]
)
@OptIn(KordPreview::class)
public class LongConverter(
    private val radix: Int = DEFAULT_RADIX,
    override var validator: Validator<Long> = null
) : SingleConverter<Long>() {
    override val signatureTypeString: String = "converters.number.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

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

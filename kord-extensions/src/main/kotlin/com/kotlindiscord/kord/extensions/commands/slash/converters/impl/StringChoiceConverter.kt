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
import com.kotlindiscord.kord.extensions.commands.slash.converters.ChoiceConverter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Choice converter for string arguments. Supports mapping up to 25 choices to string.
 */
@Converter(
    "string",

    types = [ConverterType.CHOICE, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
@OptIn(KordPreview::class)
public class StringChoiceConverter(
    choices: Map<String, String>,
    override var validator: Validator<String> = null
) : ChoiceConverter<String>(choices) {
    override val signatureTypeString: String = "converters.string.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, namedArgument: String?): Boolean {
        val arg: String = namedArgument ?: parser?.parseNext()?.data ?: return false

        this.parsed = arg

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply {
            required = true

            this@StringChoiceConverter.choices.forEach { choice(it.key, it.value) }
        }
}

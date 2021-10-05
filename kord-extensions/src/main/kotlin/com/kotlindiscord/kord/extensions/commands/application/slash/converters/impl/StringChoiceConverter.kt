@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceConverter
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToDefaulting
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToMulti
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToOptional
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
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

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        this.parsed = arg

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply {
            required = true

            this@StringChoiceConverter.choices.forEach { choice(it.key, it.value) }
        }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false
        this.parsed = optionValue

        return true
    }
}

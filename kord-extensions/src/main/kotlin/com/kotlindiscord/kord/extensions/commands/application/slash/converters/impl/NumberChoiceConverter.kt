@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
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
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Choice converter for integer arguments. Supports mapping up to 25 choices to integers.
 *
 * Discord doesn't support longs or floating point types, so this is the only numeric type you can use directly.
 */
@Converter(
    "number",

    types = [ConverterType.CHOICE, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.SINGLE],
    arguments = ["radix: Int = $DEFAULT_RADIX"]
)
@OptIn(KordPreview::class)
public
class NumberChoiceConverter(
    private val radix: Int = DEFAULT_RADIX,
    choices: Map<String, Int>,
    override var validator: Validator<Int> = null
) : ChoiceConverter<Int>(choices) {
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

            throw DiscordRelayedException(errorString)
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        IntChoiceBuilder(arg.displayName, arg.description).apply {
            required = true

            this@NumberChoiceConverter.choices.forEach { choice(it.key, it.value) }
        }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.IntOptionValue)?.value ?: return false
        this.parsed = optionValue

        return true
    }
}

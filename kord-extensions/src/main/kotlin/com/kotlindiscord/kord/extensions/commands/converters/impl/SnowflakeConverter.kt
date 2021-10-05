@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for long arguments, converting them into [Long].
 *
 * @see long
 * @see longList
 */
@Converter(
    "snowflake",

    types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
@OptIn(KordPreview::class)
public class SnowflakeConverter(
    override var validator: Validator<Snowflake> = null
) : SingleConverter<Snowflake>() {
    override val signatureTypeString: String = "converters.snowflake.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        try {
            this.parsed = Snowflake(arg)
        } catch (e: NumberFormatException) {
            throw DiscordRelayedException(
                context.translate("converters.snowflake.error.invalid", replacements = arrayOf(arg))
            )
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false

        try {
            this.parsed = Snowflake(optionValue)
        } catch (e: NumberFormatException) {
            throw DiscordRelayedException(
                context.translate("converters.snowflake.error.invalid", replacements = arrayOf(optionValue))
            )
        }

        return true
    }
}

@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

@file:Suppress("MagicNumber", "RethrowCaughtException", "TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.parsers.ColorParser
import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for colours, converting them into [Color] objects.
 *
 * Supports hex codes prefixed with `#` or `0x`, plain RGB integers, or colour names matching the Discord colour
 * palette.
 */
@Converter(
    "color", "colour",
    types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
)
@OptIn(KordPreview::class)
public class ColorConverter(
    override var validator: Validator<Color> = null
) : SingleConverter<Color>() {
    override val signatureTypeString: String = "converters.color.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        try {
            when {
                arg.startsWith("#") -> this.parsed = Color(arg.substring(1).toInt(16))
                arg.startsWith("0x") -> this.parsed = Color(arg.substring(2).toInt(16))
                arg.all { it.isDigit() } -> this.parsed = Color(arg.toInt())

                else -> this.parsed = ColorParser.parse(arg, context.getLocale()) ?: throw DiscordRelayedException(
                    context.translate("converters.color.error.unknown", replacements = arrayOf(arg))
                )
            }
        } catch (e: DiscordRelayedException) {
            throw e
        } catch (t: Throwable) {
            throw DiscordRelayedException(
                context.translate("converters.color.error.unknownOrFailed", replacements = arrayOf(arg))
            )
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false

        try {
            when {
                optionValue.startsWith("#") -> this.parsed =
                    Color(optionValue.substring(1).toInt(16))

                optionValue.startsWith("0x") -> this.parsed =
                    Color(optionValue.substring(2).toInt(16))

                optionValue.all { it.isDigit() } -> this.parsed =
                    Color(optionValue.toInt())

                else -> this.parsed =
                    ColorParser.parse(optionValue, context.getLocale()) ?: throw DiscordRelayedException(
                        context.translate("converters.color.error.unknown", replacements = arrayOf(optionValue))
                    )
            }
        } catch (e: DiscordRelayedException) {
            throw e
        } catch (t: Throwable) {
            throw DiscordRelayedException(
                context.translate("converters.color.error.unknownOrFailed", replacements = arrayOf(optionValue))
            )
        }

        return true
    }
}

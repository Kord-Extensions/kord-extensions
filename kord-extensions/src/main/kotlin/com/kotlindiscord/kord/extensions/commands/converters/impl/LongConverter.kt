/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.i18n.DEFAULT_KORDEX_BUNDLE
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.entity.interaction.IntegerOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.IntegerOptionBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for long arguments, converting them into [Long].
 *
 * @property maxValue The maximum value allowed for this argument.
 * @property minValue The minimum value allowed for this argument.
 */
@Converter(
    "long",

    types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

    builderFields = [
        "public var radix: Int = $DEFAULT_RADIX",

        "public var maxValue: Long? = null",
        "public var minValue: Long? = null",
    ]
)
public class LongConverter(
    private val radix: Int = DEFAULT_RADIX,
    public val maxValue: Long? = null,
    public val minValue: Long? = null,

    override var validator: Validator<Long> = null
) : SingleConverter<Long>() {
    override val signatureTypeString: String = "converters.number.signatureType"
    override val bundle: String = DEFAULT_KORDEX_BUNDLE

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

            throw DiscordRelayedException(errorString)
        }

        if (minValue != null && this.parsed < minValue) {
            throw DiscordRelayedException(
                context.translate(
                    "converters.number.error.invalid.tooSmall",
                    replacements = arrayOf(arg, minValue)
                )
            )
        }

        if (maxValue != null && this.parsed > maxValue) {
            throw DiscordRelayedException(
                context.translate(
                    "converters.number.error.invalid.tooLarge",
                    replacements = arrayOf(arg, maxValue)
                )
            )
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        IntegerOptionBuilder(arg.displayName, arg.description).apply {
            this@apply.maxValue = this@LongConverter.maxValue
            this@apply.minValue = this@LongConverter.minValue

            required = true
        }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? IntegerOptionValue)?.value ?: return false
        this.parsed = optionValue

        return true
    }
}

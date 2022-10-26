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
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.entity.interaction.NumberOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.NumberOptionBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

/**
 * Argument converter for decimal arguments, converting them into [Double].
 *
 * @property maxValue The maximum value allowed for this argument.
 * @property minValue The minimum value allowed for this argument.
 *
 * @see decimal
 * @see decimalList
 */
@Converter(
    "decimal",

    types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

    builderFields = [
        "public var maxValue: Double? = null",
        "public var minValue: Double? = null",
    ],
)
public class DecimalConverter(
    public val maxValue: Double? = null,
    public val minValue: Double? = null,

    override var validator: Validator<Double> = null
) : SingleConverter<Double>() {
    override val signatureTypeString: String = "converters.decimal.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        try {
            this.parsed = arg.toDouble()
        } catch (e: NumberFormatException) {
            throw DiscordRelayedException(
                context.translate("converters.decimal.error.invalid", replacements = arrayOf(arg))
            )
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
        NumberOptionBuilder(arg.displayName, arg.description).apply {
            this@apply.maxValue = this@DecimalConverter.maxValue
            this@apply.minValue = this@DecimalConverter.minValue

            required = true
        }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? NumberOptionValue)?.value ?: return false
        this.parsed = optionValue

        return true
    }
}

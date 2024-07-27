/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.core.entity.interaction.IntegerOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.IntegerOptionBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.i18n.DEFAULT_KORDEX_BUNDLE
import dev.kordex.parser.StringParser

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for integer arguments, converting them into [Int].
 *
 * @property maxValue The maximum value allowed for this argument.
 * @property minValue The minimum value allowed for this argument.
 */
@Converter(
	"int",

	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

	builderFields = [
		"public var radix: Int = $DEFAULT_RADIX",

		"public var maxValue: Int? = null",
		"public var minValue: Int? = null",
	]
)
public class IntConverter(
	private val radix: Int = DEFAULT_RADIX,
	public val maxValue: Int? = null,
	public val minValue: Int? = null,

	override var validator: Validator<Int> = null,
) : SingleConverter<Int>() {
	override val signatureTypeString: String = "converters.number.signatureType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

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
			this@apply.maxValue = this@IntConverter.maxValue?.toLong()
			this@apply.minValue = this@IntConverter.minValue?.toLong()

			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? IntegerOptionValue)?.value ?: return false
		this.parsed = optionValue.toInt()

		return true
	}
}

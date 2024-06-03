/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.utils.getIgnoringCase
import dev.kord.core.entity.interaction.IntegerOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.IntegerOptionBuilder
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
	builderFields = ["public var radix: Int = $DEFAULT_RADIX"]
)
public class NumberChoiceConverter(
	private val radix: Int = DEFAULT_RADIX,
	choices: Map<String, Long>,
	override var validator: Validator<Long> = null,
) : ChoiceConverter<Long>(choices) {
	override val signatureTypeString: String = "converters.number.signatureType"

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false
		val choiceValue = choices.getIgnoringCase(arg, context.getLocale())

		if (choiceValue != null) {
			this.parsed = choiceValue

			return true
		}

		try {
			val result = arg.toLong(radix)

			if (result !in choices.values) {
				throw DiscordRelayedException(
					context.translate(
						"converters.choice.invalidChoice",

						replacements = arrayOf(
							arg,
							choices.entries.joinToString { "**${it.key}** -> `${it.value}`" }
						)
					)
				)
			}

			this.parsed = result
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
		IntegerOptionBuilder(arg.displayName, arg.description).apply {
			required = true

			this@NumberChoiceConverter.choices.forEach { choice(it.key, it.value) }
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? IntegerOptionValue)?.value ?: return false
		this.parsed = optionValue

		return true
	}
}

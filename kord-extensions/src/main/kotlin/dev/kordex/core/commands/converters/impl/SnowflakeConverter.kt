/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.i18n.DEFAULT_KORDEX_BUNDLE
import dev.kordex.parser.StringParser

/**
 * Argument converter for Discord ID arguments, converting them into [Snowflake].
 *
 * @see defaultingSnowflake
 * @see optionalSnowflake
 * @see snowflake
 * @see snowflakeList
 */
@Converter(
	"snowflake",

	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
public class SnowflakeConverter(
	override var validator: Validator<Snowflake> = null,
) : SingleConverter<Snowflake>() {
	override val signatureTypeString: String = "converters.snowflake.signatureType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

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
		val optionValue = (option as? StringOptionValue)?.value ?: return false

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

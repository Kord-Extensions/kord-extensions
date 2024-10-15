/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapOption
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
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
	override val signatureType: Key = CoreTranslations.Converters.Snowflake.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			this.parsed = Snowflake(arg)
		} catch (_: NumberFormatException) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.Snowflake.Error.invalid
					.withContext(context)
					.withOrdinalPlaceholders(arg)
			)
		}

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false

		try {
			this.parsed = Snowflake(optionValue)
		} catch (_: NumberFormatException) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.Snowflake.Error.invalid
					.withContext(context)
					.withOrdinalPlaceholders(optionValue)
			)
		}

		return true
	}
}

/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

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
import dev.kordex.core.time.TimestampType
import dev.kordex.core.time.toDiscord
import dev.kordex.parser.StringParser
import kotlinx.datetime.Instant

private const val TIMESTAMP_PREFIX = "<t:"
private const val TIMESTAMP_SUFFIX = ">"

/**
 * Argument converter for discord-formatted timestamp arguments.
 */
@Converter(
	"timestamp",

	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
public class TimestampConverter(
	override var validator: Validator<FormattedTimestamp> = null,
) : SingleConverter<FormattedTimestamp>() {
	override val signatureType: Key = CoreTranslations.Converters.Timestamp.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		this.parsed = parseFromString(arg) ?: throw DiscordRelayedException(
			CoreTranslations.Converters.Timestamp.Error.invalid
				.withContext(context)
				.withOrdinalPlaceholders(arg)
		)

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false
		this.parsed = parseFromString(optionValue) ?: throw DiscordRelayedException(
			CoreTranslations.Converters.Timestamp.Error.invalid
				.withContext(context)
				.withOrdinalPlaceholders(optionValue)
		)

		return true
	}

	internal companion object {
		internal fun parseFromString(string: String): FormattedTimestamp? {
			if (string.startsWith(TIMESTAMP_PREFIX) && string.endsWith(TIMESTAMP_SUFFIX)) {
				val inner = string.removeSurrounding(TIMESTAMP_PREFIX, TIMESTAMP_SUFFIX).split(":")
				val epochSeconds = inner.getOrNull(0)
				val format = inner.getOrNull(1)

				return FormattedTimestamp(
					Instant.fromEpochSeconds(epochSeconds?.toLongOrNull() ?: return null),
					TimestampType.fromFormatSpecifier(format) ?: return null
				)
			} else {
				return null
			}
		}
	}
}

/**
 * Container class for a timestamp and format, as expected by Discord.
 *
 * @param instant The timestamp this represents
 * @param format Which format to display the timestamp in
 */
public data class FormattedTimestamp(val instant: Instant, val format: TimestampType) {
	/**
	 * Format the timestamp using the format into Discord's special format.
	 */
	public fun toDiscord(): String = instant.toDiscord(format)
}

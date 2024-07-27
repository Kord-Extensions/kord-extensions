/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.converters.impl

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
	override val signatureTypeString: String = "converters.timestamp.signatureType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		this.parsed = parseFromString(arg) ?: throw DiscordRelayedException(
			context.translate(
				"converters.timestamp.error.invalid",
				replacements = arrayOf(arg)
			)
		)

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false
		this.parsed = parseFromString(optionValue) ?: throw DiscordRelayedException(
			context.translate(
				"converters.timestamp.error.invalid",
				replacements = arrayOf(optionValue)
			)
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

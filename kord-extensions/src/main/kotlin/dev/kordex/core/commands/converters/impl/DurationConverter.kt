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
import dev.kordex.core.parsers.DurationParser
import dev.kordex.core.parsers.DurationParserException
import dev.kordex.core.parsers.InvalidTimeUnitException
import dev.kordex.parser.StringParser
import kotlinx.datetime.*

/**
 * Argument converter for Kotlin [DateTimePeriod] arguments. You can apply these to an `Instant` using `plus` and a
 * timezone.
 * Also accepts discord-formatted timestamps, in which case the DateTimePeriod will be the time until the timestamp.
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 * @param positiveOnly Whether a positive duration is required - `true` by default.
 */
@Converter(
	names = ["duration"],
	types = [ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.SINGLE],
	imports = ["kotlinx.datetime.*"],

	builderFields = [
		"public var longHelp: Boolean = true",
		"public var positiveOnly: Boolean = true"
	],
)
public class DurationConverter(
	public val longHelp: Boolean = true,
	public val positiveOnly: Boolean = true,
	override var validator: Validator<DateTimePeriod> = null,
) : SingleConverter<DateTimePeriod>() {
	override val signatureTypeString: String = "converters.duration.error.signatureType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			// Check if it's a discord-formatted timestamp first
			val timestamp = TimestampConverter.parseFromString(arg)
			val result: DateTimePeriod = if (timestamp == null) {
				DurationParser.parse(arg, context.getLocale())
			} else {
				(timestamp.instant - Clock.System.now()).toDateTimePeriod()
			}

			if (positiveOnly) {
				val now: Instant = Clock.System.now()
				val applied: Instant = now.plus(result, TimeZone.UTC)

				if (now > applied) {
					throw DiscordRelayedException(context.translate("converters.duration.error.positiveOnly"))
				}
			}

			parsed = result
		} catch (e: InvalidTimeUnitException) {
			val message: String = context.translate(
				"converters.duration.error.invalidUnit",
				replacements = arrayOf(e.unit)
			) + if (longHelp) "\n\n" + context.translate("converters.duration.help") else ""

			throw DiscordRelayedException(message)
		} catch (e: DurationParserException) {
			throw DiscordRelayedException(e.error)
		}

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false

		try {
			val result: DateTimePeriod = DurationParser.parse(optionValue, context.getLocale())

			if (positiveOnly) {
				val now: Instant = Clock.System.now()
				val applied: Instant = now.plus(result, TimeZone.UTC)

				if (now > applied) {
					throw DiscordRelayedException(context.translate("converters.duration.error.positiveOnly"))
				}
			}

			parsed = result
		} catch (e: InvalidTimeUnitException) {
			val message: String = context.translate(
				"converters.duration.error.invalidUnit",
				replacements = arrayOf(e.unit)
			) + if (longHelp) "\n\n" + context.translate("converters.duration.help") else ""

			throw DiscordRelayedException(message)
		} catch (e: DurationParserException) {
			throw DiscordRelayedException(e.error)
		}

		return true
	}
}

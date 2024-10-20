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
	// TODO: The signature type is not an error!
	override val signatureType: Key = CoreTranslations.Converters.Duration.Error.signatureType

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
					throw DiscordRelayedException(
						CoreTranslations.Converters.Duration.Error.positiveOnly
							.withContext(context)
					)
				}
			}

			parsed = result
		} catch (e: InvalidTimeUnitException) {
			throw DiscordRelayedException(
				if (longHelp) {
					CoreTranslations.Common.paragraphJoiner
						.withLocale(context.getLocale())
						.withOrdinalPlaceholders(
							CoreTranslations.Converters.Duration.Error.invalidUnit
								.withLocale(context.getLocale())
								.withOrdinalPlaceholders(e.unit),

							CoreTranslations.Converters.Duration.help
								.withLocale(context.getLocale())
						)
				} else {
					CoreTranslations.Converters.Duration.Error.invalidUnit
						.withLocale(context.getLocale())
						.withOrdinalPlaceholders(e.unit)
				}
			)
		} catch (e: DurationParserException) {
			throw DiscordRelayedException(e.error)
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
			val result: DateTimePeriod = DurationParser.parse(optionValue, context.getLocale())

			if (positiveOnly) {
				val now: Instant = Clock.System.now()
				val applied: Instant = now.plus(result, TimeZone.UTC)

				if (now > applied) {
					throw DiscordRelayedException(
						CoreTranslations.Converters.Duration.Error.positiveOnly
							.withContext(context)
					)
				}
			}

			parsed = result
		} catch (e: InvalidTimeUnitException) {
			throw DiscordRelayedException(
				if (longHelp) {
					CoreTranslations.Common.paragraphJoiner
						.withLocale(context.getLocale())
						.withOrdinalPlaceholders(
							CoreTranslations.Converters.Duration.Error.invalidUnit
								.withLocale(context.getLocale())
								.withOrdinalPlaceholders(e.unit),

							CoreTranslations.Converters.Duration.help
								.withLocale(context.getLocale())
						)
				} else {
					CoreTranslations.Converters.Duration.Error.invalidUnit
						.withLocale(context.getLocale())
						.withOrdinalPlaceholders(e.unit)
				}
			)
		} catch (e: DurationParserException) {
			throw DiscordRelayedException(e.error)
		}

		return true
	}
}

/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.java.time

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
import dev.kordex.core.parsers.DurationParserException
import dev.kordex.core.parsers.InvalidTimeUnitException
import dev.kordex.parser.StringParser
import java.time.Duration
import java.time.LocalDateTime

/**
 * Argument converter for Java 8 [Duration] arguments.
 *
 * For a coalescing version of this converter, see [J8DurationCoalescingConverter].
 * If you're using Time4J instead, see [T4JDurationConverter].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 * @param positiveOnly Whether a positive duration is required - `true` by default.
 */
@Converter(
	names = ["j8Duration"],
	types = [ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.SINGLE],
	imports = ["java.time.*"],

	builderFields = [
		"public var longHelp: Boolean = true",
		"public var positiveOnly: Boolean = true",
	],
)
public class J8DurationConverter(
	public val longHelp: Boolean = true,
	public val positiveOnly: Boolean = true,
	override var validator: Validator<ChronoContainer> = null,
) : SingleConverter<ChronoContainer>() {
	override val signatureType: Key = CoreTranslations.Converters.Duration.Error.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			val result: ChronoContainer = J8DurationParser.parse(arg, context.getLocale())

			if (positiveOnly) {
				val normalized: ChronoContainer = result.clone()

				normalized.normalize(LocalDateTime.now())

				if (!normalized.isPositive()) {
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
		val arg: String = (option as? StringOptionValue)?.value ?: return false

		try {
			val result: ChronoContainer = J8DurationParser.parse(arg, context.getLocale())

			if (positiveOnly) {
				val normalized: ChronoContainer = result.clone()

				normalized.normalize(LocalDateTime.now())

				if (!normalized.isPositive()) {
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

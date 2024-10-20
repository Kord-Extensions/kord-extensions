/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.time4j

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
import dev.kordex.core.parsers.DurationParserException
import dev.kordex.core.parsers.InvalidTimeUnitException
import dev.kordex.parser.StringParser
import net.time4j.Duration
import net.time4j.IsoUnit

/**
 * Argument converter for Time4J [Duration] arguments.
 *
 * For a coalescing version of this converter, see [T4JDurationCoalescingConverter].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 *
 * @see t4jDuration
 * @see t4jDurationList
 * @see parseT4JDuration
 */
@Converter(
	names = ["t4JDuration"],
	types = [ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.SINGLE],
	imports = ["net.time4j.*"],

	builderFields = [
		"public var longHelp: Boolean = true",
	],
)
public class T4JDurationConverter(
	public val longHelp: Boolean = true,
	override var validator: Validator<Duration<IsoUnit>> = null,
) : SingleConverter<Duration<IsoUnit>>() {
	override val signatureType: Key = CoreTranslations.Converters.Duration.Error.signatureType

	init {
		bot.settings.aboutBuilder.addCopyright()
	}

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			this.parsed = T4JDurationParser.parse(arg, context.getLocale())
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
			this.parsed = T4JDurationParser.parse(arg, context.getLocale())
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

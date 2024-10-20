/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.sentry

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
import io.sentry.protocol.SentryId

/**
 * Argument converter for Sentry event ID arguments.
 *
 * @see sentryId
 * @see sentryIdList
 */
@Converter(
	"sentryId",
	types = [ConverterType.SINGLE, ConverterType.LIST, ConverterType.OPTIONAL]
)
public class SentryIdConverter(
	override var validator: Validator<SentryId> = null,
) : SingleConverter<SentryId>() {
	override val signatureType: Key = CoreTranslations.Extensions.Sentry.Converter.SentryId.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			this.parsed = SentryId(arg)
		} catch (_: IllegalArgumentException) {
			throw DiscordRelayedException(
				CoreTranslations.Extensions.Sentry.Converter.Error.invalid
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
			this.parsed = SentryId(optionValue)
		} catch (_: IllegalArgumentException) {
			throw DiscordRelayedException(
				CoreTranslations.Extensions.Sentry.Converter.Error.invalid
					.withContext(context)
					.withOrdinalPlaceholders(optionValue)
			)
		}

		return true
	}
}

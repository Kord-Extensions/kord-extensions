/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("MagicNumber", "RethrowCaughtException", "TooGenericExceptionCaught")

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
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.parser.StringParser
import java.util.*

/**
 * Argument converter for supported locale, converting them into [Locale] objects.
 *
 * This converter only supports locales defined in [dev.kordex.core.i18n.SupportedLocales]. It's
 * intended for use with commands that allow users to specify what locale they want the bot to use when interacting
 * with them, rather than a more general converter.
 *
 * If the locale you want to use isn't supported yet, feel free to contribute translations for it to
 * [our Weblate project](https://hosted.weblate.org/projects/kord-extensions/main/).
 */
@Converter(
	"supportedLocale",
	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
)
public class SupportedLocaleConverter(
	override var validator: Validator<Locale> = null,
) : SingleConverter<Locale>() {
	override val signatureType: Key = CoreTranslations.Converters.SupportedLocale.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		this.parsed = SupportedLocales.ALL_LOCALES[arg.lowercase().trim()] ?: throw DiscordRelayedException(
			CoreTranslations.Converters.SupportedLocale.Error.unknown
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

		this.parsed = SupportedLocales.ALL_LOCALES[optionValue.lowercase().trim()] ?: throw DiscordRelayedException(
			CoreTranslations.Converters.SupportedLocale.Error.unknown
				.withContext(context)
				.withOrdinalPlaceholders(optionValue)
		)

		return true
	}
}

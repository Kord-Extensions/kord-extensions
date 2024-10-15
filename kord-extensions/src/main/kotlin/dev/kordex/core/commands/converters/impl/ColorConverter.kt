/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("MagicNumber", "RethrowCaughtException", "TooGenericExceptionCaught")

package dev.kordex.core.commands.converters.impl

import dev.kord.common.Color
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
import dev.kordex.core.parsers.ColorParser
import dev.kordex.parser.StringParser

/**
 * Argument converter for colours, converting them into [Color] objects.
 *
 * Supports hex codes prefixed with `#` or `0x`, plain RGB integers, or colour names matching the Discord colour
 * palette.
 */
@Converter(
	"color", "colour",
	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
)
public class ColorConverter(
	override var validator: Validator<Color> = null,
) : SingleConverter<Color>() {
	override val signatureType: Key = CoreTranslations.Converters.Color.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			when {
				arg.startsWith("#") ->
					this.parsed = Color(arg.substring(1).toInt(16))

				arg.startsWith("0x") ->
					this.parsed = Color(arg.substring(2).toInt(16))

				arg.all { it.isDigit() } ->
					this.parsed = Color(arg.toInt())

				else -> this.parsed = ColorParser.parse(arg, context.getLocale())
					?: throw DiscordRelayedException(
						CoreTranslations.Converters.Color.Error.unknown
							.withContext(context)
							.withOrdinalPlaceholders(arg)
					)
			}
		} catch (e: DiscordRelayedException) {
			throw e
		} catch (_: Throwable) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.Color.Error.unknownOrFailed
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
			when {
				optionValue.startsWith("#") ->
					this.parsed = Color(optionValue.substring(1).toInt(16))

				optionValue.startsWith("0x") ->
					this.parsed = Color(optionValue.substring(2).toInt(16))

				optionValue.all { it.isDigit() } ->
					this.parsed = Color(optionValue.toInt())

				else ->
					this.parsed = ColorParser.parse(optionValue, context.getLocale())
						?: throw DiscordRelayedException(
							CoreTranslations.Converters.Color.Error.unknown
								.withContext(context)
								.withOrdinalPlaceholders(optionValue)
						)
			}
		} catch (e: DiscordRelayedException) {
			throw e
		} catch (_: Throwable) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.Color.Error.unknownOrFailed
					.withContext(context)
					.withOrdinalPlaceholders(optionValue)
			)
		}

		return true
	}
}

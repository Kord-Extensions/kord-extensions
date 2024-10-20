/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.core.entity.interaction.NumberOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.NumberOptionBuilder
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
import kotlinx.coroutines.withContext

/**
 * Argument converter for decimal arguments, converting them into [Double].
 *
 * @property maxValue The maximum value allowed for this argument.
 * @property minValue The minimum value allowed for this argument.
 *
 * @see decimal
 * @see decimalList
 */
@Converter(
	"decimal",

	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

	builderFields = [
		"public var maxValue: Double? = null",
		"public var minValue: Double? = null",
	],
)
public class DecimalConverter(
	public val maxValue: Double? = null,
	public val minValue: Double? = null,

	override var validator: Validator<Double> = null,
) : SingleConverter<Double>() {
	override val signatureType: Key = CoreTranslations.Converters.Decimal.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			this.parsed = arg.toDouble()
		} catch (_: NumberFormatException) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.Decimal.Error.invalid
					.withContext(context)
			)
		}

		if (minValue != null && this.parsed < minValue) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.Number.Error.Invalid.tooSmall
					.withContext(context)
					.withOrdinalPlaceholders(arg, minValue)
			)
		}

		if (maxValue != null && this.parsed > maxValue) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.Number.Error.Invalid.tooLarge
					.withContext(context)
					.withOrdinalPlaceholders(arg, maxValue)
			)
		}

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<NumberOptionBuilder> =
		wrapOption(arg.displayName, arg.description) {
			this.maxValue = this@DecimalConverter.maxValue
			this.minValue = this@DecimalConverter.minValue

			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? NumberOptionValue)?.value ?: return false
		this.parsed = optionValue

		return true
	}
}

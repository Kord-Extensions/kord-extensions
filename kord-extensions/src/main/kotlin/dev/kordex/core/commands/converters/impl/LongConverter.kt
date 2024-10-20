/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.core.entity.interaction.IntegerOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.IntegerOptionBuilder
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

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for long arguments, converting them into [Long].
 *
 * @property maxValue The maximum value allowed for this argument.
 * @property minValue The minimum value allowed for this argument.
 */
@Converter(
	"long",

	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

	builderFields = [
		"public var radix: Int = $DEFAULT_RADIX",

		"public var maxValue: Long? = null",
		"public var minValue: Long? = null",
	]
)
public class LongConverter(
	private val radix: Int = DEFAULT_RADIX,
	public val maxValue: Long? = null,
	public val minValue: Long? = null,

	override var validator: Validator<Long> = null,
) : SingleConverter<Long>() {
	override val signatureType: Key = CoreTranslations.Converters.Number.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			this.parsed = arg.toLong(radix)
		} catch (_: NumberFormatException) {
			val errorKey = if (radix == DEFAULT_RADIX) {
				CoreTranslations.Converters.Number.Error.Invalid.defaultBase
					.withContext(context)
					.withOrdinalPlaceholders(arg)
			} else {
				CoreTranslations.Converters.Number.Error.Invalid.otherBase
					.withContext(context)
					.withOrdinalPlaceholders(arg, radix)
			}

			throw DiscordRelayedException(errorKey)
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

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<IntegerOptionBuilder> =
		wrapOption(arg.displayName, arg.description) {
			this.maxValue = this@LongConverter.maxValue
			this.minValue = this@LongConverter.minValue

			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? IntegerOptionValue)?.value ?: return false
		this.parsed = optionValue

		return true
	}
}

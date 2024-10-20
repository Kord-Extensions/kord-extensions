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
 * Argument converter for integer arguments, converting them into [Int].
 *
 * @property maxValue The maximum value allowed for this argument.
 * @property minValue The minimum value allowed for this argument.
 */
@Converter(
	"int",

	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

	builderFields = [
		"public var radix: Int = $DEFAULT_RADIX",

		"public var maxValue: Int? = null",
		"public var minValue: Int? = null",
	]
)
public class IntConverter(
	private val radix: Int = DEFAULT_RADIX,
	public val maxValue: Int? = null,
	public val minValue: Int? = null,

	override var validator: Validator<Int> = null,
) : SingleConverter<Int>() {
	override val signatureType: Key = CoreTranslations.Converters.Number.signatureType

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			this.parsed = arg.toInt(radix)
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
			this.maxValue = this@IntConverter.maxValue?.toLong()
			this.minValue = this@IntConverter.minValue?.toLong()

			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? IntegerOptionValue)?.value ?: return false
		this.parsed = optionValue.toInt()

		return true
	}
}

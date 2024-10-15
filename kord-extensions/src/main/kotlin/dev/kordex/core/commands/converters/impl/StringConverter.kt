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
import dev.kordex.parser.StringParser

/**
 * Coalescing argument that simply returns the argument as it was given.
 *
 * The multi version of this converter (via [toList]) will consume all remaining arguments.
 *
 * @property maxLength The maximum length allowed for this argument.
 * @property minLength The minimum length allowed for this argument.
 */
@Converter(
	"string",

	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

	builderFields = [
		"public var maxLength: Int? = null",
		"public var minLength: Int? = null",
	]
)
public class StringConverter(
	public val maxLength: Int? = null,
	public val minLength: Int? = null,
	override var validator: Validator<String> = null,
) : SingleConverter<String>() {
	override val signatureType: Key = CoreTranslations.Converters.String.signatureType
	override val showTypeInSignature: Boolean = false

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		this.parsed = arg

		if (minLength != null && this.parsed.length < minLength) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.String.Error.Invalid.tooShort
					.withContext(context)
					.withOrdinalPlaceholders(arg, minLength)
			)
		}

		if (maxLength != null && this.parsed.length > maxLength) {
			throw DiscordRelayedException(
				CoreTranslations.Converters.String.Error.Invalid.tooLong
					.withContext(context)
					.withOrdinalPlaceholders(arg, maxLength)
			)
		}

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			this.maxLength = this@StringConverter.maxLength
			this.minLength = this@StringConverter.minLength

			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false
		this.parsed = optionValue

		return true
	}
}

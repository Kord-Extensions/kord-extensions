/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.slash.converters.impl

import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.application.slash.converters.ChoiceConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.utils.getIgnoringCase
import dev.kordex.parser.StringParser

/**
 * Choice converter for string arguments. Supports mapping up to 25 choices to string.
 */
@Converter(
	"string",

	types = [ConverterType.CHOICE, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
public class StringChoiceConverter(
	choices: Map<String, String>,
	override var validator: Validator<String> = null,
) : ChoiceConverter<String>(choices) {
	override val signatureTypeString: String = "converters.string.signatureType"

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false
		val choiceValue = choices.getIgnoringCase(arg, context.getLocale())

		if (choiceValue != null) {
			this.parsed = choiceValue

			return true
		}

		if (arg.lowercase(context.getLocale()) !in choices.values.map { it.lowercase(context.getLocale()) }) {
			throw DiscordRelayedException(
				context.translate(
					"converters.choice.invalidChoice",

					replacements = arrayOf(
						arg,
						choices.entries.joinToString { "**${it.key}** -> `${it.value}`" }
					)
				)
			)
		}

		this.parsed = arg

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply {
			required = true

			this@StringChoiceConverter.choices.forEach { choice(it.key, it.value) }
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false
		this.parsed = optionValue

		return true
	}
}

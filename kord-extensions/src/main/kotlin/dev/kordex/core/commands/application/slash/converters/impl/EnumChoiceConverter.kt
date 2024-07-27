/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
import dev.kordex.core.commands.application.slash.converters.ChoiceEnum
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.utils.getIgnoringCase
import dev.kordex.parser.StringParser
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Choice converter for enum arguments. Supports mapping up to 25 choices to an enum type.
 *
 * All enums used for this must implement the [ChoiceEnum] interface.
 */
@Converter(
	"enum",

	types = [ConverterType.SINGLE, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.CHOICE],
	imports = [
		"dev.kordex.core.commands.converters.impl.getEnum",
		"dev.kordex.core.commands.application.slash.converters.ChoiceEnum"
	],

	builderGeneric = "E",
	builderConstructorArguments = [
		"public var getter: suspend (String) -> E?",
		"!! argMap: Map<String, E>",
	],

	builderFields = [
		"public lateinit var typeName: String",
		"public var bundle: String? = null"
	],

	builderInitStatements = [
		"choices(argMap)"
	],

	builderSuffixedWhere = "E : Enum<E>, E : ChoiceEnum",

	functionGeneric = "E",
	functionBuilderArguments = [
		"getter = { getEnum(it) }",
		"argMap = enumValues<E>().associateBy { it.readableName }",
	],

	functionSuffixedWhere = "E : Enum<E>, E : ChoiceEnum"
)
public class EnumChoiceConverter<E>(
	typeName: String,
	private val getter: suspend (String) -> E?,
	choices: Map<String, E>,
	override var validator: Validator<E> = null,
	override val bundle: String? = null,
) : ChoiceConverter<E>(choices) where E : Enum<E>, E : ChoiceEnum {
	override val signatureTypeString: String = typeName

	private val logger = KotlinLogging.logger { }

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false
		val choiceValue = choices.getIgnoringCase(arg, context.getLocale())

		if (null != choiceValue) {
			// The conditional looks weird, but it won't compile otherwise
			this.parsed = choiceValue

			return true
		}

		try {
			val result = getter.invoke(arg)
				?: throw DiscordRelayedException(
					context.translate(
						"converters.choice.invalidChoice",

						replacements = arrayOf(
							arg,
							choices.entries.joinToString { "**${it.key}** -> `${it.value}`" }
						)
					)
				)

			this.parsed = result
		} catch (e: IllegalArgumentException) {
			logger.warn(e) { "Failed to get enum value for argument: $arg" }

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

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply {
			required = true

			this@EnumChoiceConverter.choices.forEach { choice(it.key, it.value.name) }
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val stringOption = option as? StringOptionValue ?: return false

		try {
			parsed = getter.invoke(stringOption.value) ?: return false
		} catch (e: IllegalArgumentException) {
			return false
		}

		return true
	}
}

/**
 * The default choice enum value getter - matches choice enums via a case-insensitive string comparison with the names.
 */
public inline fun <reified E> getEnum(arg: String): E? where E : Enum<E>, E : ChoiceEnum =
	enumValues<E>().firstOrNull {
		it.readableName.equals(arg, true) ||
			it.name.equals(arg, true)
	}

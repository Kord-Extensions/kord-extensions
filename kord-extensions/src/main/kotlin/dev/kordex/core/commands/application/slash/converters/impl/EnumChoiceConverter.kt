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
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.application.slash.converters.ChoiceConverter
import dev.kordex.core.commands.application.slash.converters.ChoiceEnum
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapStringOption
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.core.utils.getIgnoringCase
import dev.kordex.parser.StringParser
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Locale

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
		"dev.kordex.core.commands.application.slash.converters.ChoiceEnum",
		"java.util.Locale",
	],

	builderGeneric = "E",
	builderConstructorArguments = [
		"public var getter: suspend (String, Locale) -> E?",
		"!! argMap: Map<Key, E>",
	],

	builderFields = [
		"public lateinit var typeName: Key",
	],

	builderInitStatements = [
		"choices(argMap)",
	],

	builderSuffixedWhere = "E : Enum<E>, E : ChoiceEnum",

	functionGeneric = "E",
	functionBuilderArguments = [
		"getter = ::getEnum",
		"argMap = enumValues<E>().associateBy { it.readableName }",
	],

	functionSuffixedWhere = "E : Enum<E>, E : ChoiceEnum",
)
public class EnumChoiceConverter<E>(
	typeName: Key,
	private val getter: suspend (String, Locale) -> E?,
	choices: Map<Key, E>,
	override var validator: Validator<E> = null,
) : ChoiceConverter<E>(choices) where E : Enum<E>, E : ChoiceEnum {
	override val signatureType: Key = typeName

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
			val result = getter.invoke(arg, context.getLocale())
				?: throw DiscordRelayedException(
					CoreTranslations.Converters.Choice.invalidChoice
						.withContext(context)
						.withOrdinalPlaceholders(
							arg,
							choices.entries.joinToString { "**${it.key}** -> `${it.value}`" }
						)
				)

			this.parsed = result
		} catch (e: IllegalArgumentException) {
			logger.warn(e) { "Failed to get enum value for argument: $arg" }

			throw DiscordRelayedException(
				CoreTranslations.Converters.Choice.invalidChoice
					.withContext(context)
					.withOrdinalPlaceholders(
						arg,
						choices.entries.joinToString { "**${it.key}** -> `${it.value}`" }
					)
			)
		}

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> {
		val option = wrapStringOption(arg.displayName, arg.description) {
			required = true
		}

		this.choices.forEach { option.choice(it.key, it.value.name) }

		return option
	}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val stringOption = option as? StringOptionValue ?: return false

		try {
			parsed = getter.invoke(stringOption.value, context.getLocale()) ?: return false
		} catch (_: IllegalArgumentException) {
			return false
		}

		return true
	}
}

/**
 * The default choice enum value getter — matches choice enums via a case-insensitive string comparison with the names.
 */
public inline fun <reified E> getEnum(arg: String, locale: Locale): E? where E : Enum<E>, E : ChoiceEnum =
	enumValues<E>().firstOrNull {
		it.readableName.translateLocale(locale).equals(arg, true) ||
			it.readableName.key.equals(arg, true) ||
			it.name.equals(arg, true)
	}

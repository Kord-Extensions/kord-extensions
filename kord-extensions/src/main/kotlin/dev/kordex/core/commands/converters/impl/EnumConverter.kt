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
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.application.slash.converters.ChoiceEnum
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapOption
import dev.kordex.core.i18n.types.Key
import dev.kordex.parser.StringParser
import java.util.Locale

/**
 * Argument converter for arbitrary enum arguments.
 *
 * As this converter is generic, it takes a getter lambda. You can either provide one yourself, or use the default
 * one via the provided extension functions - the default getter simply checks for case-insensitive matches on enum
 * names.
 *
 * @see enum
 * @see enumList
 */
@Converter(
	"enum",

	types = [ConverterType.SINGLE, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.LIST],
	imports = [
		"dev.kordex.core.commands.converters.impl.getEnum",
		"dev.kordex.core.commands.application.slash.converters.ChoiceEnum",
		"java.util.Locale",
	],

	builderGeneric = "E",
	builderConstructorArguments = [
		"public var getter: suspend (String, Locale) -> E?",
	],

	builderFields = [
		"public lateinit var typeName: Key",
	],

	builderSuffixedWhere = "E : Enum<E>, E : ChoiceEnum",

	functionGeneric = "E",
	functionBuilderArguments = [
		"getter = ::getEnum",
	],

	functionSuffixedWhere = "E : Enum<E>, E : ChoiceEnum",
)
public class EnumConverter<E : Enum<E>>(
	typeName: Key,
	private val getter: suspend (String, Locale) -> E?,
	override var validator: Validator<E> = null,
) : SingleConverter<E>() {
	override val signatureType: Key = typeName

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			parsed = getter.invoke(arg, context.getLocale()) ?: return false
		} catch (_: IllegalArgumentException) {
			return false
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
			parsed = getter.invoke(optionValue, context.getLocale()) ?: return false
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

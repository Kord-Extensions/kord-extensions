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
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.i18n.DEFAULT_KORDEX_BUNDLE
import dev.kordex.parser.StringParser

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
	imports = ["dev.kordex.core.commands.converters.impl.getEnum"],

	builderGeneric = "E: Enum<E>",
	builderConstructorArguments = [
		"public var getter: suspend (String) -> E?"
	],

	builderFields = [
		"public lateinit var typeName: String",
		"public var bundle: String? = null"
	],

	functionGeneric = "E: Enum<E>",
	functionBuilderArguments = [
		"getter = { getEnum(it) }",
	]
)
public class EnumConverter<E : Enum<E>>(
	typeName: String,
	private val getter: suspend (String) -> E?,
	override val bundle: String? = DEFAULT_KORDEX_BUNDLE,
	override var validator: Validator<E> = null,
) : SingleConverter<E>() {
	override val signatureTypeString: String = typeName

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		try {
			parsed = getter.invoke(arg) ?: return false
		} catch (e: IllegalArgumentException) {
			return false
		}

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false

		try {
			parsed = getter.invoke(optionValue) ?: return false
		} catch (e: IllegalArgumentException) {
			return false
		}

		return true
	}
}

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified E : Enum<E>> getEnum(arg: String): E? =
	enumValues<E>().firstOrNull {
		it.name.equals(arg, true)
	}

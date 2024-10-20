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
import dev.kordex.core.commands.converters.CoalescingConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapOption
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.parser.StringParser

/**
 * Coalescing argument converter for regular expression arguments, combining the arguments into a single [Regex]
 * object by joining them with spaces.
 *
 * Please note that user-provided regular expressions are not safe - they can take down your entire bot.
 *
 * As there is no way to validate individual segments of regex, this converter will consume all remaining arguments.
 *
 * @param options Optional set of [RegexOption]s to pass to the regex parser.
 *
 * @see coalescedRegex
 */
@Converter(
	"regex",

	types = [ConverterType.COALESCING, ConverterType.DEFAULTING, ConverterType.OPTIONAL],
	imports = ["kotlin.text.RegexOption"],
	builderFields = ["public var options: Set<RegexOption> = setOf()"]
)
public class RegexCoalescingConverter(
	private val options: Set<RegexOption> = setOf(),
	shouldThrow: Boolean = false,
	override var validator: Validator<Regex> = null,
) : CoalescingConverter<Regex>(shouldThrow) {
	override val signatureType: Key = CoreTranslations.Converters.Regex.SignatureType.plural
	override val showTypeInSignature: Boolean = false

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: List<String>?): Int {
		val args: String = named?.joinToString(" ") ?: parser?.consumeRemaining() ?: return 0

		this.parsed = args.toRegex(options)

		return args.length
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false
		this.parsed = optionValue.toRegex(options)

		return true
	}
}

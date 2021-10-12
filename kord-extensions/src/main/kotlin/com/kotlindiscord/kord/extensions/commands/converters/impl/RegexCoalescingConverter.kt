@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

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

    types = [ConverterType.COALESCING, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.SINGLE],
    imports = ["kotlin.text.RegexOption"],
    arguments = ["options: Set<RegexOption> = setOf()"]
)
public class RegexCoalescingConverter(
    private val options: Set<RegexOption> = setOf(),
    shouldThrow: Boolean = false,
    override var validator: Validator<Regex> = null
) : CoalescingConverter<Regex>(shouldThrow) {
    override val signatureTypeString: String = "converters.regex.signatureType.plural"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: List<String>?): Int {
        val args: String = named?.joinToString(" ") ?: parser?.consumeRemaining() ?: return 0

        this.parsed = args.toRegex(options)

        return args.length
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false
        this.parsed = optionValue.toRegex(options)

        return true
    }
}

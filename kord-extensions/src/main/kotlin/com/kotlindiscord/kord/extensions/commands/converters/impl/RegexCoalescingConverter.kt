@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
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
public class RegexCoalescingConverter(
    private val options: Set<RegexOption> = setOf(),
    shouldThrow: Boolean = false,
    override var validator: (suspend Argument<*>.(Regex) -> Unit)? = null
) : CoalescingConverter<Regex>(shouldThrow) {
    override val signatureTypeString: String = "converters.regex.signatureType.plural"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(args: List<String>, context: CommandContext): Int {
        this.parsed = args.joinToString(" ").toRegex(options)

        return args.size
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedRegex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex) -> Unit)? = null,
): CoalescingConverter<Regex> =
    arg(displayName, description, RegexCoalescingConverter(options, validator = validator))

/**
 * Create an optional coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedRegex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex?) -> Unit)? = null,
): OptionalCoalescingConverter<Regex?> =
    arg(
        displayName,
        description,

        RegexCoalescingConverter(options).toOptional(nestedValidator = validator)
    )

/**
 * Create a defaulting coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedRegex(
    displayName: String,
    description: String,
    defaultValue: Regex,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex) -> Unit)? = null,
): DefaultingCoalescingConverter<Regex> =
    arg(
        displayName,
        description,
        RegexCoalescingConverter(options)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

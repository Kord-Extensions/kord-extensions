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
 * Coalescing argument converter that simply joins all arguments with spaces to produce a single string.
 *
 * This converter will consume all remaining arguments.
 *
 * @see coalescedString
 */
public class StringCoalescingConverter(
    shouldThrow: Boolean = false,
    override var validator: Validator<String> = null
) : CoalescingConverter<String>(shouldThrow) {
    override val signatureTypeString: String = "converters.string.signatureType"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(args: List<String>, context: CommandContext): Int {
        this.parsed = args.joinToString(" ")

        return args.size
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a coalescing string converter.
 *
 * @see StringCoalescingConverter
 */
public fun Arguments.coalescedString(
    displayName:
    String,
    description: String,
    validator: Validator<String> = null,
): CoalescingConverter<String> =
    arg(displayName, description, StringCoalescingConverter(validator = validator))

/**
 * Create an optional coalescing string converter.
 *
 * @see StringCoalescingConverter
 */
public fun Arguments.optionalCoalescedString(
    displayName: String,
    description: String,
    validator: Validator<String?> = null,
): OptionalCoalescingConverter<String?> =
    arg(
        displayName,
        description,

        StringCoalescingConverter().toOptional(nestedValidator = validator)
    )

/**
 * Create a defaulting coalescing string converter.
 *
 * @see StringCoalescingConverter
 */
public fun Arguments.defaultingCoalescedString(
    displayName: String,
    description: String,
    defaultValue: String,
    validator: Validator<String> = null,
): DefaultingCoalescingConverter<String> =
    arg(
        displayName,
        description,
        StringCoalescingConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

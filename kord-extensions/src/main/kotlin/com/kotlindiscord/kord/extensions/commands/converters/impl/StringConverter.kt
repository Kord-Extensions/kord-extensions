package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Coalescing argument that simply returns the argument as it was given.
 *
 * The multi version of this converter (via [toMulti]) will consume all remaining arguments.
 */
@Converter(
    "string",

    types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
@OptIn(KordPreview::class)
public class StringConverter(
    override var validator: Validator<String> = null
) : SingleConverter<String>() {
    override val signatureTypeString: String = "converters.string.signatureType"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(parser: StringParser?, context: CommandContext, namedArgument: String?): Boolean {
        val arg: String = namedArgument ?: parser?.parseNext()?.data ?: return false

        this.parsed = arg

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

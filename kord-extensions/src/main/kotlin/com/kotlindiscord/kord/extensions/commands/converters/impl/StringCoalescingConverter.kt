package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.coalescedString
import com.kotlindiscord.kord.extensions.commands.parser.Argument
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
    shouldThrow: Boolean = false
) :  CoalescingConverter<String>(shouldThrow) {
    override val signatureTypeString: String = "text"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        this.parsed = args.joinToString(" ")

        return args.size
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

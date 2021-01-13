package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.int
import com.kotlindiscord.kord.extensions.commands.converters.intList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for integer arguments, converting them into [Int].
 *
 * @see int
 * @see intList
 */
@OptIn(KordPreview::class)
public class IntConverter(
    private val radix: Int = DEFAULT_RADIX
) : SingleConverter<Int>() {
    override val signatureTypeString: String = "number"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = arg.toInt(radix)
        } catch (e: NumberFormatException) {
            throw ParseException(
                "Value '$arg' is not a valid whole number" + if (radix != DEFAULT_RADIX) " in base-$radix." else "."
            )
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        IntChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

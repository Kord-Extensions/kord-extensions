package com.kotlindiscord.kord.extensions.slash_commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.slash_commands.converters.ChoiceConverter

private const val DEFAULT_RADIX = 10

public class NumberChoiceConverter(
    private val radix: Int = DEFAULT_RADIX
) : ChoiceConverter<Long>() {
    override val signatureTypeString: String = "number"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = arg.toLong(radix)
        } catch (e: NumberFormatException) {
            throw ParseException(
                "Value '$arg' is not a valid whole number" + if (radix != DEFAULT_RADIX) " in base-$radix." else "."
            )
        }

        return true
    }
}


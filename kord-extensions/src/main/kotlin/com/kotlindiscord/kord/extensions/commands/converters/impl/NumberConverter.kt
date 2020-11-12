package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter

class NumberConverter(required: Boolean = true, private val radix: Int = 10) : SingleConverter<Long>(required) {
    override val typeString = "number"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = arg.toLong(radix)
        } catch (e: NumberFormatException) {
            throw ParseException(
                "Value '$arg' is not a valid whole number" + if (radix != 10) " in base-$radix." else "."
            )
        }

        return true
    }
}

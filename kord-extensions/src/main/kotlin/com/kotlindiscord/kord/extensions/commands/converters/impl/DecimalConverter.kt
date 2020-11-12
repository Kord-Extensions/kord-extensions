package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter

class DecimalConverter(required: Boolean = true) : SingleConverter<Double>(required) {
    override val typeString = "decimal"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = arg.toDouble()
        } catch (e: NumberFormatException) {
            throw ParseException(
                "Value '$arg' is not a valid decimal number."
            )
        }

        return true
    }
}

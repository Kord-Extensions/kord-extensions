package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter

class NumberListConverter(required: Boolean = true, private val radix: Int = 10) : MultiConverter<Long>(required) {
    override val typeString = "numbers"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val ints = mutableListOf<Long>()

        for (arg in args) {
            try {
                ints.add(arg.toLong(radix))
            } catch (e: NumberFormatException) {
                break
            }
        }

        parsed = ints.toList()

        return parsed.size
    }
}

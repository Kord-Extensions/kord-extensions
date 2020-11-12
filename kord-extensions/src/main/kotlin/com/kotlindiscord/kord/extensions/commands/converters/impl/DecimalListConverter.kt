package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter

class DecimalListConverter(required: Boolean = true) : MultiConverter<Double>(required) {
    override val signatureTypeString = "decimals"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val doubles = mutableListOf<Double>()

        for (arg in args) {
            try {
                doubles.add(arg.toDouble())
            } catch (e: NumberFormatException) {
                break
            }
        }

        parsed = doubles.toList()

        return parsed.size
    }
}

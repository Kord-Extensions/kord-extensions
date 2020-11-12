package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.utils.parseBoolean

class BooleanListConverter(required: Boolean = true) : MultiConverter<Boolean>(required) {
    override val signatureTypeString = "yes/no"

    override val errorTypeString = "multiple `yes` or `no` values"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val booleans = mutableListOf<Boolean>()

        for (arg in args) {
            booleans.add(
                arg.parseBoolean() ?: break
            )
        }

        this.parsed = booleans

        return parsed.size
    }
}

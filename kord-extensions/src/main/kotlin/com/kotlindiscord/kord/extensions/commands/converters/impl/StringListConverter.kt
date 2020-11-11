package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter

class StringListConverter(required: Boolean = true) : MultiConverter<String>(required) {
    override val typeString = "text"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        this.parsed = args

        return args.size
    }

    override fun handleError(t: Throwable?): String = ""
}

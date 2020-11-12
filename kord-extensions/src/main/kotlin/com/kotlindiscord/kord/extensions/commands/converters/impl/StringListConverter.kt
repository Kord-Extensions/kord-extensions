package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter

class StringListConverter(required: Boolean = true) : MultiConverter<String>(required) {
    override val typeString = "text"
    override val showTypeInSignature = false

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        this.parsed = args

        return args.size
    }

    override suspend fun handleError(
        t: Throwable,
        value: List<String>,
        context: CommandContext,
        bot: ExtensibleBot
    ): String = ""
}

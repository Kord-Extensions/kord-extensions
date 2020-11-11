package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.MultiConverter

class StringListConverter(required: Boolean = true) : MultiConverter<String>(required) {
    override val typeString = "text"

    override suspend fun parse(args: List<String>, context: CommandContext): Int {
        this.parsed = args

        return args.size
    }

    override fun handleError(t: Throwable?): String = ""
}

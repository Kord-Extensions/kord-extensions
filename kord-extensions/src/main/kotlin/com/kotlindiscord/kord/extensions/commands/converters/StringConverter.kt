package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.SingleConverter

class StringConverter(required: Boolean = true) : SingleConverter<String>(required) {
    override val typeString = "text"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        this.parsed = arg

        return true
    }

    override fun handleError(t: Throwable?): String = ""
}

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter

class RegexConverter(
    required: Boolean = true,
    private val options: Set<RegexOption> = setOf()
) : SingleConverter<Regex>(required) {
    override val signatureTypeString = "regex"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        this.parsed = arg.toRegex(options)

        return true
    }
}

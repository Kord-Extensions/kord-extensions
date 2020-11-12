package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter

class RegexListConverter(
    required: Boolean = true,
    private val options: Set<RegexOption> = setOf()
) : MultiConverter<Regex>(required) {
    override val signatureTypeString = "regexes"
    override val showTypeInSignature = false

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        this.parsed = args.map { it.toRegex(options) }

        return args.size
    }
}

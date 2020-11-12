package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter

class RegexCoalescingConverter(
    required: Boolean = true,
    private val options: Set<RegexOption> = setOf()
) : CoalescingConverter<Regex>(required) {
    override val signatureTypeString = "regexes"
    override val showTypeInSignature = false

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        this.parsed = args.joinToString(" ").toRegex(options)

        return args.size
    }
}

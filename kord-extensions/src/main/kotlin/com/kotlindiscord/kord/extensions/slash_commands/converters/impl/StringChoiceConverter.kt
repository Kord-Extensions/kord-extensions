package com.kotlindiscord.kord.extensions.slash_commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.slash_commands.converters.ChoiceConverter

public class StringChoiceConverter(vararg choices: String) : ChoiceConverter<String>(*choices) {
    override val signatureTypeString: String = "text"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        this.parsed = arg

        return true
    }
}

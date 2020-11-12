package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.utils.parseBoolean

class BooleanConverter(required: Boolean = true) : SingleConverter<Boolean>(required) {
    override val signatureTypeString = "yes/no"

    override val errorTypeString = "`yes` or `no`"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val bool = arg.parseBoolean() ?: return false

        this.parsed = bool

        return true
    }
}

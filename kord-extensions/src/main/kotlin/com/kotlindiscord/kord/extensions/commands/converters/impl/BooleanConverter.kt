package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.utils.parseBoolean
import com.kotlindiscord.kord.extensions.commands.converters.boolean
import com.kotlindiscord.kord.extensions.commands.converters.booleanList

/**
 * Argument converter for [Boolean] arguments.
 *
 * Truthiness is determined by the [parseBoolean] function.
 *
 * @see boolean
 * @see booleanList
 */
class BooleanConverter : SingleConverter<Boolean>() {
    override val signatureTypeString = "yes/no"
    override val errorTypeString = "`yes` or `no`"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val bool = arg.parseBoolean() ?: return false

        this.parsed = bool

        return true
    }
}

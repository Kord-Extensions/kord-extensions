package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.converters.stringList

/**
 * Coalescing argument that simply returns the argument as it was given.
 *
 * The multi version of this converter (via [toMulti]) will consume all remaining arguments.
 *
 * @see string
 * @see stringList
 */
class StringConverter(required: Boolean = true) : SingleConverter<String>(required) {
    override val signatureTypeString = "text"
    override val showTypeInSignature = false

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        this.parsed = arg

        return true
    }
}

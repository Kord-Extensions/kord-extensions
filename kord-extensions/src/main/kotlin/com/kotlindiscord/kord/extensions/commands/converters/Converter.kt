package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext

abstract class Converter<T : Any>(val required: Boolean = true) {
    var parseSuccess: Boolean = false
    open val showTypeInSignature = true

    abstract val typeString: String
}

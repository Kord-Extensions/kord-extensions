package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import kotlin.reflect.KProperty

abstract class SingleConverter<T: Any>(required: Boolean = true): Converter<T>(required) {
    /** Parsed value, which won't be set until after parsing has occurred. **/
    lateinit var parsed: T

    abstract suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean

    /** For delegation, retrieve the parsed value if it's been set, or null if it hasn't. **/
    open operator fun getValue(thisRef: Arguments, property: KProperty<*>): T? {
        if (::parsed.isInitialized) {
            return parsed
        }

        return null
    }

    /** Given a Throwable encountered during parsing, return a human-readable string to display on Discord. **/
    open suspend fun handleError(
        t: Throwable,
        value: String,
        context: CommandContext,
        bot: ExtensibleBot
    ): String {
        throw t
    }
}

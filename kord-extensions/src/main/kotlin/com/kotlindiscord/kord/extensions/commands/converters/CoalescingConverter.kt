package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import kotlin.reflect.KProperty

abstract class CoalescingConverter<T : Any>(required: Boolean = true) : Converter<List<T>>(required) {
    lateinit var parsed: T

    abstract suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int

    /** For delegation, retrieve the parsed value if it's been set, or null if it hasn't. **/
    operator fun getValue(thisRef: Arguments, property: KProperty<*>): T? {
        if (::parsed.isInitialized) {
            return parsed
        }

        return null
    }

    /** Given a Throwable encountered during parsing, return a human-readable string to display on Discord. **/
    open suspend fun handleError(
        t: Throwable,
        values: List<String>,
        context: CommandContext,
        bot: ExtensibleBot
    ): String = throw t
}

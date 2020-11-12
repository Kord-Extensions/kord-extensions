package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import kotlin.reflect.KProperty

abstract class MultiConverter<T: Any>(required: Boolean = true): Converter<List<T>>(required) {
    var parsed: List<T> = listOf()

    abstract suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int

    operator fun getValue(thisRef: Arguments, property: KProperty<*>): List<T> {
        return parsed
    }

    /** Given a Throwable encountered during parsing, return a human-readable string to display on Discord. **/
    open suspend fun handleError(
        t: Throwable,
        values: List<String>,
        context: CommandContext,
        bot: ExtensibleBot
    ): String {
        throw t
    }
}

package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import kotlin.reflect.KProperty

abstract class MultiConverter<T: Any>(required: Boolean = true): Converter<List<T>>(required) {
    var parsed: List<T> = listOf()

    abstract suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int

    operator fun getValue(thisRef: Arguments, property: KProperty<*>): List<T> {
        return parsed
    }
}

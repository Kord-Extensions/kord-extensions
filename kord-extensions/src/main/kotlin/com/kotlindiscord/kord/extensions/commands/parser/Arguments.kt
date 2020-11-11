package com.kotlindiscord.kord.extensions.commands.parser

import kotlin.reflect.KProperty

open class Arguments {
    val args: MutableList<Argument<*>> = mutableListOf()

    fun <T: Any> arg(displayName: String, converter: SingleConverter<T>): SingleConverter<T> {
        args.add(Argument(displayName, converter))

        return converter
    }

    fun <T: Any> arg(displayName: String, converter: MultiConverter<T>): MultiConverter<T> {
        args.add(Argument(displayName, converter))

        return converter
    }
}

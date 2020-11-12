package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter

open class Arguments {
    val args: MutableList<Argument<*>> = mutableListOf()

    fun <T : Any> arg(displayName: String, converter: SingleConverter<T>): SingleConverter<T> {
        args.add(Argument(displayName, converter))

        return converter
    }

    fun <T : Any> arg(displayName: String, converter: MultiConverter<T>): MultiConverter<T> {
        args.add(Argument(displayName, converter))

        return converter
    }

    fun <T : Any> arg(displayName: String, converter: CoalescingConverter<T>): CoalescingConverter<T> {
        args.add(Argument(displayName, converter))

        return converter
    }
}

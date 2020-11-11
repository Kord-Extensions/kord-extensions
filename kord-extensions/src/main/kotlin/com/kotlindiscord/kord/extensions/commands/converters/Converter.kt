package com.kotlindiscord.kord.extensions.commands.converters

abstract class Converter<T : Any>(val required: Boolean = true) {
    var parseSuccess: Boolean = false

    abstract val typeString: String

    /** Given a Throwable encountered during parsing, return a human-readable string to display on Discord. **/
    abstract fun handleError(t: Throwable?): String
}

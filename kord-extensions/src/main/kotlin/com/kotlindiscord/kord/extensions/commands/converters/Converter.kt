package com.kotlindiscord.kord.extensions.commands.converters

abstract class Converter<T : Any>(val required: Boolean = true) {
    var parseSuccess: Boolean = false
    open val showTypeInSignature = true

    abstract val typeString: String
}

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.utils.startsWithVowel

abstract class Converter<T : Any>(val required: Boolean = true) {
    var parseSuccess: Boolean = false

    open val showTypeInSignature = true
    abstract val signatureTypeString: String

    open val errorTypeString: String? = null

    open fun getErrorString(): String = when (this) {
        is MultiConverter<*> -> errorTypeString ?: signatureTypeString
        is CoalescingConverter<*> -> errorTypeString ?: signatureTypeString

        else -> errorTypeString ?: if (signatureTypeString.startsWithVowel()) {
            "an "
        } else {
            "a "
        } + signatureTypeString
    }
}

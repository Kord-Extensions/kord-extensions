package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.utils.startsWithVowel

/**
 * Base class for an argument converter.
 *
 * Argument converters are in charge of taking either one or several string arguments, and converting them into some
 * other type. They're a convenience for people working with the commands framework.
 *
 * You probably don't want to subclass this directly. There are three direct subclasses you can look at if you'd like
 * to implement your own converters.
 *
 * @param required Whether this converter must succeed for a command invocation to be valid.
 */
abstract class Converter<T : Any>(val required: Boolean = true) {
    /** This will be set to true by the argument parser if the conversion succeeded. **/
    var parseSuccess: Boolean = false

    /** For commands with generated signatures, set whether the type string should be shown in the signature. **/
    open val showTypeInSignature = true

    /** A short string describing the type of data this converter handles. Should be very short. **/
    abstract val signatureTypeString: String

    /**
     * If the [signatureTypeString] isn't sufficient, you can optionally provide a longer type string to use for error
     * messages.
     */
    open val errorTypeString: String? = null

    /**
     * Return a formatted error string.
     *
     * This will attempt to use the [errorTypeString], falling back to [signatureTypeString]. If this is a
     * [SingleConverter], it will add "an" or "a" to it, depending on whether the given type string starts with a
     * vowel.
     */
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

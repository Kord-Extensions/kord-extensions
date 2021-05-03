package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument

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
public abstract class Converter<T : Any?>(
    public open val required: Boolean = true,
) {
    /** This will be set to true by the argument parser if the conversion succeeded. **/
    public var parseSuccess: Boolean = false

    /** For commands with generated signatures, set whether the type string should be shown in the signature. **/
    public open val showTypeInSignature: Boolean = true

    /**
     * Translation key pointing to a short string describing the type of data this converter handles. Should be very
     * short.
     */
    public abstract val signatureTypeString: String

    /**
     * String referring to the translation bundle name required to resolve translations for this converter.
     *
     * For more information, see the i18n page of the documentation.
     */
    public open val bundle: String? = null

    /**
     * If the [signatureTypeString] isn't sufficient, you can optionally provide a translation key pointing to a
     * longer type string to use for error messages.
     */
    public open val errorTypeString: String? = null

    /** Argument object containing this converter and its metadata. **/
    public open lateinit var argumentObj: Argument<*>

    /**
     * Return a translated, formatted error string.
     *
     * This will attempt to use the [errorTypeString], falling back to [signatureTypeString]. If this is a
     * [SingleConverter], it will add "an" or "a" to it, depending on whether the given type string starts with a
     * vowel.
     */
    public open suspend fun getErrorString(context: CommandContext): String = when (this) {
        is MultiConverter<*> -> context.translate(errorTypeString ?: signatureTypeString)
        is CoalescingConverter<*> -> context.translate(errorTypeString ?: signatureTypeString)

        else -> if (errorTypeString != null) {
            context.translate(errorTypeString!!)
        } else {
            context.translate(signatureTypeString)
        }
    }
}

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import kotlin.reflect.KProperty

/**
 * Abstract base class for a single converter.
 *
 * Single converters take a single string argument, transforming it into a single resulting value. Single converters
 * are always required.
 *
 * You can convert a [SingleConverter] instance to a defaulting, optional or multi converter
 * using [toDefaulting]. [toMulti] or [toOptional] respectively.
 *
 * You can create a single converter of your own by extending this class.
 */
abstract class SingleConverter<T : Any> : Converter<T>(true) {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    lateinit var parsed: T

    /**
     * Process the given [arg], converting it into a new value.
     *
     * The resulting value should be stored in [parsed] - this will not be done for you.
     *
     * If you'd like to return more detailed feedback to the user on invalid input, you can throw a [ParseException]
     * here.
     *
     * @param arg [String] argument, provided by the user running the current command
     * @param context Command context object, containing the event, message, and other command-related things
     * @param bot Current instance of [ExtensibleBot], representing the currently-connected bot
     *
     * @return Whether you managed to convert the argument. If you don't want to provide extra context to the user,
     * simply return `false` - the commands system will generate an error message for you.
     *
     * @see Converter
     */
    abstract suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean

    /** For delegation, retrieve the parsed value if it's been set, or throw if it hasn't. **/
    open operator fun getValue(thisRef: Arguments, property: KProperty<*>): T = parsed

    /**
     * Given a Throwable encountered during the [parse] function, return a human-readable string to display on Discord.
     *
     * This will always be called if an unhandled exception is thrown, unless it's a [ParseException] - those will be
     * displayed as an error message on Discord. If appropriate for your converter, you can use this function to
     * transform a thrown exception into a nicer, human-readable format.
     *
     * Please note: [value] will be set to `null` if this function is called when this converter has been wrapped
     * by a [SingleToMultiConverter] via the [toMulti] function.
     */
    open suspend fun handleError(
        t: Throwable,
        value: String?,
        context: CommandContext,
        bot: ExtensibleBot
    ): String {
        throw t
    }

    /**
     * Wrap this single converter with a [SingleToMultiConverter], which is a special converter that will act like a
     * [MultiConverter] using the same logic of this converter.
     *
     * Your converter should be designed with this pattern in mind. If that's not possible, please override this
     * function and throw an exception in the body.
     *
     * For more information on the parameters, see [Converter].
     *
     * @param required Whether command parsing should fail if no arguments could be converted.
     *
     * @param signatureTypeString Optionally, a signature type string to use instead of the one this converter
     * provides.
     *
     * @param showTypeInSignature Optionally, override this converter's setting for showing the type string in a
     * generated command signature.
     *
     * @param errorTypeString Optionally, a longer type string to be shown in errors instead of the one this converter
     * provides.
     */
    open fun toMulti(
        required: Boolean = true,
        signatureTypeString: String? = null,
        showTypeInSignature: Boolean? = null,
        errorTypeString: String? = null
    ): MultiConverter<T> = SingleToMultiConverter(
        required,
        this,
        signatureTypeString,
        showTypeInSignature,
        errorTypeString
    )

    /**
     * Wrap this single converter with a [SingleToOptionalConverter], which is a special converter that will act like
     * an [OptionalConverter] using the same logic of this converter.
     *
     * Your converter should be designed with this pattern in mind. If that's not possible, please override this
     * function and throw an exception in the body.
     *
     * For more information on the parameters, see [Converter].
     *
     * @param signatureTypeString Optionally, a signature type string to use instead of the one this converter
     * provides.
     *
     * @param showTypeInSignature Optionally, override this converter's setting for showing the type string in a
     * generated command signature.
     *
     * @param errorTypeString Optionally, a longer type string to be shown in errors instead of the one this converter
     * provides.
     */
    open fun toOptional(
        signatureTypeString: String? = null,
        showTypeInSignature: Boolean? = null,
        errorTypeString: String? = null
    ): OptionalConverter<T?> = SingleToOptionalConverter(
        this,
        signatureTypeString,
        showTypeInSignature,
        errorTypeString
    )

    /**
     * Wrap this single converter with a [SingleToDefaultingConverter], which is a special converter that will act like
     * a [DefaultingConverter] using the same logic of this converter.
     *
     * Your converter should be designed with this pattern in mind. If that's not possible, please override this
     * function and throw an exception in the body.
     *
     * For more information on the parameters, see [Converter].
     *
     * @param defaultValue The default value to use when an argument can't be converted.
     *
     * @param signatureTypeString Optionally, a signature type string to use instead of the one this converter
     * provides.
     *
     * @param showTypeInSignature Optionally, override this converter's setting for showing the type string in a
     * generated command signature.
     *
     * @param errorTypeString Optionally, a longer type string to be shown in errors instead of the one this converter
     * provides.
     */
    open fun toDefaulting(
        defaultValue: T,
        signatureTypeString: String? = null,
        showTypeInSignature: Boolean? = null,
        errorTypeString: String? = null
    ): DefaultingConverter<T> = SingleToDefaultingConverter(
        this,
        defaultValue,
        signatureTypeString,
        showTypeInSignature,
        errorTypeString
    )
}

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import kotlin.reflect.KProperty

/**
 * Abstract base class for an optional single converter.
 *
 * This works just like [SingleConverter], but the value can be nullable and it can never be required.
 */
abstract class OptionalConverter<T : Any?> : Converter<T>(false) {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    var parsed: T? = null

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
    open operator fun getValue(thisRef: Arguments, property: KProperty<*>): T? = parsed

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
}

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import kotlin.reflect.KProperty

/**
 * Abstract base class for a defaulting converter.
 *
 * Single converters take a single string argument, transforming it into a single resulting value. A default value
 * will be provided in case parsing fails.
 *
 * You can create a defaulting converter of your own by extending this class.
 *
 * @property validator Validation lambda, which may throw a [CommandException] if required.
 */
@KordPreview
public abstract class DefaultingConverter<T : Any>(
    defaultValue: T,
    public open var validator: (suspend Argument<*>.(result: T) -> Unit)? = null
) : Converter<T>(false), SlashCommandConverter {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    public var parsed: T = defaultValue

    /**
     * Process the given [arg], converting it into a new value.
     *
     * The resulting value should be stored in [parsed] - this will not be done for you.
     *
     * If you'd like to return more detailed feedback to the user on invalid input, you can throw a [CommandException]
     * here.
     *
     * @param arg [String] argument, provided by the user running the current command
     * @param context MessageCommand context object, containing the event, message, and other command-related things
     *
     * @return Whether you managed to convert the argument. If you don't want to provide extra context to the user,
     * simply return `false`.
     *
     * @see Converter
     */
    public abstract suspend fun parse(arg: String, context: CommandContext): Boolean

    /** For delegation, retrieve the parsed value if it's been set, or throw if it hasn't. **/
    public open operator fun getValue(thisRef: Arguments, property: KProperty<*>): T = parsed

    /** Call the validator lambda, if one was provided. **/
    public open suspend fun validate() {
        validator?.let { it(this.argumentObj, parsed) }
    }

    /**
     * Given a Throwable encountered during the [parse] function, return a human-readable string to display on Discord.
     *
     * This will always be called if an unhandled exception is thrown. If appropriate for your converter, you can use
     * this function to transform a thrown exception into a nicer, human-readable format.
     *
     * The default behaviour simply re-throws the Throwable (or returns the reason if it's a CommandException), so you
     * only need to override this if you want to do something else.
     */
    public open suspend fun handleError(
        t: Throwable,
        value: String?,
        context: CommandContext
    ): String = if (t is CommandException) t.reason else throw t
}

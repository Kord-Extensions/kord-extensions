package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.parser.StringParser
import kotlin.reflect.KProperty

/**
 * Abstract base class for a defaulting coalescing converter.
 *
 * Coalescing converters take a list of multiple arguments, and consumes as many arguments as it can, combining
 * those arguments into a single value. Upon reaching an argument that can't be consumed, the converter stores
 * its final result and tells the parser how many arguments it managed to consume. The parser will continue
 * processing the unused arguments, passing them to the remaining converters.
 *
 * A defaulting coalescing converter has a default value that will be provided if nothing could be parsed.
 *
 * You can create an optional coalescing converter of your own by extending this class.
 *
 * @property validator Validation lambda, which may throw a [CommandException] if required.
 */
public abstract class DefaultingCoalescingConverter<T : Any>(
    defaultValue: T,
    public open var validator: Validator<T> = null
) : Converter<List<T>>(
    false
) {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    public var parsed: T = defaultValue

    /**
     * Process the given [args], converting them into a single value.
     *
     * The resulting value should be stored in [parsed] - this will not be done for you.
     *
     * This type of converter should avoid throwing [CommandException]. The commands framework will return an
     * appropriate error to the user, if this converter is marked as required. A thrown [CommandException] will still
     * be shown in that situation, however.
     *
     * @param args List of [String] arguments, provided by the user running the current command
     * @param context MessageCommand context object, containing the event, message, and other command-related things
     *
     * @return The number of arguments this converter consumed to produce its resulting value. Return 0 if you didn't
     * consume any (eg, you failed to convert anything).
     *
     * @see Converter
     */
    public abstract suspend fun parse(
        parser: StringParser?,
        context: CommandContext,
        namedArguments: List<String>? = null
    ): Int

    /** For delegation, retrieve the parsed value if it's been set, or throw if it hasn't. **/
    public open operator fun getValue(thisRef: Arguments, property: KProperty<*>): T = parsed

    /** Call the validator lambda, if one was provided. **/
    public open suspend fun validate(context: CommandContext) {
        validator?.let { it(context, this.argumentObj, parsed) }
    }

    /**
     * Given a Throwable encountered during the [parse] function, return a human-readable string to display on Discord.
     *
     * For coalescing converters, this is only called when the converter is required. The default behaviour simply
     * re-throws the Throwable (or returns the reason if it's a CommandException), so you only need to override this
     * if you want to do something else.
     */
    public open suspend fun handleError(
        t: Throwable,
        context: CommandContext
    ): String = if (t is CommandException) t.reason else throw t
}

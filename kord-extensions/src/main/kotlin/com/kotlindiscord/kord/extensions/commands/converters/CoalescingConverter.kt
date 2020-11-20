package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.ParseException
import kotlin.reflect.KProperty

/**
 * Abstract base class for a coalescing converter.
 *
 * Coalescing converters take a list of multiple arguments, and consumes as many arguments as it can, combining
 * those arguments into a single value. Upon reaching an argument that can't be consumed, the converter stores
 * its final result and tells the parser how many arguments it managed to consume. The parser will continue
 * processing the unused arguments, passing them to the remaining converters.
 *
 * You can convert a [CoalescingConverter] instance to a defaulting or multi converter using [toDefaulting]
 * or [toOptional] respectively.
 *
 * You can create a coalescing converter of your own by extending this class.
 */
public abstract class CoalescingConverter<T : Any> : Converter<List<T>>(true) {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    public lateinit var parsed: T

    /**
     * Process the given [args], converting them into a single value.
     *
     * The resulting value should be stored in [parsed] - this will not be done for you.
     *
     * This type of converter should avoid throwing [ParseException]. The commands framework will return an appropriate
     * error to the user, if this converter is marked as required. A thrown [ParseException] will still be shown in
     * that situation, however.
     *
     * @param args List of [String] arguments, provided by the user running the current command
     * @param context Command context object, containing the event, message, and other command-related things
     * @param bot Current instance of [ExtensibleBot], representing the currently-connected bot
     *
     * @return The number of arguments this converter consumed to produce its resulting value. Return 0 if you didn't
     * consume any (eg, you failed to convert anything).
     *
     * @see Converter
     */
    public abstract suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int

    /** For delegation, retrieve the parsed value if it's been set, or throw if it hasn't. **/
    public open operator fun getValue(thisRef: Arguments, property: KProperty<*>): T = parsed

    /**
     * Given a Throwable encountered during the [parse] function, return a human-readable string to display on Discord.
     *
     * For coalescing converters, this is only called when the converter is required. The default behaviour simply
     * re-throws the Throwable, so you only need to override this if you want to do something else.
     */
    public open suspend fun handleError(
        t: Throwable,
        values: List<String>,
        context: CommandContext,
        bot: ExtensibleBot
    ): String = throw t

    /**
     * Wrap this coalescing converter with a [CoalescingToOptionalConverter], which is a special converter that will
     * act like an [OptionalCoalescingConverter] using the same logic of this converter.
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
    public open fun toOptional(
        signatureTypeString: String? = null,
        showTypeInSignature: Boolean? = null,
        errorTypeString: String? = null
    ): OptionalCoalescingConverter<T?> = CoalescingToOptionalConverter(
        this,
        signatureTypeString,
        showTypeInSignature,
        errorTypeString
    )

    /**
     * Wrap this coalescing converter with a [CoalescingToDefaultingConverter], which is a special converter that will
     * act like an [DefaultingCoalescingConverter] using the same logic of this converter.
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
    public open fun toDefaulting(
        defaultValue: T,
        signatureTypeString: String? = null,
        showTypeInSignature: Boolean? = null,
        errorTypeString: String? = null
    ): DefaultingCoalescingConverter<T> = CoalescingToDefaultingConverter(
        this,
        defaultValue,
        signatureTypeString,
        showTypeInSignature,
        errorTypeString
    )
}

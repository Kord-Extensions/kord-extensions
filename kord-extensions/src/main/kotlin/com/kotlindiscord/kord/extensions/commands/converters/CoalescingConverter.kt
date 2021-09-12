@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import dev.kord.common.annotation.KordPreview

/**
 * Abstract base class for a coalescing converter.
 *
 * Coalescing converters take a list of multiple arguments, and consumes as many arguments as it can, combining
 * those arguments into a single value. Upon reaching an argument that can't be consumed, the converter stores
 * its final result and tells the parser how many arguments it managed to consume. The parser will continue
 * processing the unused arguments, passing them to the remaining converters.
 *
 * You can convert a [CoalescingConverter] instance to a defaulting or optional converter using [toDefaulting]
 * or [toOptional] respectively.
 *
 * You can create a coalescing converter of your own by extending this class.
 *
 * @property shouldThrow Intended only for use if this converter is the last one in a set of arguments, if this is
 * `true` then the converter should throw a [DiscordRelayedException] when an argument can't be parsed, instead of just
 * stopping and allowing parsing to continue.
 *
 * @property validator Validation lambda, which may throw a [DiscordRelayedException] if required.
 */
public abstract class CoalescingConverter<T : Any>(
    public open val shouldThrow: Boolean = false,
    override var validator: Validator<T> = null
) : Converter<List<T>, T, List<String>, Int>(true), SlashCommandConverter {
    /**
     * The parsed value.
     *
     * This should be set by the converter during the course of the [parse] function.
     */
    public override lateinit var parsed: T

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
     *
     * @param outputError Optionally, provide `true` to fail parsing and return errors if the converter throws a
     * [DiscordRelayedException], instead of continuing. You probably only want to set this if the converter is the
     * last one in a set of arguments.
     */
    @ConverterToOptional
    public open fun toOptional(
        signatureTypeString: String? = null,
        showTypeInSignature: Boolean? = null,
        errorTypeString: String? = null,
        outputError: Boolean = false,
        nestedValidator: Validator<T?> = null
    ): OptionalCoalescingConverter<T?> = CoalescingToOptionalConverter(
        this,
        signatureTypeString,
        showTypeInSignature,
        errorTypeString,
        outputError,
        nestedValidator
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
     * @param outputError Whether the argument parser should output parsing errors on invalid arguments.
     * @param signatureTypeString Optionally, a signature type string to use instead of the one this converter
     * provides.
     *
     * @param showTypeInSignature Optionally, override this converter's setting for showing the type string in a
     * generated command signature.
     *
     * @param errorTypeString Optionally, a longer type string to be shown in errors instead of the one this converter
     * provides.
     */
    @ConverterToDefaulting
    public open fun toDefaulting(
        defaultValue: T,
        outputError: Boolean = false,
        signatureTypeString: String? = null,
        showTypeInSignature: Boolean? = null,
        errorTypeString: String? = null,
        nestedValidator: Validator<T> = null
    ): DefaultingCoalescingConverter<T> = CoalescingToDefaultingConverter(
        this,
        defaultValue = defaultValue,
        outputError = outputError,
        newSignatureTypeString = signatureTypeString,
        newShowTypeInSignature = showTypeInSignature,
        newErrorTypeString = errorTypeString,
        validator = nestedValidator,
    )
}

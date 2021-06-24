package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.parser.StringParser

/**
 * A special [OptionalConverter] that wraps a [SingleConverter], effectively turning it into an optional
 * converter with the same logic.
 *
 * The behaviours specified in [OptionalConverter] also apply to this converter, so it's worth reading about it.
 *
 * @param coalescingConverter The [SingleConverter] to wrap.
 *
 * @param newSignatureTypeString An optional signature type string to override the one set in [coalescingConverter].
 * @param newShowTypeInSignature An optional boolean to override the [showTypeInSignature] setting set in
 * [coalescingConverter].
 * @param newErrorTypeString An optional error type string to override the one set in [coalescingConverter].
 */
public class CoalescingToDefaultingConverter<T : Any>(
    public val coalescingConverter: CoalescingConverter<T>,
    defaultValue: T,

    newSignatureTypeString: String? = null,
    newShowTypeInSignature: Boolean? = null,
    newErrorTypeString: String? = null,

    override var validator: Validator<T> = null
) : DefaultingCoalescingConverter<T>(defaultValue) {
    override val signatureTypeString: String = newSignatureTypeString ?: coalescingConverter.signatureTypeString
    override val showTypeInSignature: Boolean = newShowTypeInSignature ?: coalescingConverter.showTypeInSignature
    override val errorTypeString: String? = newErrorTypeString ?: coalescingConverter.errorTypeString

    override suspend fun parse(parser: StringParser?, context: CommandContext, namedArgument: List<String>?): Int {
        val result = coalescingConverter.parse(parser, context, namedArgument)

        if (result > 0) {
            this.parsed = coalescingConverter.parsed
        }

        return result
    }

    override suspend fun handleError(
        t: Throwable,
        context: CommandContext
    ): String = coalescingConverter.handleError(t, context)
}

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext

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
public class CoalescingToOptionalConverter<T : Any>(
    public val coalescingConverter: CoalescingConverter<T>,

    newSignatureTypeString: String? = null,
    newShowTypeInSignature: Boolean? = null,
    newErrorTypeString: String? = null,
    outputError: Boolean = false,

    override var validator: (suspend (T?) -> Unit)? = null
) : OptionalCoalescingConverter<T?>(outputError) {
    override val signatureTypeString: String = newSignatureTypeString ?: coalescingConverter.signatureTypeString
    override val showTypeInSignature: Boolean = newShowTypeInSignature ?: coalescingConverter.showTypeInSignature
    override val errorTypeString: String? = newErrorTypeString ?: coalescingConverter.errorTypeString

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val result = coalescingConverter.parse(args, context, bot)

        if (result > 0) {
            this.parsed = coalescingConverter.parsed
        }

        return result
    }

    override suspend fun handleError(
        t: Throwable,
        values: List<String>,
        context: CommandContext,
        bot: ExtensibleBot
    ): String = coalescingConverter.handleError(t, values, context, bot)
}

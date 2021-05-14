package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments

/**
 * A special [MultiConverter] that wraps a [SingleConverter], effectively turning it into a list-handling converter
 * with the same logic.
 *
 * The behaviours specified in [MultiConverter] also apply to this converter, so it's worth reading about it.
 *
 * @param singleConverter The [SingleConverter] to wrap.
 *
 * @param newSignatureTypeString An optional signature type string to override the one set in [singleConverter].
 * @param newShowTypeInSignature An optional boolean to override the [showTypeInSignature] setting set in
 * [singleConverter].
 * @param newErrorTypeString An optional error type string to override the one set in [singleConverter].
 */
public class SingleToMultiConverter<T : Any>(
    required: Boolean = true,
    public val singleConverter: SingleConverter<T>,

    newSignatureTypeString: String? = null,
    newShowTypeInSignature: Boolean? = null,
    newErrorTypeString: String? = null,

    override var validator: (suspend Argument<*>.(List<T>) -> Unit)? = null
) : MultiConverter<T>(required) {
    override val signatureTypeString: String = newSignatureTypeString ?: singleConverter.signatureTypeString
    override val showTypeInSignature: Boolean = newShowTypeInSignature ?: singleConverter.showTypeInSignature
    override val errorTypeString: String? = newErrorTypeString ?: singleConverter.errorTypeString

    override suspend fun parse(args: List<String>, context: CommandContext): Int {
        val values = mutableListOf<T>()
        val dummyArgs = Arguments()

        for (arg in args) {
            try {
                val result = singleConverter.parse(arg, context)

                if (!result) {
                    break
                }

                val value = singleConverter.getValue(dummyArgs, singleConverter::parsed)

                values.add(value)
            } catch (e: CommandException) {
                break
            }
        }

        parsed = values

        return parsed.size
    }

    override suspend fun handleError(
        t: Throwable,
        values: List<String>,
        context: CommandContext
    ): String = singleConverter.handleError(t, null, context)
}

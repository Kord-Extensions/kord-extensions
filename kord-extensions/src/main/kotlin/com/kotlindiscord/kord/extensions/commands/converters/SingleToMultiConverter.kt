package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.parser.StringParser

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

    override var validator: Validator<List<T>> = null
) : MultiConverter<T>(required) {
    override val signatureTypeString: String = newSignatureTypeString ?: singleConverter.signatureTypeString
    override val showTypeInSignature: Boolean = newShowTypeInSignature ?: singleConverter.showTypeInSignature
    override val errorTypeString: String? = newErrorTypeString ?: singleConverter.errorTypeString

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: List<String>?): Int {
        val values = mutableListOf<T>()
        val dummyArgs = Arguments()
        val tokens = named?.toMutableList() ?: mutableListOf()

        if (tokens.isEmpty()) {
            while (parser!!.hasNext) {
                tokens.add(parser.parseNext()!!.data)
            }
        }

        for (arg in tokens) {
            try {
                val result = singleConverter.parse(null, context, arg)

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
        context: CommandContext
    ): String = singleConverter.handleError(t, context)
}

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder

/**
 * A special [OptionalConverter] that wraps a [SingleConverter], effectively turning it into an optional
 * converter with the same logic.
 *
 * The behaviours specified in [OptionalConverter] also apply to this converter, so it's worth reading about it.
 *
 * @param singleConverter The [SingleConverter] to wrap.
 *
 * @param newSignatureTypeString An optional signature type string to override the one set in [singleConverter].
 * @param newShowTypeInSignature An optional boolean to override the [showTypeInSignature] setting set in
 * [singleConverter].
 * @param newErrorTypeString An optional error type string to override the one set in [singleConverter].
 */
@OptIn(KordPreview::class)
public class SingleToOptionalConverter<T : Any>(
    public val singleConverter: SingleConverter<T>,

    newSignatureTypeString: String? = null,
    newShowTypeInSignature: Boolean? = null,
    newErrorTypeString: String? = null,
    outputError: Boolean = false,

    override var validator: Validator<T?> = null
) : OptionalConverter<T?>(outputError) {
    override val signatureTypeString: String = newSignatureTypeString ?: singleConverter.signatureTypeString
    override val showTypeInSignature: Boolean = newShowTypeInSignature ?: singleConverter.showTypeInSignature
    override val errorTypeString: String? = newErrorTypeString ?: singleConverter.errorTypeString

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val result = singleConverter.parse(arg, context)

        if (result) {
            this.parsed = singleConverter.parsed
        }

        return result
    }

    override suspend fun handleError(
        t: Throwable,
        value: String?,
        context: CommandContext
    ): String = singleConverter.handleError(t, value, context)

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder {
        val option = singleConverter.toSlashOption(arg)
        option.required = false

        return option
    }
}

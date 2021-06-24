package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder

/**
 * A special [SingleConverter] that wraps another [SingleConverter], effectively turning it into an optional
 * converter with the same logic, but with a default value if parsing fails.
 *
 * @param singleConverter The [SingleConverter] to wrap.
 *
 * @param newSignatureTypeString An optional signature type string to override the one set in [singleConverter].
 * @param newShowTypeInSignature An optional boolean to override the [showTypeInSignature] setting set in
 * [singleConverter].
 * @param newErrorTypeString An optional error type string to override the one set in [singleConverter].
 */
@OptIn(KordPreview::class)
public class SingleToDefaultingConverter<T : Any>(
    public val singleConverter: SingleConverter<T>,
    defaultValue: T,

    newSignatureTypeString: String? = null,
    newShowTypeInSignature: Boolean? = null,
    newErrorTypeString: String? = null,

    override var validator: Validator<T> = null
) : DefaultingConverter<T>(defaultValue) {
    override val signatureTypeString: String = newSignatureTypeString ?: singleConverter.signatureTypeString
    override val showTypeInSignature: Boolean = newShowTypeInSignature ?: singleConverter.showTypeInSignature
    override val errorTypeString: String? = newErrorTypeString ?: singleConverter.errorTypeString

    override suspend fun parse(
        parser: StringParser?,
        context: CommandContext,
        named: String?
    ): Boolean {
        val result = singleConverter.parse(parser, context, named)

        if (result) {
            this.parsed = singleConverter.parsed

            return true
        }

        return false
    }

    override suspend fun handleError(
        t: Throwable,
        context: CommandContext
    ): String = singleConverter.handleError(t, context)

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder {
        val option = singleConverter.toSlashOption(arg)
        option.required = false

        return option
    }
}

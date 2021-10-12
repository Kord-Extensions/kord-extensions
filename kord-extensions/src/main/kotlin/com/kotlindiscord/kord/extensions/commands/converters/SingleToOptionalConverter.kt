package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
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

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val token = parser?.peekNext()
        val result = singleConverter.parse(parser, context, named ?: token?.data)

        if (result) {
            this.parsed = singleConverter.parsed

            if (named == null) {
                parser?.parseNext()  // Move the cursor ahead
            }
        }

        return result
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

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val result = singleConverter.parseOption(context, option)

        if (result) {
            this.parsed = singleConverter.parsed
        }

        return result
    }
}

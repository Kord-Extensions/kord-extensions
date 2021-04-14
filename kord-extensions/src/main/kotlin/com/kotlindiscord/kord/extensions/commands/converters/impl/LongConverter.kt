package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.long
import com.kotlindiscord.kord.extensions.commands.converters.longList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for long arguments, converting them into [Long].
 *
 * @see long
 * @see longList
 */
@OptIn(KordPreview::class)
public class LongConverter(
    private val radix: Int = DEFAULT_RADIX,
    override var validator: (suspend Argument<*>.(Long) -> Unit)? = null
) : SingleConverter<Long>() {
    override val signatureTypeString: String = "converters.number.signatureType"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = arg.toLong(radix)
        } catch (e: NumberFormatException) {
            val endChar = context.translate("converters.number.error.invalid.endCharacter")
            var errorString = context.translate("converters.number.error.invalid", replacements = arrayOf(arg))

            if (radix != DEFAULT_RADIX) {
                errorString += " " + context.translate(
                    "converters.number.error.invalid.withBase",
                    replacements = arrayOf(radix)
                )
            }

            errorString += endChar

            throw CommandException(errorString)
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        IntChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

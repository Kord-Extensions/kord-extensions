package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.int
import com.kotlindiscord.kord.extensions.commands.converters.intList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for integer arguments, converting them into [Int].
 *
 * @see int
 * @see intList
 */
@OptIn(KordPreview::class)
public class IntConverter(
    private val radix: Int = DEFAULT_RADIX,
    override var validator: (suspend Argument<*>.(Int) -> Unit)? = null
) : SingleConverter<Int>() {
    override val signatureTypeString: String = "converters.number.signatureType"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = arg.toInt(radix)
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

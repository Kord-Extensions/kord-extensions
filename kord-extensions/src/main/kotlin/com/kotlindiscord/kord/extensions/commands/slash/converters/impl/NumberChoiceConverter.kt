package com.kotlindiscord.kord.extensions.commands.slash.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.slash.converters.ChoiceConverter
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

/**
 * Choice converter for integer arguments. Supports mapping up to 25 choices to integers.
 *
 * Discord doesn't support longs or floating point types, so this is the only numeric type you can use directly.
 */
@OptIn(KordPreview::class)
public class NumberChoiceConverter(
    private val radix: Int = DEFAULT_RADIX,
    choices: Map<String, Int>,
    override var validator: (suspend Argument<*>.(Int) -> Unit)? = null
) : ChoiceConverter<Int>(choices) {
    override val signatureTypeString: String = "converters.number.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            this.parsed = arg.toInt(radix)
        } catch (e: NumberFormatException) {
            val errorString = if (radix == DEFAULT_RADIX) {
                context.translate("converters.number.error.invalid.defaultBase", replacements = arrayOf(arg))
            } else {
                context.translate("converters.number.error.invalid.otherBase", replacements = arrayOf(arg, radix))
            }

            throw CommandException(errorString)
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        IntChoiceBuilder(arg.displayName, arg.description).apply {
            required = true

            this@NumberChoiceConverter.choices.forEach { choice(it.key, it.value) }
        }
}

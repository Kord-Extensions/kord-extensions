package com.kotlindiscord.kord.extensions.slash_commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.slash_commands.converters.ChoiceConverter
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.IntChoiceBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

private const val DEFAULT_RADIX = 10

@OptIn(KordPreview::class)
public class NumberChoiceConverter(
    private val radix: Int = DEFAULT_RADIX, choices: Map<String, Int>
) : ChoiceConverter<Int>(choices) {
    override val signatureTypeString: String = "number"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = arg.toInt(radix)
        } catch (e: NumberFormatException) {
            throw ParseException(
                "Value '$arg' is not a valid whole number" + if (radix != DEFAULT_RADIX) " in base-$radix." else "."
            )
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        IntChoiceBuilder(arg.displayName, arg.description).apply {
            required = true
            this@NumberChoiceConverter.choices.forEach { choice(it.key) { it.value } }
        }
}


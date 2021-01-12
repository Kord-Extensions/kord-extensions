package com.kotlindiscord.kord.extensions.commands.slash.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.slash.converters.ChoiceConverter
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Choice converter for string arguments. Supports mapping up to 10 choices to string.
 *
 * If you want users to be able to input any string, don't provide any choices.
 */
@OptIn(KordPreview::class)
public class StringChoiceConverter(choices: Map<String, String>) : ChoiceConverter<String>(choices) {
    override val signatureTypeString: String = "text"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        this.parsed = arg

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply {
            required = true

            this@StringChoiceConverter.choices.forEach { choice(it.key, it.value) }
        }
}

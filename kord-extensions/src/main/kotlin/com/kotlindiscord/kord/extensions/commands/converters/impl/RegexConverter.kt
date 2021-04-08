package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.regex
import com.kotlindiscord.kord.extensions.commands.converters.regexList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for regular expression arguments, converting them into [Regex] objects.
 *
 * Please note that user-provided regular expressions are not safe - they can take down your entire bot.
 *
 * As there is no way to validate individual segments of regex, the multi version of this converter
 * (via [toMulti]) will consume all remaining arguments.
 *
 * @param options Optional set of [RegexOption]s to pass to the regex parser.
 *
 * @see regex
 * @see regexList
 */

@OptIn(KordPreview::class)
public class RegexConverter(
    private val options: Set<RegexOption> = setOf(),
    override var validator: (suspend (Regex) -> Unit)? = null
) : SingleConverter<Regex>() {
    override val signatureTypeString: String = "regex"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        this.parsed = arg.toRegex(options)

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

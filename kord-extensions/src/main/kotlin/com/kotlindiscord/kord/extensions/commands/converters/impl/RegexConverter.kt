package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.regex
import com.kotlindiscord.kord.extensions.commands.converters.regexList

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

class RegexConverter(
    required: Boolean = true,
    private val options: Set<RegexOption> = setOf()
) : SingleConverter<Regex>(required) {
    override val signatureTypeString = "regex"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        this.parsed = arg.toRegex(options)

        return true
    }
}

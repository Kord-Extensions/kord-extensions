package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.coalescedRegex

/**
 * Coalescing argument converter for regular expression arguments, combining the arguments into a single [Regex]
 * object by joining them with spaces.
 *
 * Please note that user-provided regular expressions are not safe - they can take down your entire bot.
 *
 * As there is no way to validate individual segments of regex, this converter will consume all remaining arguments.
 *
 * @param options Optional set of [RegexOption]s to pass to the regex parser.
 *
 * @see coalescedRegex
 */
class RegexCoalescingConverter(
    private val options: Set<RegexOption> = setOf()
) : CoalescingConverter<Regex>() {
    override val signatureTypeString = "regexes"
    override val showTypeInSignature = false

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        this.parsed = args.joinToString(" ").toRegex(options)

        return args.size
    }
}

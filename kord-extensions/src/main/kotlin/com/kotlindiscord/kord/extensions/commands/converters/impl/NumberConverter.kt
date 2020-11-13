package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.number
import com.kotlindiscord.kord.extensions.commands.converters.numberList

private const val DEFAULT_RADIX = 10

/**
 * Argument converter for whole number arguments, converting them into [Long].
 *
 * @see number
 * @see numberList
 */
class NumberConverter(
    private val radix: Int = DEFAULT_RADIX
) : SingleConverter<Long>() {
    override val signatureTypeString = "number"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = arg.toLong(radix)
        } catch (e: NumberFormatException) {
            throw ParseException(
                "Value '$arg' is not a valid whole number" + if (radix != DEFAULT_RADIX) " in base-$radix." else "."
            )
        }

        return true
    }
}

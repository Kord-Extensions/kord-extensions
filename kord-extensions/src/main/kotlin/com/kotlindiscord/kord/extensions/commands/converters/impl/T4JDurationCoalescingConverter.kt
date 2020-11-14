package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.coalescedT4jDuration
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import com.kotlindiscord.kord.extensions.parsers.parseDuration
import net.time4j.Duration
import net.time4j.IsoUnit

/**
 * Coalescing argument converter for Time4J [Duration] arguments.
 *
 * This converter will take individual duration specifiers ("1w", "2y", "3d" etc) until it no longer can, and then
 * combine them into a single [Duration].
 *
 * @see coalescedT4jDuration
 * @see parseDuration
 */
class T4JDurationCoalescingConverter : CoalescingConverter<Duration<IsoUnit>>() {
    override val signatureTypeString = "duration"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val durations = mutableListOf<String>()

        for (arg in args) {
            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                parseDuration(arg)
                durations.add(arg)
            } catch (e: InvalidTimeUnitException) {
                break
            }
        }

        parsed = parseDuration(
            durations.joinToString()
        )

        return durations.size
    }
}

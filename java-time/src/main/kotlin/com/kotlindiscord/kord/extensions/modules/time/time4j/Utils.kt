package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.modules.time.time4j.formatters.J8DurationFormatter
import java.time.Duration
import java.util.*

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
@Suppress("MagicNumber")  // These are all time units!
public fun Duration.toHuman(locale: Locale): String? = J8DurationFormatter.format(this, locale)

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
@Suppress("MagicNumber")  // These are all time units!
public suspend fun Duration.toHuman(context: CommandContext): String? = toHuman(context.getLocale())

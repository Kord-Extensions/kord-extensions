package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.utils.getKoin
import java.time.Duration
import java.util.*

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
@Suppress("MagicNumber")  // These are all time units!
public fun Duration.toHuman(locale: Locale): String? {
    val translationsProvider = getKoin().get<TranslationsProvider>()
    val parts = mutableListOf<String>()

    val seconds = this.seconds % 60
    val minutesTotal = this.seconds / 60

    val minutes = minutesTotal % 60
    val hoursTotal = minutesTotal / 60

    val hours = hoursTotal % 24
    val days = hoursTotal / 24

    if (days > 0) {
        parts.add(
            translationsProvider.translate("utils.time.days", locale, replacements = arrayOf(days))
        )
    }

    if (hours > 0) {
        parts.add(
            translationsProvider.translate("utils.time.hours", locale, replacements = arrayOf(hours))
        )
    }

    if (minutes > 0) {
        parts.add(
            translationsProvider.translate("utils.time.minutes", locale, replacements = arrayOf(minutes))
        )
    }

    if (seconds > 0) {
        parts.add(
            translationsProvider.translate("utils.time.seconds", locale, replacements = arrayOf(seconds))
        )
    }

    if (parts.isEmpty()) return null

    // I have no idea how I should _actually_ do this...
    val andJoiner = translationsProvider.translate("utils.time.andJoiner", locale).reversed() + " "
    return parts.joinToString(", ").reversed().replaceFirst(",", andJoiner).reversed()
}

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
@Suppress("MagicNumber")  // These are all time units!
public suspend fun Duration.toHuman(context: CommandContext): String? = toHuman(context.getLocale())

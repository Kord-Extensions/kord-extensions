package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import java.util.*

/**
 * Return a [Pair] containing the start of the given string up to the first [separator] character, followed by
 * the rest of the string. If the [separator] character wasn't found, the returned [Pair] will contain the entire
 * string as the first element, followed by the empty string.
 *
 * @param separator Separator character to split on.
 * @return Pair containing the split string.
 */
public fun String.splitOn(separator: (Char) -> Boolean): Pair<String, String> {
    val i = this.indexOfFirst(separator)

    if (i == -1) {
        return this to ""
    }

    return slice(IntRange(0, i - 1)) to slice(IntRange(i, this.length - 1))
}

/**
 * Check whether a string starts with a vowel, ignoring case.
 *
 * @return `true` if the string starts with an English vowel, `false` otherwise.
 */
public fun String.startsWithVowel(): Boolean = "aeiou".any { startsWith(it, true) }

/**
 * Check whether a string starts with a vowel, ignoring case, using the context's detected locale.
 *
 * This uses the `utils.string.vowels` translation key, which should contain all vowels for that translation with
 * no delimiters.
 *
 * @return `true` if the string starts with a vowel, `false` otherwise.
 */
public suspend fun String.startsWithVowel(context: CommandContext): Boolean =
    context.translate("utils.string.vowels")
        .any { startsWith(it, true) }

/**
 * Check whether a string starts with a vowel, ignoring case, using the given locale.
 *
 * This uses the `utils.string.vowels` translation key, which should contain all vowels for that translation with
 * no delimiters.
 *
 * @return `true` if the string starts with a vowel, `false` otherwise.
 */
public fun String.startsWithVowel(locale: Locale, bot: ExtensibleBot): Boolean =
    bot.translationsProvider.translate("utils.string.vowels", locale)
        .any { startsWith(it, true) }

/**
 * Parse a string into a boolean, based on English characters.
 *
 * This function operates based on the first character of the string, following these rules:
 *
 * * `0`, `n`, `f` -> `false`
 * * `1`, `y`, `t` -> `true`
 * * Anything else -> null
 */
public fun String.parseBoolean(): Boolean? = when (firstOrNull()?.toLowerCase()) {
    '0', 'n', 'f' -> false

    '1', 'y', 't' -> true

    else -> null
}

/**
 * Parse a string into a boolean, based on the context's detected locale.
 *
 * This function uses a full match based on whatever is specified in the translations, lower-cased.
 *
 * * `utils.string.false` for `false` values
 * * `utils.string.true` for `true` values
 *
 * Translations may contain commas, in which case any of the given values will be suitable.
 */
public suspend fun String.parseBoolean(context: CommandContext): Boolean? {
    val trueValues = context.translate("utils.string.true").split(',').map { it.trim() }
    val falseValues = context.translate("utils.string.false").split(',').map { it.trim() }

    val lower = toLowerCase()

    return when {
        falseValues.contains(lower) -> false
        trueValues.contains(lower) -> true

        else -> null
    }
}

/**
 * Parse a string into a boolean, based on the provided locale object.
 *
 * This function uses a full match based on whatever is specified in the translations, lower-cased.
 *
 * * `utils.string.false` for `false` values
 * * `utils.string.true` for `true` values
 *
 * Translations may contain commas, in which case any of the given values will be suitable.
 */
public fun String.parseBoolean(bot: ExtensibleBot, locale: Locale): Boolean? {
    val trueValues = bot.translationsProvider.translate("utils.string.true", locale)
        .split(',')
        .map { it.trim() }

    val falseValues = bot.translationsProvider.translate("utils.string.false", locale)
        .split(',')
        .map { it.trim() }

    val lower = toLowerCase()

    return when {
        falseValues.contains(lower) -> false
        trueValues.contains(lower) -> true

        else -> null
    }
}

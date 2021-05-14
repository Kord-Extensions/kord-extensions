package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
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
public fun String.parseBoolean(locale: Locale): Boolean? {
    val translationsProvider = getKoin().get<TranslationsProvider>()

    val trueValues = translationsProvider.translate("utils.string.true", locale)
        .split(',')
        .map { it.trim() }

    val falseValues = translationsProvider.translate("utils.string.false", locale)
        .split(',')
        .map { it.trim() }

    val lower = toLowerCase()

    return when {
        falseValues.contains(lower) -> false
        trueValues.contains(lower) -> true

        else -> null
    }
}

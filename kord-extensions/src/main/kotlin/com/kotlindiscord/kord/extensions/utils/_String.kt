package com.kotlindiscord.kord.extensions.utils

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

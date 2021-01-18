package com.kotlindiscord.kord.extensions.utils

/**
 * Return a [Pair] made of the start of the string up to the first character matching the predicate excluded,
 * and the rest of the string.
 *
 * @param predicate Predicate used to determine the split.
 */
public fun String.splitOn(predicate: (Char) -> Boolean): Pair<String, String> {
    val i = this.indexOfFirst(predicate)
    if (i == -1) {
        return this to ""
    }
    return slice(IntRange(0, i - 1)) to slice(IntRange(i, this.length - 1))
}

/**
 * Check whether a string starts with a vowel.
 *
 * @return `true` if the string starts with an English vowel, `false` otherwise.
 */
public fun String.startsWithVowel(): Boolean = "aeiou".any { startsWith(it) }

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

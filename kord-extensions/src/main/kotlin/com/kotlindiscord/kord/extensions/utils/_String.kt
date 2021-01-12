@file:JvmMultifileClass
@file:JvmName("StringKt")

package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.ParseException
import kotlin.jvm.Throws

/**
 * Regex to know if a [String] corresponding to a [Boolean] `true` value, in several language format.
 */
private val BOOL_TRUE_REGEX by lazy {
    Regex("1|y(es)?|t(rue)?", RegexOption.IGNORE_CASE)
}

/**
 * Regex to know if a [String] corresponding to a [Boolean] `false` value, in several language format.
 */
private val BOOL_FALSE_REGEX by lazy {
    Regex("0|no?|f(alse)?", RegexOption.IGNORE_CASE)
}

/**
 * Check whether a string starts with a vowel.
 * The vowel considered are : a, e, i, o, u
 *
 * @return `true` if the string starts with an English vowel, `false` otherwise.
 */
public fun String.startsWithVowel(): Boolean =
    "aeiou".any { startsWith(it, true) }

/**
 * Parse a string into a boolean, based on English characters.
 * @receiver String that will be analyzed
 * @return `true` if the value parsable to a Boolean true value,
 * `false` if the value parsable to a Boolean false value
 * @throws ParseException Exception if the value cannot be parsed to a true or false Boolean value
 * @see [parseBooleanOrNull]
 */
@Throws(ParseException::class)
public fun String.parseBoolean(): Boolean =
    parseBooleanOrNull() ?: throw ParseException("The value cannot be parsed to a Boolean value")

/**
 * Parse a string into a boolean, based on English characters.
 *
 * This function operates based on the first character of the string, following these rules:
 *
 * * `0`, `n`, `no`, `f`, `false` -> `false`
 * * `1`, `y`, `yes`, `t`, `true` -> `true`
 * * Anything else -> null
 * @receiver String that will be analyzed
 * @return `true` if the value parsable to a Boolean rue value,
 * `false` if the value parsable to a Boolean false value,
 * `null` otherwise
 */
public fun String.parseBooleanOrNull(): Boolean? = when {
    isEmpty() -> null
    matches(BOOL_TRUE_REGEX) -> true
    matches(BOOL_FALSE_REGEX) -> false
    else -> null
}

/**
 * Return a [Pair] made of the start of the string up to the first character matching the predicate excluded,
 * and the rest of the string.
 *
 * @param predicate Predicate used to determine the split.
 */
public fun String.splitOn(predicate: (Char) -> Boolean): Pair<String, String> {
    val i = indexOfFirst(predicate)
    return if (i == -1) {
        this to ""
    } else slice(IntRange(0, i - 1)) to slice(IntRange(i, this.length - 1))
}

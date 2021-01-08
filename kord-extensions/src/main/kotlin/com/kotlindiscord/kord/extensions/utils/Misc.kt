@file:JvmMultifileClass
@file:JvmName("MiscKt")

package com.kotlindiscord.kord.extensions.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Return a [Pair] made of the start of the string up to the first character matching the predicate excluded,
 * and the rest of the string.
 *
 * @param predicate Predicate used to determine the split.
 */
public fun String.splitOn(predicate: (Char) -> Boolean): Pair<String, String> {
    val i = this.indexOfFirst(predicate)
    if (i == -1) {
        return Pair(this, "")
    }
    return Pair(this.slice(IntRange(0, i - 1)), this.slice(IntRange(i, this.length - 1)))
}

/**
 * Run a block of code within a coroutine scope, defined by a given dispatcher.
 *
 * This is intended for use with code that normally isn't designed to be run within a coroutine, such as
 * database actions.
 *
 * @param dispatcher The dispatcher to use - defaults to [Dispatchers.IO].
 * @param body The block of code to be run.
 */
public suspend fun <T> runSuspended(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    body: suspend CoroutineScope.() -> T
): T =
    withContext(dispatcher, body)

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
    '0' -> false
    'n' -> false
    'f' -> false

    '1' -> true
    'y' -> true
    't' -> true

    else -> null
}

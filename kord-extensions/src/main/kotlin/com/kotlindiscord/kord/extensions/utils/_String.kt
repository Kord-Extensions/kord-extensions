/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.parsers.BooleanParser
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
public suspend fun String.parseBoolean(context: CommandContext): Boolean? = parseBoolean(context.getLocale())

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
public fun String.parseBoolean(locale: Locale): Boolean? = BooleanParser.parse(this, locale)

/**
 * Capitalize words in this string according to the given locale. Uses the Java default locale if none is
 * provided.
 */
public fun String.capitalizeWords(locale: Locale? = null): String {
	return split(" ").joinToString(" ") { word ->
		word.replaceFirstChar {
			if (it.isLowerCase()) {
				it.titlecase(locale ?: Locale.getDefault())
			} else {
				it.toString()
			}
		}
	}
}

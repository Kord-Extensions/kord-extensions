/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.utils.linkie

import me.shedaniel.linkie.Obf

/** Format this obfuscated member as a pair of strings, client to server. **/
fun Obf.stringPairs(): Pair<String?, String?> = when {
	isEmpty() -> "" to null
	isMerged() -> merged!! to null

	else -> client to server
}

/**
 *  If not null or equal to the given string, return the string with the given mapping lambda applied, otherwise null.
 */
inline fun String?.mapIfNotNullOrNotEquals(other: String, mapper: (String) -> String): String? =
	when {
		isNullOrEmpty() -> null
		this == other -> null
		else -> mapper(this)
	}

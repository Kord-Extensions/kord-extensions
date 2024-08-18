/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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

/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.pluralkit.utils

/**
 * A [LinkedHashMap] that only stores up to [maxSize] elements, dropping the oldest entries in order to maintain this
 * max size.
 */
class LRUHashMap<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>() {
	override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean =
		size > maxSize
}

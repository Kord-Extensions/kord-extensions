/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.Event
import dev.kordex.core.i18n.types.Key
import java.util.*

/** Type alias representing a string keyed map. **/
public typealias StringKeyedMap<T> = Map<String, T & Any>

/** Type alias representing a map with string keys and values. **/
public typealias StringMap = StringKeyedMap<String>

/** Type alias representing a mutable string keyed map. **/
public typealias MutableStringKeyedMap<T> = MutableMap<String, T & Any>

/** Type alias representing a mutable map with string keys and values. **/
public typealias MutableStringMap = MutableStringKeyedMap<String>

/** Provides direct access to the map KordEx registers for [Event.customContext]. **/
@OptIn(KordPreview::class)
@Suppress("UNCHECKED_CAST")
public val Event.extraData: MutableStringKeyedMap<Any>
	get() =
		customContext as MutableStringKeyedMap<Any>

/**
 * Utility function for getting a key from the given String-keyed map, attempting to cast it to the given generic
 * type, [T]. Will throw if the key is missing or the value cannot be cast.
 *
 * **Note:** This function does not support maps with nullable values.
 *
 * @throws IllegalArgumentException when the map doesn't contain the given key
 */
public inline fun <reified T : Any> StringKeyedMap<*>.getOf(key: String): T =
	(this[key] ?: throw IllegalArgumentException("Map does not contain key: $key"))
		as T

/**
 * Utility function for getting a key from the given String-keyed map, attempting to cast it to the given generic
 * type, [T]. Will return the provided [default] value if the key is missing or the value cannot be cast.
 *
 * **Note:** This function does not support maps with nullable values.
 */
public inline fun <reified T : Any> StringKeyedMap<*>.getOfOrDefault(key: String, default: T): T =
	this[key] as? T ?: default

/**
 * Utility function for getting a key from the given String-keyed map, attempting to cast it to the given generic
 * type, [T]. Will return null if the key is missing or the value cannot be cast.
 *
 * **Note:** This function does not support maps with nullable values.
 */
public inline fun <reified T : Any?> StringKeyedMap<*>.getOfOrNull(key: String): T? =
	this[key] as? T

/**
 * Utility function for getting a key from the given String-keyed map, attempting to cast it to the given generic
 * type, [T]. Will return the provided [default] value if they key is missing or the value cannot be cast.
 *
 * If [store] is `true`, this will store the given default value if the key is missing or the value cannot be cast.
 *
 * **Note:** This function does not support maps with nullable values.
 */
public inline fun <reified V : Any, reified T : V> MutableStringKeyedMap<V>.getOfOrDefault(
	key: String,
	default: T,
	store: Boolean,
): T {
	val value = this[key] as? T

	if (value == null) {
		if (store) {
			this[key] = default
		}

		return default
	}

	return value
}

/** For string-keyed maps, attempt to retrieve a value using a case-insensitive key. **/
public fun <V : Any> Map<String, V>.getIgnoringCase(key: String, locale: Locale? = null): V? {
	val lowerCase = entries.associate { it.key.lowercase() to it.value }

	return if (locale != null) {
		lowerCase[key.lowercase(locale)]
	} else {
		lowerCase[key.lowercase()]
	}
}

/** For Key-keyed maps, attempt to retrieve a value using a case-insensitive key. **/
@JvmName("getIgnoringCaseKeyed")
public fun <V : Any> Map<Key, V>.getIgnoringCase(key: String, locale: Locale? = null): V? {
	val lowerCase = entries.associate { it.key.withLocale(locale).translate().lowercase() to it.value }

	return if (locale != null) {
		lowerCase[key.lowercase(locale)]
	} else {
		lowerCase[key.lowercase()]
	}
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

internal typealias StringKeyedMap<T> = Map<String, T & Any>

/** Type alias representing a mutable string keyed map. **/
public typealias MutableStringKeyedMap<T> = MutableMap<String, T & Any>

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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.annotations.converters

/**
 * Enum representing different types of converter functions.
 *
 * @property fragment String fragment to add to the converter type name
 * @property order Order of types - for naming order and compatibility
 * @property appendFragment Whether to append the string fragment, or ignore it
 */
public enum class ConverterType(
	public val fragment: String,
	public val order: Int,
	public val appendFragment: Boolean = true,
) {
	DEFAULTING("Defaulting", 0),
	LIST("List", 0),
	OPTIONAL("Optional", 0),

	COALESCING("Coalescing", 1),
	SINGLE("", 1),

	CHOICE("Choice", 2, false);

	public companion object {
		/** Given the `.name` or `.simpleName` of a converter type, get the relevant enum entry. **/
		public fun fromName(name: String): ConverterType? =
			when (name) {
				DEFAULTING.name -> DEFAULTING
				LIST.name -> LIST
				OPTIONAL.name -> OPTIONAL

				COALESCING.name -> COALESCING
				SINGLE.name -> SINGLE

				CHOICE.name -> CHOICE

				else -> null
			}
	}
}

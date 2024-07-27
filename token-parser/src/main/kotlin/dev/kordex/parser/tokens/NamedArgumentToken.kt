/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.parser.tokens

/**
 * Data class representing a single named argument token.
 *
 * @param name Token name
 * @param data Argument data
 */
public data class NamedArgumentToken(
	public val name: String,
	override val data: String,
) : Token<String>

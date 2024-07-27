/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.parser.tokens

/**
 * Simple base class for a parser token. Exists in order to make changes later easier.
 */
public interface Token<T : Any?> {
	/** Stored token data. **/
	public val data: T
}

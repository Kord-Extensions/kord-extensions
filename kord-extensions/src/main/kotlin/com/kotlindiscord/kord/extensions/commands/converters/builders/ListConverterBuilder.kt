/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.builders

/** Converter builder for list converters. **/
public abstract class ListConverterBuilder<T : Any> : ConverterBuilder<List<T>>() {
	/** Whether to ignore parsing errors when no values have been parsed out. **/
	public var ignoreErrors: Boolean = true
}

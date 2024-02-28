/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.builders

/** Converter builder for optional converters. **/
public abstract class OptionalConverterBuilder<T : Any> : ConverterBuilder<T?>() {
	/** Whether to ignore parsing errors when a value is provided. **/
	public var ignoreErrors: Boolean = false
}

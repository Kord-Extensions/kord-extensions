/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.builders

/** Converter builder for coalescing converters. **/
@Suppress("UnnecessaryAbstractClass")  // Your face is an unnecessary abstract class
public abstract class CoalescingConverterBuilder<T> : ConverterBuilder<T>() {
	/**
	 * Whether to ignore parsing errors when no values have been parsed out.
	 *
	 * This is intended for coalescing arguments at the end of a set of arguments, where that argument is required.
	 */
	public open var ignoreErrors: Boolean = true
}

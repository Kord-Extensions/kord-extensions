/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.converters.builders

import dev.kordex.core.InvalidArgumentException

/** Converter builder for defaulting converters. **/
public abstract class DefaultingConverterBuilder<T : Any> : ConverterBuilder<T>() {
	/** Whether to ignore parsing errors when a value is provided. **/
	public var ignoreErrors: Boolean = false

	/** Value to use when none is provided, or when there's a parsing error and [ignoreErrors] is `true`. **/
	public open lateinit var defaultValue: T

	override fun validateArgument() {
		super.validateArgument()

		if (!this::defaultValue.isInitialized) {
			throw InvalidArgumentException(this, "Required field not provided: defaultValue")
		}
	}
}

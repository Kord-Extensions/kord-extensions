/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.builders

import dev.kordex.core.InvalidArgumentException

/** Converter builder for defaulting coalescing converters. **/
public abstract class DefaultingCoalescingConverterBuilder<T : Any> : CoalescingConverterBuilder<T>() {
	override var ignoreErrors: Boolean = false

	/** Value to use when none is provided, or when there's a parsing error and [ignoreErrors] is `true`. **/
	public open lateinit var defaultValue: T

	override fun validateArgument() {
		super.validateArgument()

		if (!this::defaultValue.isInitialized) {
			throw InvalidArgumentException(this, "Required field not provided: defaultValue")
		}
	}
}

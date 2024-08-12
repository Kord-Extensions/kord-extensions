/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.builders

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

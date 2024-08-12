/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.builders

/** Converter builder for list converters. **/
public abstract class ListConverterBuilder<T : Any> : ConverterBuilder<List<T>>() {
	/** Whether to ignore parsing errors when no values have been parsed out. **/
	public var ignoreErrors: Boolean = true
}

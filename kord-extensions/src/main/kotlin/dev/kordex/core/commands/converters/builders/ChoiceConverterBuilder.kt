/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.builders

import dev.kordex.core.i18n.types.Key

/** Converter builder for choice converters. **/
public interface ChoiceConverterBuilder<T> {
	/** List of possible choices, if any. **/
	public var choices: MutableMap<Key, T>

	/** Add a choice to the list of possible choices. **/
	public fun choice(key: Key, value: T) {
		choices[key] = value
	}

	/** Add a choice to the list of possible choices. **/
	public fun choices(all: Map<Key, T>) {
		choices = all.toMutableMap()
	}
}

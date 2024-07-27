/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.converters.builders

import dev.kordex.core.utils.MutableStringKeyedMap

/** Converter builder for choice converters. **/
public interface ChoiceConverterBuilder<T> {
	/** List of possible choices, if any. **/
	public var choices: MutableStringKeyedMap<T>

	/** Add a choice to the list of possible choices. **/
	public fun choice(key: String, value: T) {
		choices[key] = value
	}

	/** Add a choice to the list of possible choices. **/
	public fun choices(all: Map<String, T>) {
		choices = all.toMutableMap()
	}
}

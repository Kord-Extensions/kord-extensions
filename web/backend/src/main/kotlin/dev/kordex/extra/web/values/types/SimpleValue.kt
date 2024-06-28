/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.values.types

import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.optional
import dev.kordex.extra.web.types.Identifier
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

public class SimpleValue<T : Any> public constructor(
	override val identifier: Identifier,
	override val writable: Boolean,
	override val serializer: KSerializer<T>,
) : Value<T?, T?, T>() {
	private var optional: Optional<T?> = Optional.Missing()

	public override fun read(): T? =
		if (optional is Optional.Missing) {
			error("Value has not been set.")
		} else {
			optional.value
		}

	public override fun write(value: T?) {
		this.optional = value.optional()
	}

	public fun clear() {
		this.optional = Optional.Missing()
	}
}

public inline fun <reified T : Any> SimpleValue(
	identifier: Identifier,
	writable: Boolean
): SimpleValue<T> = SimpleValue(identifier, writable, serializer())

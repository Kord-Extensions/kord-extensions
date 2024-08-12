/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.values.types

import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.optional
import dev.kordex.modules.web.core.backend.types.Identifier
import dev.kordex.modules.web.core.backend.values.serializers.SimpleValueSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable(with = SimpleValueSerializer::class)
public class SimpleValue<T : Any> public constructor(
	override val identifier: Identifier,
	override val writable: Boolean,
	override val serializer: KSerializer<T>,
) : Value<T?, T?, T>() {
	internal var optional: Optional<T?> = Optional.Missing()

	public override fun read(): T? =
		if (optional is Optional.Missing) {
			error("Value has not been set.")
		} else {
			optional.value
		}

	public override fun write(value: T?) {
		this.optional = value.optional()
	}

	public fun writeOptional(value: Optional<T?>) {
		this.optional = value
	}

	public fun clear() {
		this.optional = Optional.Missing()
	}
}

public inline fun <reified T : Any> SimpleValue(
	identifier: Identifier,
	writable: Boolean,
): SimpleValue<T> = SimpleValue(identifier, writable, serializer())

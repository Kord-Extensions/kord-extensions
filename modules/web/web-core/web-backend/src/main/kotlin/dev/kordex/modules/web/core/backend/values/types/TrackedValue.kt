/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.values.types

import dev.kordex.core.utils.collections.FixedLengthQueue
import dev.kordex.modules.web.core.backend.types.Identifier
import dev.kordex.modules.web.core.backend.values.TimedContainer
import dev.kordex.modules.web.core.backend.values.ValueInterval
import dev.kordex.modules.web.core.backend.values.serializers.TrackedValueSerializer
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable(with = TrackedValueSerializer::class)
public class TrackedValue<T : Any>(
	override val identifier: Identifier,

	public val maxValues: Int = 48,
	public val precision: ValueInterval = ValueInterval.HalfHour,

	public override val serializer: KSerializer<T>,
) : Value<T?, List<TimedContainer<T?>>, T>() {
	internal val values: FixedLengthQueue<TimedContainer<T?>> = FixedLengthQueue(maxValues)

	public override fun read(): List<TimedContainer<T?>> =
		values.getAll()

	public override fun write(value: T?) {
		values.push(
			TimedContainer(value, Clock.System.now())
		)
	}

	public fun writeTimed(value: TimedContainer<T?>) {
		values.push(value)
	}
}

public inline fun <reified T : Any> TrackedValue(
	identifier: Identifier,
	maxValues: Int = 48,
	precision: ValueInterval = ValueInterval.HalfHour,
): TrackedValue<T> = TrackedValue(identifier, maxValues, precision, serializer())

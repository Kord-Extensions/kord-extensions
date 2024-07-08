/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.values.types

import com.kotlindiscord.kord.extensions.utils.collections.FixedLengthQueue
import dev.kordex.extra.web.types.Identifier
import dev.kordex.extra.web.values.TimedContainer
import dev.kordex.extra.web.values.ValueInterval
import dev.kordex.extra.web.values.serializers.TrackedValueSerializer
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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.values.serializers

import dev.kordex.modules.web.core.backend.values.TimedContainer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

public class TimedContainerSerializer<T : Any?>(
	private val dataSerializer: KSerializer<T>,
) : KSerializer<TimedContainer<T>> {
	override val descriptor: SerialDescriptor =
		buildClassSerialDescriptor("TimedContainer", dataSerializer.descriptor) {
			element<Instant>("time")
			element("value", dataSerializer.descriptor)
		}

	override fun deserialize(decoder: Decoder): TimedContainer<T> {
		var value: T? = null
		var time: Instant = Clock.System.now()

		decoder.decodeStructure(descriptor) {
			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> time = decodeSerializableElement(descriptor, index, Instant.serializer())
					1 -> value = decodeSerializableElement(descriptor, index, dataSerializer)

					CompositeDecoder.DECODE_DONE -> break

					else -> error("Unexpected descriptor index: $index")
				}
			}
		}

		@Suppress("UNCHECKED_CAST")
		return TimedContainer(value as T, time)
	}

	override fun serialize(encoder: Encoder, value: TimedContainer<T>) {
		encoder.encodeStructure(descriptor) {
			encodeSerializableElement(descriptor, 0, Instant.serializer(), value.time)
			encodeSerializableElement(descriptor, 1, dataSerializer, value.value)
		}
	}
}

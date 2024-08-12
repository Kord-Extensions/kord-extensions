/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("MagicNumber")

package dev.kordex.modules.web.core.backend.values.serializers

import dev.kordex.core.utils.collections.FixedLengthQueue
import dev.kordex.core.utils.collections.serializers.FixedLengthQueueSerializer
import dev.kordex.modules.web.core.backend.types.Identifier
import dev.kordex.modules.web.core.backend.values.TimedContainer
import dev.kordex.modules.web.core.backend.values.ValueInterval
import dev.kordex.modules.web.core.backend.values.types.TrackedValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json

public class TrackedValueSerializer<T : Any>(
	private val dataSerializer: KSerializer<T>,
) : KSerializer<TrackedValue<T>> {
	private val queueSerializer = FixedLengthQueueSerializer(
		TimedContainer.serializer(dataSerializer.nullable)
	)

	override val descriptor: SerialDescriptor =
		buildClassSerialDescriptor("TrackedValue", dataSerializer.descriptor) {
			element<Identifier>("identifier")
			element<Int>("maxValues")
			element<ValueInterval>("precision")
			element("values", queueSerializer.descriptor)
		}

	override fun deserialize(decoder: Decoder): TrackedValue<T> {
		var identifier = Identifier("", "")
		var maxValues = 48
		var precision: ValueInterval = ValueInterval.HalfHour
		var values: FixedLengthQueue<TimedContainer<T?>> = FixedLengthQueue(1)

		decoder.decodeStructure(descriptor) {
			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> identifier = decodeSerializableElement(descriptor, index, Identifier.serializer())
					1 -> maxValues = decodeIntElement(descriptor, index)
					2 -> precision = decodeSerializableElement(descriptor, index, ValueInterval.serializer())
					3 -> values = decodeSerializableElement(descriptor, index, queueSerializer)

					CompositeDecoder.DECODE_DONE -> break

					else -> error("Unexpected descriptor index: $index")
				}
			}
		}

		val value = TrackedValue(
			identifier,
			maxValues,
			precision,
			dataSerializer
		)

		values.forEach { value.writeTimed(it) }

		return value
	}

	override fun serialize(encoder: Encoder, value: TrackedValue<T>) {
		encoder.encodeStructure(descriptor) {
			encodeSerializableElement(descriptor, 0, Identifier.serializer(), value.identifier)
			encodeIntElement(descriptor, 1, value.maxValues)
			encodeSerializableElement(descriptor, 2, ValueInterval.serializer(), value.precision)
			encodeSerializableElement(descriptor, 3, queueSerializer, value.values)
		}
	}
}

public fun main() {
	val value = TrackedValue<String>(
		Identifier("a:b"),
		48,
		ValueInterval.HalfHour,
	)

	value.write("a")
	value.write("b")
	value.write("c")
	value.write("d")

	val string = Json.encodeToString(value)
	val output = Json.decodeFromString<TrackedValue<String>>(string)

	println("== INPUT ==")
	println("Max Values: ${value.maxValues}")
	println("Precision: ${value.precision}")
	println("Values: ${value.read()}")
	println()

	println("== SERIALIZED ==")
	println(string)
	println()

	println("== OUTPUT ==")
	println("Max Values: ${output.maxValues}")
	println("Precision: ${output.precision}")
	println("Values: ${output.read()}")
}

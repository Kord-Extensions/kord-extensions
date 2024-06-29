/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("MagicNumber")

package com.kotlindiscord.kord.extensions.utils.collections.serializers

import com.kotlindiscord.kord.extensions.utils.collections.FixedLengthQueue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json

/**
 * Serializer for fixed-length queues.
 *
 * Serialises to an object containing "maxSize" and "contents" keys.
 */
public class FixedLengthQueueSerializer<T : Any?>(
	private val dataSerializer: KSerializer<T>,
) : KSerializer<FixedLengthQueue<T>> {
	private val listSerializer = ListSerializer(dataSerializer)

	override val descriptor: SerialDescriptor =
		buildClassSerialDescriptor("FixedLengthQueue", dataSerializer.descriptor) {
			element<Int>("maxSize")
			element("contents", listSerializer.descriptor)
		}

	override fun deserialize(decoder: Decoder): FixedLengthQueue<T> {
		var maxSize = 0
		var reversedElements: List<T> = emptyList()

		decoder.decodeStructure(descriptor) {
			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					0 -> maxSize = decodeIntElement(descriptor, 0)
					1 -> reversedElements = decodeSerializableElement(descriptor, 1, listSerializer)

					CompositeDecoder.DECODE_DONE -> break

					else -> error("Unexpected descriptor index: $index")
				}
			}

			require(maxSize > 0)
		}

		val queue = FixedLengthQueue<T>(maxSize)

		queue.addAll(reversedElements.reversed())

		return queue
	}

	override fun serialize(encoder: Encoder, value: FixedLengthQueue<T>) {
		val allElements = value.getAll()

		encoder.encodeStructure(descriptor) {
			encodeIntElement(descriptor, 0, value.maxSize)
			encodeSerializableElement(descriptor, 1, listSerializer, allElements)
		}
	}
}

public fun main() {
	val queue = FixedLengthQueue<String>(4)

	queue.addAll(listOf("1", "2", "3", "4"))

	val string = Json.encodeToString(queue)
	val output = Json.decodeFromString<FixedLengthQueue<String>>(string)

	println("== INPUT ==")
	println("Size: ${queue.maxSize}")
	println("Elements: ${queue.getAll()}")
	println()

	println("== SERIALIZED ==")
	println(string)
	println()

	println("== OUTPUT ==")
	println("Size: ${output.maxSize}")
	println("Elements: ${output.getAll()}")
}

/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("MagicNumber")

package dev.kordex.modules.web.core.backend.values.serializers

import dev.kord.common.entity.optional.Optional
import dev.kordex.modules.web.core.backend.types.Identifier
import dev.kordex.modules.web.core.backend.values.types.SimpleValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

public class SimpleValueSerializer<T : Any>(
	private val dataSerializer: KSerializer<T>,
) : KSerializer<SimpleValue<T>> {
	private val optionalSerializer = Optional.serializer(dataSerializer.nullable)

	override val descriptor: SerialDescriptor =
		buildClassSerialDescriptor("SimpleValue", dataSerializer.descriptor) {
			element<Identifier>("identifier")
			element<Boolean>("writable")
			element("value", optionalSerializer.descriptor)
		}

	override fun deserialize(decoder: Decoder): SimpleValue<T> {
		var identifier = Identifier("", "")
		var writeable = true
		var value: Optional<T?> = Optional.Missing()

		decoder.decodeStructure(descriptor) {
			while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					1 -> identifier = decodeSerializableElement(descriptor, index, Identifier.serializer())
					2 -> writeable = decodeBooleanElement(descriptor, index)
					3 -> value = decodeSerializableElement(descriptor, index, optionalSerializer)

					CompositeDecoder.DECODE_DONE -> break

					else -> error("Unexpected descriptor index: $index")
				}
			}
		}

		val result = SimpleValue(identifier, writeable, dataSerializer)

		result.writeOptional(value)

		return result
	}

	override fun serialize(encoder: Encoder, value: SimpleValue<T>) {
		encoder.encodeStructure(descriptor) {
			encodeSerializableElement(descriptor, 0, Identifier.serializer(), value.identifier)
			encodeBooleanElement(descriptor, 1, value.writable)
			encodeSerializableElement(descriptor, 2, optionalSerializer, value.optional)
		}
	}
}

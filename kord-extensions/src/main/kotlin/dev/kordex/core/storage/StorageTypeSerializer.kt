/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Simple serializer for the [StorageType] sealed class. **/
public class StorageTypeSerializer : KSerializer<StorageType> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StorageType", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): StorageType =
		when (val string = decoder.decodeString()) {
			StorageType.Config.type -> StorageType.Config
			StorageType.Data.type -> StorageType.Data

			else -> error("Unknown storage type: $string")
		}

	override fun serialize(encoder: Encoder, value: StorageType) {
		encoder.encodeString(value.type)
	}
}

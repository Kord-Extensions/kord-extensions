/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public class IdentifierSerializer : KSerializer<Identifier> {
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): Identifier =
		Identifier(decoder.decodeString())

	override fun serialize(encoder: Encoder, value: Identifier) {
		encoder.encodeString(value.toString())
	}
}

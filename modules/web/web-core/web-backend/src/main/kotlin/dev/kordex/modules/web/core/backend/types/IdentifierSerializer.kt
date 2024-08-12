/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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

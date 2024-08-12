/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.components.forms

public sealed class InitialUnsafeModalResponse {
	public data object EphemeralAck : InitialUnsafeModalResponse()
	public data object PublicAck : InitialUnsafeModalResponse()

	public data class EphemeralResponse(val builder: InitialEphemeralModalResponseBuilder) :
		InitialUnsafeModalResponse()

	public data class PublicResponse(val builder: InitialPublicModalResponseBuilder) :
		InitialUnsafeModalResponse()
}

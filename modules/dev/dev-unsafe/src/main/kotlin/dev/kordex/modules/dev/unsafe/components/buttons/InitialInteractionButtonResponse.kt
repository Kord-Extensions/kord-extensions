/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.components.buttons

import dev.kordex.core.components.buttons.InitialEphemeralButtonResponseBuilder
import dev.kordex.core.components.buttons.InitialPublicButtonResponseBuilder

public sealed class InitialInteractionButtonResponse {
	public data object EphemeralAck : InitialInteractionButtonResponse()
	public data object PublicAck : InitialInteractionButtonResponse()
	public data object None : InitialInteractionButtonResponse()

	public data class EphemeralResponse(val builder: InitialEphemeralButtonResponseBuilder) :
		InitialInteractionButtonResponse()

	public data class PublicResponse(val builder: InitialPublicButtonResponseBuilder) :
		InitialInteractionButtonResponse()
}

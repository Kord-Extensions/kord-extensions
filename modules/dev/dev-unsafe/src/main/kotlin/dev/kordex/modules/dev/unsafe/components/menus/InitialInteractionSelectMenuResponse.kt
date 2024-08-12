/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.components.menus

import dev.kordex.core.components.menus.InitialEphemeralSelectMenuResponseBuilder
import dev.kordex.core.components.menus.InitialPublicSelectMenuResponseBuilder

public sealed class InitialInteractionSelectMenuResponse {
	public data object EphemeralAck : InitialInteractionSelectMenuResponse()
	public data object PublicAck : InitialInteractionSelectMenuResponse()
	public data object None : InitialInteractionSelectMenuResponse()

	public data class EphemeralResponse(val builder: InitialEphemeralSelectMenuResponseBuilder) :
		InitialInteractionSelectMenuResponse()

	public data class PublicResponse(val builder: InitialPublicSelectMenuResponseBuilder) :
		InitialInteractionSelectMenuResponse()
}

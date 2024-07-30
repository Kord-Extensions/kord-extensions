/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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

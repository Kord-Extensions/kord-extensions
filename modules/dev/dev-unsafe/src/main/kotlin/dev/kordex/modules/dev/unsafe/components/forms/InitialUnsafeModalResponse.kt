/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.types

import dev.kordex.core.commands.application.message.InitialEphemeralMessageResponseBuilder
import dev.kordex.core.commands.application.message.InitialPublicMessageResponseBuilder
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI

/** Sealed class representing the initial response types for an unsafe message command. **/
@UnsafeAPI
public sealed class InitialMessageCommandResponse {
	/** Respond with an ephemeral ack, without any content. **/
	public object EphemeralAck : InitialMessageCommandResponse()

	/** Respond with a public ack, without any content. **/
	public object PublicAck : InitialMessageCommandResponse()

	/** Don't respond. Warning: You may not be able to respond in time! **/
	public object None : InitialMessageCommandResponse()

	/**
	 * Respond with an ephemeral ack, including message content.
	 *
	 * @param builder Response builder, containing the message content.
	 */
	public data class EphemeralResponse(val builder: InitialEphemeralMessageResponseBuilder) :
		InitialMessageCommandResponse()

	/**
	 * Respond with a public ack, including message content.
	 *
	 * @param builder Response builder, containing the message content.
	 **/
	public data class PublicResponse(val builder: InitialPublicMessageResponseBuilder) :
		InitialMessageCommandResponse()
}

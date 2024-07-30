/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.commands.slash

import dev.kordex.core.commands.application.slash.InitialEphemeralSlashResponseBuilder
import dev.kordex.core.commands.application.slash.InitialPublicSlashResponseBehavior
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI

/** Sealed class representing the initial response types for an unsafe slash command. **/
@UnsafeAPI
public sealed class InitialSlashCommandResponse {
	/** Respond with an ephemeral ack, without any content. **/
	public object EphemeralAck : InitialSlashCommandResponse()

	/** Respond with a public ack, without any content. **/
	public object PublicAck : InitialSlashCommandResponse()

	/** Don't respond. Warning: You may not be able to respond in time! **/
	public object None : InitialSlashCommandResponse()

	/**
	 * Respond with an ephemeral ack, including message content.
	 *
	 * @param builder Response builder, containing the message content.
	 */
	public data class EphemeralResponse(val builder: InitialEphemeralSlashResponseBuilder) :
		InitialSlashCommandResponse()

	/**
	 * Respond with a public ack, including message content.
	 *
	 * @param builder Response builder, containing the message content.
	 **/
	public data class PublicResponse(val builder: InitialPublicSlashResponseBehavior) :
		InitialSlashCommandResponse()
}

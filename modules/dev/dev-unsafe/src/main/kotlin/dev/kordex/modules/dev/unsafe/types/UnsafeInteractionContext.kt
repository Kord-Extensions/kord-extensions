/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")
@file:OptIn(KordUnsafe::class)

package dev.kordex.modules.dev.unsafe.types

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.response.*
import dev.kord.core.entity.interaction.followup.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.core.entity.interaction.response.MessageInteractionResponse
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import dev.kordex.core.annotations.AlwaysPublicResponse
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.pagination.BaseButtonPaginator
import dev.kordex.core.pagination.builders.PaginatorBuilder
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import java.util.*

/** Interface representing a generic, unsafe interaction action context. **/
@UnsafeAPI
public interface UnsafeInteractionContext<R : MessageInteractionResponseBehavior, E : InteractionCreateEvent> {
	/** Response created by acknowledging the interaction. Generic. **/
	public var interactionResponse: R?

	/** Original interaction event object, for manual acks. **/
	public val event: InteractionCreateEvent

	// NOTE: The type system prevents us from implementing these generically.

	/** Send an ephemeral ack if you haven't acknowledged the interaction yet. **/
	@UnsafeAPI
	public suspend fun ackEphemeral(
		builder: (suspend InteractionResponseCreateBuilder.() -> Unit)? = null,
	): EphemeralMessageInteractionResponseBehavior

	/** Send a public ack if you haven't acknowledged the interaction yet. **/
	@UnsafeAPI
	public suspend fun ackPublic(
		builder: (suspend InteractionResponseCreateBuilder.() -> Unit)? = null,
	): PublicMessageInteractionResponseBehavior

	/** Respond to the current interaction with an ephemeral followup. **/
	@UnsafeAPI
	public suspend fun respondEphemeral(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): EphemeralFollowupMessage

	/** Respond to the current interaction with a public followup. **/
	@UnsafeAPI
	public suspend fun respondPublic(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): PublicFollowupMessage

	/** Edit the current interaction's response. **/
	@UnsafeAPI
	public suspend fun edit(
		builder: suspend InteractionResponseModifyBuilder.() -> Unit,
	): MessageInteractionResponse

	/** Create a paginator that edits the original interaction. **/
	@UnsafeAPI
	public suspend fun editingPaginator(
		defaultGroup: Key = EMPTY_KEY,
		locale: Locale? = null,
		builder: suspend (PaginatorBuilder).() -> Unit,
	): BaseButtonPaginator

	/** Create a paginator that creates a public follow-up message and edits it. **/
	@AlwaysPublicResponse
	@UnsafeAPI
	public suspend fun respondingPaginator(
		defaultGroup: Key = EMPTY_KEY,
		locale: Locale? = null,
		builder: suspend (PaginatorBuilder).() -> Unit,
	): BaseButtonPaginator
}

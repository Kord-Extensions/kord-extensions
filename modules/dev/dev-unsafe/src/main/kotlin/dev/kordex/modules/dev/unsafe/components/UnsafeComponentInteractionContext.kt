/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")
@file:OptIn(KordUnsafe::class)

package dev.kordex.modules.dev.unsafe.components

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.*
import dev.kord.core.entity.interaction.followup.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.core.entity.interaction.response.MessageInteractionResponse
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import dev.kordex.core.annotations.AlwaysPublicResponse
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.pagination.BaseButtonPaginator
import dev.kordex.core.pagination.EphemeralResponsePaginator
import dev.kordex.core.pagination.PublicFollowUpPaginator
import dev.kordex.core.pagination.PublicResponsePaginator
import dev.kordex.core.pagination.builders.PaginatorBuilder
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.types.UnsafeInteractionContext
import java.util.*

/** Interface representing a generic, unsafe interaction interaction button context. **/
@UnsafeAPI
public interface UnsafeComponentInteractionContext<E : ComponentInteractionCreateEvent> :
    UnsafeInteractionContext<MessageInteractionResponseBehavior, E> {

	override val event: E

	@UnsafeAPI
	override suspend fun ackEphemeral(
		builder: (suspend InteractionResponseCreateBuilder.() -> Unit)?,
	): EphemeralMessageInteractionResponseBehavior {
		if (interactionResponse != null) {
			error("The interaction has already been acknowledged.")
		}

		interactionResponse = if (builder == null) {
			event.interaction.deferEphemeralResponseUnsafe()
		} else {
			event.interaction.respondEphemeral { builder() }
		}

		return interactionResponse as EphemeralMessageInteractionResponseBehavior
	}

	@UnsafeAPI
	override suspend fun ackPublic(
		builder: (suspend InteractionResponseCreateBuilder.() -> Unit)?,
	): PublicMessageInteractionResponseBehavior {
		if (interactionResponse != null) {
			error("The interaction has already been acknowledged.")
		}

		interactionResponse = if (builder == null) {
			event.interaction.deferPublicResponseUnsafe()
		} else {
			event.interaction.respondPublic { builder() }
		}

		return interactionResponse as PublicMessageInteractionResponseBehavior
	}

	@UnsafeAPI
	override suspend fun respondEphemeral(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): EphemeralFollowupMessage =
		when (val interaction = interactionResponse) {
			is InteractionResponseBehavior -> interaction.createEphemeralFollowup { builder() }

			null -> error("Acknowledge the interaction before trying to follow-up.")
			else -> error("Unsupported initial interaction response type $interaction - please report this.")
		}

	@UnsafeAPI
	override suspend fun respondPublic(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): PublicFollowupMessage =
		when (val interaction = interactionResponse) {
			is InteractionResponseBehavior -> interaction.createPublicFollowup {
				builder()
			}

			null -> error("Acknowledge the interaction before trying to follow-up.")
			else -> error("Unsupported initial interaction response type $interaction - please report this.")
		}

	@UnsafeAPI
	override suspend fun edit(
		builder: suspend InteractionResponseModifyBuilder.() -> Unit,
	): MessageInteractionResponse =
		when (val interaction = interactionResponse) {
			is InteractionResponseBehavior -> interaction.edit { builder() }

			null -> error("Acknowledge the interaction before trying to edit it.")
			else -> error("Unsupported initial interaction response type $interaction - please report this.")
		}

	@UnsafeAPI
	override suspend fun editingPaginator(
		defaultGroup: Key,
		locale: Locale?,
		builder: suspend PaginatorBuilder.() -> Unit,
	): BaseButtonPaginator {
		val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

		builder(pages)

		return when (val interaction = interactionResponse) {
			is PublicMessageInteractionResponseBehavior -> PublicResponsePaginator(pages, interaction)
			is EphemeralMessageInteractionResponseBehavior -> EphemeralResponsePaginator(pages, interaction)

			null -> error("Acknowledge the interaction before trying to edit it.")
			else -> error("Unsupported initial interaction response type - please report this.")
		}
	}

	@Suppress("UseIfInsteadOfWhen")
	@AlwaysPublicResponse
	@UnsafeAPI
	override suspend fun respondingPaginator(
		defaultGroup: Key,
		locale: Locale?,
		builder: suspend PaginatorBuilder.() -> Unit,
	): BaseButtonPaginator {
		val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

		builder(pages)

		return when (val interaction = interactionResponse) {
			is PublicInteractionResponseBehavior -> PublicFollowUpPaginator(pages, interaction)

			null -> error("Acknowledge the interaction before trying to follow-up.")
			else -> error("Initial interaction response was not public.")
		}
	}
}

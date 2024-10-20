/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.types

import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.interaction.followup.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.core.entity.interaction.response.PublicMessageInteractionResponse
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import dev.kordex.core.annotations.AlwaysPublicResponse
import dev.kordex.core.annotations.UnexpectedFunctionBehaviour
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.pagination.PublicFollowUpPaginator
import dev.kordex.core.pagination.PublicResponsePaginator
import dev.kordex.core.pagination.builders.PaginatorBuilder
import java.util.*

/**
 * Interface representing a public interaction context.
 *
 * @see InteractionContext
 */
public interface PublicInteractionContext : InteractionContext<
	PublicMessageInteractionResponseBehavior,
	PublicMessageInteractionResponse,
	PublicFollowupMessage,
	EphemeralFollowupMessage,
	> {
	public override suspend fun respond(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): PublicFollowupMessage = interactionResponse.createPublicFollowup { builder() }

	@UnexpectedFunctionBehaviour
	public override suspend fun respondOpposite(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): EphemeralFollowupMessage = interactionResponse.createEphemeralFollowup { builder() }

	public override suspend fun edit(
		builder: suspend InteractionResponseModifyBuilder.() -> Unit,
	): PublicMessageInteractionResponse = interactionResponse.edit { builder() }

	public override fun editingPaginator(
		defaultGroup: Key,
		locale: Locale?,
		builder: (PaginatorBuilder).() -> Unit,
	): PublicResponsePaginator {
		val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

		builder(pages)

		return PublicResponsePaginator(pages, interactionResponse)
	}

	@AlwaysPublicResponse
	public override suspend fun respondingPaginator(
		defaultGroup: Key,
		locale: Locale?,
		builder: suspend PaginatorBuilder.() -> Unit,
	): PublicFollowUpPaginator {
		val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

		builder(pages)

		return PublicFollowUpPaginator(pages, interactionResponse)
	}
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.types

import com.kotlindiscord.kord.extensions.annotations.AlwaysPublicResponse
import com.kotlindiscord.kord.extensions.annotations.UnexpectedBehaviour
import com.kotlindiscord.kord.extensions.pagination.PublicFollowUpPaginator
import com.kotlindiscord.kord.extensions.pagination.PublicResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.interaction.followup.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.core.entity.interaction.response.PublicMessageInteractionResponse
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
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

	@UnexpectedBehaviour
	public override suspend fun respondOpposite(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): EphemeralFollowupMessage = interactionResponse.createEphemeralFollowup { builder() }

	public override suspend fun edit(
		builder: suspend InteractionResponseModifyBuilder.() -> Unit,
	): PublicMessageInteractionResponse = interactionResponse.edit { builder() }

	public override fun editingPaginator(
		defaultGroup: String,
		locale: Locale?,
		builder: (PaginatorBuilder).() -> Unit,
	): PublicResponsePaginator {
		val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

		builder(pages)

		return PublicResponsePaginator(pages, interactionResponse)
	}

	@AlwaysPublicResponse
	public override suspend fun respondingPaginator(
		defaultGroup: String,
		locale: Locale?,
		builder: suspend PaginatorBuilder.() -> Unit,
	): PublicFollowUpPaginator {
		val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

		builder(pages)

		return PublicFollowUpPaginator(pages, interactionResponse)
	}
}

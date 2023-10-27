/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("UnnecessaryAbstractClass")

package com.kotlindiscord.kord.extensions.types

import com.kotlindiscord.kord.extensions.annotations.AlwaysPublicResponse
import com.kotlindiscord.kord.extensions.annotations.UnexpectedBehaviour
import com.kotlindiscord.kord.extensions.pagination.BaseButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.core.entity.interaction.response.MessageInteractionResponse
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import java.util.*

/**
 * Interface representing an interaction context. Provides a generic base type for action contexts when working
 * with interactions.
 *
 * More specific types representing each interaction type (ephemeral/public) extend this class.
 *
 * @param ResponseBehavior Generic representing the relevant interaction response behavior.
 * @param FollowupType Generic representing the follow-up type for the current interaction type.
 * @param OppositeFollowupType Generic representing the opposite follow-up type for the current interaction type.
 */
public interface InteractionContext<
	ResponseBehavior,
	ResponseType : MessageInteractionResponse,
	FollowupType : FollowupMessage,
	OppositeFollowupType : FollowupMessage,
	> {
	/** Current interaction response being worked with. **/
	public val interactionResponse: ResponseBehavior

	/** Create a paginator that edits the original interaction response. **/
	public fun editingPaginator(
		defaultGroup: String = "",
		locale: Locale? = null,
		builder: (PaginatorBuilder).() -> Unit,
	): BaseButtonPaginator

	/**
	 * Create a paginator that creates a follow-up message, and edits that.
	 *
	 * This function always creates a public follow-up, as Discord prevents bots from editing ephemeral follow-ups.
	 */
	@AlwaysPublicResponse
	public fun respondingPaginator(
		defaultGroup: String = "",
		locale: Locale? = null,
		builder: (PaginatorBuilder).() -> Unit,
	): BaseButtonPaginator

	/** Edit the original interaction response. **/
	public suspend fun edit(
		builder: suspend InteractionResponseModifyBuilder.() -> Unit,
	): ResponseType

	/** Create a follow-up response. **/
	public suspend fun respond(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): FollowupType

	/**
	 * Create a follow-up response using the opposite interaction type.
	 *
	 * While Discord's API allows you to do this, it will rarely do what you'd expect.
	 * Only use this if you're sure it'll do what you want, and test thoroughly.
	 */
	@UnexpectedBehaviour
	public suspend fun respondOpposite(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): OppositeFollowupType
}

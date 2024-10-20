/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UnnecessaryAbstractClass")

package dev.kordex.core.types

import dev.kord.core.behavior.interaction.response.InteractionResponseBehavior
import dev.kord.core.entity.interaction.followup.FollowupMessage
import dev.kord.core.entity.interaction.response.MessageInteractionResponse
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import dev.kordex.core.annotations.AlwaysPublicResponse
import dev.kordex.core.annotations.UnexpectedFunctionBehaviour
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.pagination.BaseButtonPaginator
import dev.kordex.core.pagination.builders.PaginatorBuilder
import java.util.*

/** Type alias for a generic interaction context, meant to be used with extension functions. **/
public typealias GenericInteractionContext = InteractionContext<*, *, *, *>

/**
 * Interface representing an interaction context. Provides a generic base type for action contexts when working
 * with interactions.
 *
 * More specific types representing each interaction type (ephemeral/public) extend this class.
 *
 * If you're writing an extension function, you'll likely want to use [GenericInteractionContext] as the
 * receiver type.
 *
 * @param ResponseBehavior Generic representing the relevant interaction response behavior.
 * @param FollowupType Generic representing the follow-up type for the current interaction type.
 * @param OppositeFollowupType Generic representing the opposite follow-up type for the current interaction type.
 */
public interface InteractionContext<
	ResponseBehavior : InteractionResponseBehavior,
	ResponseType : MessageInteractionResponse,
	FollowupType : FollowupMessage,
	OppositeFollowupType : FollowupMessage,
	> {
	/** Current interaction response being worked with. **/
	public val interactionResponse: ResponseBehavior

	/** Create a paginator that edits the original interaction response. **/
	public fun editingPaginator(
		defaultGroup: Key = EMPTY_KEY,
		locale: Locale? = null,
		builder: (PaginatorBuilder).() -> Unit,
	): BaseButtonPaginator

	/**
	 * Create a paginator that creates a follow-up message, and edits that.
	 *
	 * This function always creates a public follow-up, as Discord prevents bots from editing ephemeral follow-ups.
	 */
	@AlwaysPublicResponse
	public suspend fun respondingPaginator(
		defaultGroup: Key = EMPTY_KEY,
		locale: Locale? = null,
		builder: suspend (PaginatorBuilder).() -> Unit,
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
	@UnexpectedFunctionBehaviour
	public suspend fun respondOpposite(
		builder: suspend FollowupMessageCreateBuilder.() -> Unit,
	): OppositeFollowupType
}

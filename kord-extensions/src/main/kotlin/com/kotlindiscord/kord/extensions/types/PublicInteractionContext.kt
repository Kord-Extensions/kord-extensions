package com.kotlindiscord.kord.extensions.types

import com.kotlindiscord.kord.extensions.pagination.PublicFollowUpPaginator
import com.kotlindiscord.kord.extensions.pagination.ResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.utils.ephemeralFollowup
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import java.util.*

/** Interface representing a public-only interaction action context. **/
public interface PublicInteractionContext {
    /** Response created by acknowledging the interaction publicly. **/
    public val interactionResponse: PublicInteractionResponseBehavior
}

/** Respond to the current interaction with a public followup. **/
public suspend inline fun PublicInteractionContext.respond(
    builder: FollowupMessageCreateBuilder.() -> Unit
): PublicFollowupMessage = interactionResponse.followUp(builder = builder)

/** Respond to the current interaction with an ephemeral followup. **/
public suspend inline fun PublicInteractionContext.respondEphemeral(
    builder: FollowupMessageCreateBuilder.() -> Unit
): EphemeralFollowupMessage = interactionResponse.ephemeralFollowup(builder)

/**
 * Edit the current interaction's response.
 */
public suspend inline fun PublicInteractionContext.edit(
    builder: InteractionResponseModifyBuilder.() -> Unit
): Unit = interactionResponse.edit(builder)

/** Create a paginator that edits the original interaction. **/
public suspend inline fun PublicInteractionContext.editingPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): ResponsePaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return ResponsePaginator(pages, interactionResponse)
}

/** Create a paginator that creates a follow-up message, and edits that. **/
public suspend inline fun PublicInteractionContext.respondingPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): PublicFollowUpPaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return PublicFollowUpPaginator(pages, interactionResponse)
}

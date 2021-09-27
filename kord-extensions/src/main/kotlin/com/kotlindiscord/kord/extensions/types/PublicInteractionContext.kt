package com.kotlindiscord.kord.extensions.types

import com.kotlindiscord.kord.extensions.pagination.PublicFollowUpPaginator
import com.kotlindiscord.kord.extensions.pagination.PublicResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.rest.builder.message.create.PublicFollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.PublicInteractionResponseModifyBuilder
import java.util.*

/** Interface representing a public-only interaction action context. **/
public interface PublicInteractionContext {
    /** Response created by acknowledging the interaction publicly. **/
    public val interactionResponse: PublicInteractionResponseBehavior
}

/** Respond to the current interaction with a public followup. **/
public suspend inline fun PublicInteractionContext.respond(
    builder: PublicFollowupMessageCreateBuilder.() -> Unit
): PublicFollowupMessage = interactionResponse.followUp(builder)

/**
 * Edit the current interaction's response.
 */
public suspend inline fun PublicInteractionContext.edit(
    builder: PublicInteractionResponseModifyBuilder.() -> Unit
): Message = interactionResponse.edit(builder)

/** Create a paginator that edits the original interaction. **/
public suspend inline fun PublicInteractionContext.editingPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): PublicResponsePaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return PublicResponsePaginator(pages, interactionResponse)
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

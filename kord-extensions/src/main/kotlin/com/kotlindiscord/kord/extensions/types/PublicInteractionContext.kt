/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.types

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

/** Interface representing a public-only interaction action context. **/
public interface PublicInteractionContext {
    /** Response created by acknowledging the interaction publicly. **/
    public val interactionResponse: PublicMessageInteractionResponseBehavior
}

/** Respond to the current interaction with a public followup. **/
public suspend inline fun PublicInteractionContext.respond(
    builder: FollowupMessageCreateBuilder.() -> Unit
): PublicFollowupMessage = interactionResponse.createPublicFollowup { builder() }

/** Respond to the current interaction with an ephemeral followup. **/
public suspend inline fun PublicInteractionContext.respondEphemeral(
    builder: FollowupMessageCreateBuilder.() -> Unit
): EphemeralFollowupMessage = interactionResponse.createEphemeralFollowup { builder() }

/**
 * Edit the current interaction's response.
 */
public suspend inline fun PublicInteractionContext.edit(
    builder: InteractionResponseModifyBuilder.() -> Unit
): PublicMessageInteractionResponse = interactionResponse.edit(builder)

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

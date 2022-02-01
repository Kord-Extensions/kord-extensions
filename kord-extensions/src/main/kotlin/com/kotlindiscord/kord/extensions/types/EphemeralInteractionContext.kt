/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.types

import com.kotlindiscord.kord.extensions.pagination.EphemeralResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.PublicFollowUpPaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import java.util.*

/** Interface representing an ephemeral-only interaction action context. **/
public interface EphemeralInteractionContext {
    /** Response created by acknowledging the interaction ephemerally. **/
    public val interactionResponse: EphemeralInteractionResponseBehavior
}

/**
 * Respond to the current interaction with an ephemeral followup.
 *
 * **Note:** Calling this twice (or at all after [edit]) will result in a public followup!
 */
public suspend inline fun EphemeralInteractionContext.respond(
    builder: FollowupMessageCreateBuilder.() -> Unit
): EphemeralFollowupMessage = interactionResponse.followUpEphemeral(builder)

/** Respond to the current interaction with a public followup. **/
public suspend inline fun PublicInteractionContext.respondPublic(
    builder: FollowupMessageCreateBuilder.() -> Unit
): PublicFollowupMessage = interactionResponse.followUp(builder)

/**
 * Edit the current interaction's response.
 */
public suspend inline fun EphemeralInteractionContext.edit(
    builder: InteractionResponseModifyBuilder.() -> Unit
): Message = interactionResponse.edit(builder)

/**
 * Create a paginator that edits the original interaction. This is the only option for an ephemeral interaction, as
 * it's impossible to edit an ephemeral follow-up.
 */
public suspend inline fun EphemeralInteractionContext.editingPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): EphemeralResponsePaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return EphemeralResponsePaginator(pages, interactionResponse)
}

/** Create a paginator that creates a follow-up message, and edits that. **/
public suspend inline fun EphemeralInteractionContext.publicRespondingPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): PublicFollowUpPaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return PublicFollowUpPaginator(pages, interactionResponse)
}

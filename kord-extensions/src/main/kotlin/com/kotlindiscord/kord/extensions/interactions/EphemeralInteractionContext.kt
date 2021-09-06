package com.kotlindiscord.kord.extensions.interactions

import com.kotlindiscord.kord.extensions.pagination.EphemeralResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.rest.builder.message.create.EphemeralFollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.EphemeralInteractionResponseModifyBuilder
import java.util.*

/** Interface representing an ephemeral-only application command context. **/
public interface EphemeralInteractionContext {
    /** Response created by acknowledging the interaction ephemerally. **/
    public val interactionResponse: EphemeralInteractionResponseBehavior
}

/**
 * Respond to the current interaction with an ephemeral followup.
 *
 * **Note:** Calling this twice will result in a public followup!
 */
public suspend inline fun EphemeralInteractionContext.respond(
    builder: EphemeralFollowupMessageCreateBuilder.() -> Unit
): EphemeralFollowupMessage = interactionResponse.followUpEphemeral(builder)

/**
 * Edit the current interaction's response.
 */
public suspend inline fun EphemeralInteractionContext.edit(
    builder: EphemeralInteractionResponseModifyBuilder.() -> Unit
): Unit = interactionResponse.edit(builder)

public suspend inline fun EphemeralInteractionContext.editingPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): EphemeralResponsePaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return EphemeralResponsePaginator(pages, interactionResponse)
}

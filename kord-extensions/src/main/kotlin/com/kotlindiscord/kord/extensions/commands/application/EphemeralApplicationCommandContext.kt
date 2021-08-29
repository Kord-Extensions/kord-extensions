package com.kotlindiscord.kord.extensions.commands.application

import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.rest.builder.message.create.EphemeralFollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.EphemeralInteractionResponseModifyBuilder

/** Interface representing an ephemeral-only application command context. **/
public interface EphemeralApplicationCommandContext {
    /** Response created by acknowledging the interaction ephemerally. **/
    public val interactionResponse: EphemeralInteractionResponseBehavior
}

/**
 * Respond to the current interaction with an ephemeral followup.
 *
 * **Note:** Calling this twice will result in a public followup!
 */
public suspend inline fun EphemeralApplicationCommandContext.respond(
    builder: EphemeralFollowupMessageCreateBuilder.() -> Unit
): EphemeralFollowupMessage = interactionResponse.followUpEphemeral(builder)

/**
 * Edit the current interaction's response, if one was sent via `initialResponse`.
 */
public suspend inline fun EphemeralApplicationCommandContext.edit(
    builder: EphemeralInteractionResponseModifyBuilder.() -> Unit
): Unit? = (interactionResponse as? EphemeralInteractionResponseBehavior)?.edit(builder)

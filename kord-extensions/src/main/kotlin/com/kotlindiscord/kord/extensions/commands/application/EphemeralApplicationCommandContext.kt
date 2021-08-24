package com.kotlindiscord.kord.extensions.commands.application

import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUpEphemeral
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.rest.builder.message.create.EphemeralFollowupMessageCreateBuilder

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

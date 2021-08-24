package com.kotlindiscord.kord.extensions.commands.application

import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.rest.builder.message.create.PublicFollowupMessageCreateBuilder

/** Interface representing a public-only application command context. **/
public interface PublicApplicationCommandContext {
    /** Response created by acknowledging the interaction publicly. **/
    public val interactionResponse: PublicInteractionResponseBehavior
}

/** Respond to the current interaction with a public followup. **/
public suspend inline fun PublicApplicationCommandContext.respond(
    builder: PublicFollowupMessageCreateBuilder.() -> Unit
): PublicFollowupMessage = interactionResponse.followUp(builder)

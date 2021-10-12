package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Ephemeral-only message command context. **/
public class EphemeralMessageCommandContext(
    override val event: MessageCommandInteractionCreateEvent,
    override val command: MessageCommand<EphemeralMessageCommandContext>,
    override val interactionResponse: EphemeralInteractionResponseBehavior
) : MessageCommandContext<EphemeralMessageCommandContext>(event, command), EphemeralInteractionContext

package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.commands.application.EphemeralApplicationCommandContext
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Ephemeral-only message command context. **/
public class EphemeralMessageCommandContext(
    override val event: MessageCommandInteractionCreateEvent,
    override val command: MessageCommand<MessageCommandContext>,
    override val interactionResponse: EphemeralInteractionResponseBehavior
) : MessageCommandContext(event, command), EphemeralApplicationCommandContext

package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/** Ephemeral-only user command context. **/
public class EphemeralUserCommandContext(
    override val event: UserCommandInteractionCreateEvent,
    override val command: UserCommand<EphemeralUserCommandContext>,
    override val interactionResponse: EphemeralInteractionResponseBehavior
) : UserCommandContext<EphemeralUserCommandContext>(event, command), EphemeralInteractionContext

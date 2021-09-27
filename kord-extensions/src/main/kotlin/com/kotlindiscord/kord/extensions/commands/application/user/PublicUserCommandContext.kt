package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/** Public-only user command context. **/
public class PublicUserCommandContext(
    override val event: UserCommandInteractionCreateEvent,
    override val command: UserCommand<PublicUserCommandContext>,
    override val interactionResponse: PublicInteractionResponseBehavior
) : UserCommandContext<PublicUserCommandContext>(event, command), PublicInteractionContext

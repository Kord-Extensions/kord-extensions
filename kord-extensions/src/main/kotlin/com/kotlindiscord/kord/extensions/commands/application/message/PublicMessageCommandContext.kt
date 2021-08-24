package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.commands.application.PublicApplicationCommandContext
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Public-only message command context. **/
public class PublicMessageCommandContext(
    override val event: MessageCommandInteractionCreateEvent,
    override val command: MessageCommand<MessageCommandContext>,
    override val interactionResponse: PublicInteractionResponseBehavior
) : MessageCommandContext(event, command), PublicApplicationCommandContext

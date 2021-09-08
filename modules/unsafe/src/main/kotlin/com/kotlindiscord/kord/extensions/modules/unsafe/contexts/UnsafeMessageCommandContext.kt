package com.kotlindiscord.kord.extensions.modules.unsafe.contexts

import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommandContext
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.types.UnsafeInteractionContext
import dev.kord.core.behavior.interaction.InteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Command context for an unsafe message command. **/
@UnsafeAPI
public class UnsafeMessageCommandContext(
    override val event: MessageCommandInteractionCreateEvent,
    override val command: MessageCommand<UnsafeMessageCommandContext>,
    override val interactionResponse: InteractionResponseBehavior,
) : MessageCommandContext<UnsafeMessageCommandContext>(event, command), UnsafeInteractionContext

package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/**
 *  Message command context, containing everything you need for your message command's execution.
 *
 *  @param event Event that triggered this message command.
 *  @param command Message command instance.
 */
public open class MessageCommandContext(
    public open val event: MessageCommandInteractionCreateEvent,
    public open val command: MessageCommand<MessageCommandContext>,
) : ApplicationCommandContext(event, command) {
    /** Messages that this message command is being executed against. **/
    public val targetMessages: Collection<Message> = event.interaction.messages?.values ?: listOf()
}

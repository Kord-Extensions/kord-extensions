package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Message context command, for right-click actions on messages. **/
public class MessageCommand(
    extension: Extension
) : ApplicationCommand<MessageCommandInteractionCreateEvent>(extension) {
    override suspend fun call(event: MessageCommandInteractionCreateEvent) {
        TODO("Not yet implemented")
    }
}

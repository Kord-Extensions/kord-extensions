package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/** User context command, for right-click actions on users. **/
public class UserCommand(
    extension: Extension
) : ApplicationCommand<UserCommandInteractionCreateEvent>(extension) {
    override suspend fun call(event: UserCommandInteractionCreateEvent) {
        TODO("Not yet implemented")
    }
}

package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Message context command, for right-click actions on messages. **/
public open class MessageCommand<C : MessageCommandContext>(
    extension: Extension
) : ApplicationCommand<MessageCommandInteractionCreateEvent>(extension) {
    /** Command body, to be called when the command is executed. **/
    public lateinit var body: suspend C.() -> Unit

    /** Call this to supply a command [body], to be called when the command is executed. **/
    public fun action(action: suspend C.() -> Unit) {
        body = action
    }

    override suspend fun call(event: MessageCommandInteractionCreateEvent) {
        TODO("Not yet implemented")
    }
}

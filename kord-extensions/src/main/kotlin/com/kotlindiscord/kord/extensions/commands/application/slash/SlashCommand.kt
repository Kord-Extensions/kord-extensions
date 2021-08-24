package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

/** Slash command, executed directly in the chat input. **/
public abstract class SlashCommand(
    extension: Extension
) : ApplicationCommand<ChatInputCommandInteractionCreateEvent>(extension) {
    /** Command description, to explain what your command does. **/
    public lateinit var description: String

    override fun validate() {
        super.validate()

        if (::description.isInitialized.not() || description.isEmpty()) {
            error("Slash command description must be provided.")
        }
    }

    override suspend fun runChecks(event: ChatInputCommandInteractionCreateEvent): Boolean =
        super.runChecks(event)

    override suspend fun call(event: ChatInputCommandInteractionCreateEvent) {
        TODO("Not yet implemented")
    }
}

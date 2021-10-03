@file:Suppress(
    "UNCHECKED_CAST"
)

package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.registry.RegistryStorage
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/**
 * ApplicationCommandRegistry which acts based off a specified storage interface.
 *
 * Discord lifecycles may not be implemented in this class and require manual updating.
 */
public class StorageAwareApplicationCommandRegistry(
    builder: () -> RegistryStorage<Snowflake, out ApplicationCommand<*>>
) : ApplicationCommandRegistry() {

    private val slashCommandStorage: RegistryStorage<Snowflake, SlashCommand<*, *>> =
        builder as RegistryStorage<Snowflake, SlashCommand<*, *>>

    private val messageCommandStorage: RegistryStorage<Snowflake, MessageCommand<*>> =
        builder as RegistryStorage<Snowflake, MessageCommand<*>>

    private val userCommandStorage: RegistryStorage<Snowflake, UserCommand<*>> =
        builder as RegistryStorage<Snowflake, UserCommand<*>>

    override suspend fun initialize(commands: List<ApplicationCommand<*>>) {
        commands.forEach {
            when (it) {
                is SlashCommand<*, *> -> slashCommandStorage.register(it)
                is MessageCommand<*> -> messageCommandStorage.register(it)
                is UserCommand<*> -> userCommandStorage.register(it)
            }
        }

        // check unknown & sync
    }

    override suspend fun register(command: SlashCommand<*, *>): SlashCommand<*, *> {
        slashCommandStorage.register(command)
        return command
    }

    override suspend fun register(command: MessageCommand<*>): MessageCommand<*> {
        messageCommandStorage.register(command)
        return command
    }

    override suspend fun register(command: UserCommand<*>): UserCommand<*> {
        userCommandStorage.register(command)
        return command
    }

    override suspend fun handle(event: ChatInputCommandInteractionCreateEvent) {
        val commandId = event.interaction.invokedCommandId
        val command = slashCommandStorage.get(commandId)

        command ?: return logger.warn { "Received interaction for unknown slash command: ${commandId.asString}" }

        command.call(event)
    }

    override suspend fun handle(event: MessageCommandInteractionCreateEvent) {
        val commandId = event.interaction.invokedCommandId
        val command = messageCommandStorage.get(commandId)

        command ?: return logger.warn { "Received interaction for unknown message command: ${commandId.asString}" }

        command.call(event)
    }

    override suspend fun handle(event: UserCommandInteractionCreateEvent) {
        val commandId = event.interaction.invokedCommandId
        val command = userCommandStorage.get(commandId)

        command ?: return logger.warn { "Received interaction for unknown user command: ${commandId.asString}" }

        command.call(event)
    }

    @Suppress("StringLiteralDuplication")
    override suspend fun unregister(command: SlashCommand<*, *>, delete: Boolean): SlashCommand<*, *>? {
        TODO("Not yet implemented")
    }

    @Suppress("StringLiteralDuplication")
    override suspend fun unregister(command: MessageCommand<*>, delete: Boolean): MessageCommand<*>? {
        TODO("Not yet implemented")
    }

    @Suppress("StringLiteralDuplication")
    override suspend fun unregister(command: UserCommand<*>, delete: Boolean): UserCommand<*>? {
        TODO("Not yet implemented")
    }
}

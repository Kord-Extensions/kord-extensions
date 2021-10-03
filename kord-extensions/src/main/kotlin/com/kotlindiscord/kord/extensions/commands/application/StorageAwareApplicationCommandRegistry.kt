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
import kotlinx.coroutines.flow.firstOrNull

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

    override suspend fun register(command: SlashCommand<*, *>): SlashCommand<*, *>? {
        val commandId = createDiscordCommand(command) ?: return null

        slashCommandStorage.set(commandId, command)

        return command
    }

    override suspend fun register(command: MessageCommand<*>): MessageCommand<*>? {
        val commandId = createDiscordCommand(command) ?: return null

        messageCommandStorage.set(commandId, command)

        return command
    }

    override suspend fun register(command: UserCommand<*>): UserCommand<*>? {
        val commandId = createDiscordCommand(command) ?: return null

        userCommandStorage.set(commandId, command)

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

    override suspend fun unregister(command: SlashCommand<*, *>, delete: Boolean): SlashCommand<*, *>? =
        unregisterGeneric(slashCommandStorage, command, delete)

    override suspend fun unregister(command: MessageCommand<*>, delete: Boolean): MessageCommand<*>? =
        unregisterGeneric(messageCommandStorage, command, delete)

    override suspend fun unregister(command: UserCommand<*>, delete: Boolean): UserCommand<*>? =
        unregisterGeneric(userCommandStorage, command, delete)

    private suspend fun <T : ApplicationCommand<*>> unregisterGeneric(
        registry: RegistryStorage<Snowflake, T>,
        command: T,
        delete: Boolean
    ): T? {
        val id = registry.constructUniqueIdentifier(command)
        val snowflake = registry.entryFlow()
            .firstOrNull { registry.constructUniqueIdentifier(it.value) == id }
            ?.key

        snowflake?.let {
            if (delete) {
                deleteGeneric(command, it)
            }

            return registry.remove(it)
        }

        return null
    }
}

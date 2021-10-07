@file:Suppress(
    "TooGenericExceptionCaught",
    "StringLiteralDuplication",
    "AnnotationSpacing",
    "SpacingBetweenAnnotations"
)

package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.createApplicationCommands
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.rest.json.JsonErrorCode
import dev.kord.rest.request.KtorRequestException
import kotlinx.coroutines.flow.toList

/** Registry for all Discord application commands. **/
public open class DefaultApplicationCommandRegistry : ApplicationCommandRegistry() {

    /** Mapping of Discord-side command ID to a message command object. **/
    public open val messageCommands: MutableMap<Snowflake, MessageCommand<*>> = mutableMapOf()

    /** Mapping of Discord-side command ID to a slash command object. **/
    public open val slashCommands: MutableMap<Snowflake, SlashCommand<*, *>> = mutableMapOf()

    /** Mapping of Discord-side command ID to a user command object. **/
    public open val userCommands: MutableMap<Snowflake, UserCommand<*>> = mutableMapOf()

    public override suspend fun initialize(commands: List<ApplicationCommand<*>>) {
        if (!bot.settings.applicationCommandsBuilder.register) {
            logger.debug {
                "Application command registration is disabled, pairing existing commands with extension commands"
            }
        }

        try {
            syncAll(true, commands)
        } catch (t: Throwable) {
            logger.error(t) { "Failed to synchronise application commands" }
        }
    }

    // region: Untyped sync functions

    /** Register multiple generic application commands. **/
    public open suspend fun syncAll(removeOthers: Boolean = false, commands: List<ApplicationCommand<*>>) {
        val groupedCommands = commands.groupBy { it.guildId }.toMutableMap()

        if (removeOthers && !groupedCommands.containsKey(null)) {
            groupedCommands[null] = listOf()
        }

        groupedCommands.forEach {
            try {
                sync(removeOthers, it.key, it.value)
            } catch (e: KtorRequestException) {
                logger.error(e) {
                    var message = if (it.key == null) {
                        "Failed to synchronise global application commands"
                    } else {
                        "Failed to synchronise application commands for guild with ID: ${it.key!!.asString}"
                    }

                    if (e.error?.message != null) {
                        message += "\n        Discord error message: ${e.error?.message}"
                    }

                    if (e.error?.code == JsonErrorCode.MissingAccess) {
                        message += "\n        Double-check that the bot was added to this guild with the " +
                            "`application.commands` scope enabled"
                    }

                    message
                }
            } catch (t: Throwable) {
                logger.error(t) {
                    if (it.key == null) {
                        "Failed to synchronise global application commands"
                    } else {
                        "Failed to synchronise application commands for guild with ID: ${it.key!!.asString}"
                    }
                }
            }
        }

        val commandsWithPerms = (messageCommands + slashCommands + userCommands)
            .filterValues {
                it.allowedRoles.isNotEmpty() ||
                    it.allowedUsers.isNotEmpty() ||
                    it.disallowedRoles.isNotEmpty() ||
                    it.disallowedUsers.isNotEmpty() ||
                    !it.allowByDefault
            }
            .toList()
            .groupBy { it.second.guildId }

        try {
            commandsWithPerms.forEach { (guildId, commands) ->
                if (guildId != null) {
                    kord.bulkEditApplicationCommandPermissions(guildId) {
                        commands.forEach { (id, commandObj) ->
                            command(id) { injectRawPermissions(this, commandObj) }
                        }
                    }

                    logger.trace { "Applied permissions for ${commands.size} commands." }
                } else {
                    logger.warn { "Applying permissions to global application commands is currently not supported." }
                }
            }
        } catch (e: KtorRequestException) {
            logger.error(e) {
                "Failed to apply application command permissions - for this reason, all commands with configured" +
                    "permissions will be disabled." +
                    if (e.error?.message != null) {
                        "\n        Discord error message: ${e.error?.message}"
                    } else {
                        ""
                    }
            }
        } catch (t: Throwable) {
            logger.error(t) {
                "Failed to apply application command permissions - for this reason, all commands with configured" +
                    "permissions will be disabled."
            }

            commandsWithPerms.forEach { (_, commands) ->
                commands.forEach { (id, _) ->
                    messageCommands.remove(id)
                    slashCommands.remove(id)
                    userCommands.remove(id)
                }
            }
        }
    }

    /** Register multiple generic application commands. **/
    public open suspend fun sync(
        removeOthers: Boolean = false,
        guildId: Snowflake?,
        commands: List<ApplicationCommand<*>>
    ) {
        // NOTE: Someday, discord will make real i18n possible, we hope...
        val locale = bot.settings.i18nBuilder.defaultLocale

        val guild = if (guildId != null) {
            kord.getGuild(guildId)
                ?: return logger.debug {
                    "Cannot register application commands for guild ID ${guildId.asString}, " +
                        "as it seems to be missing."
                }
        } else {
            null
        }

        // Get guild commands if we're registering them (guild != null), otherwise get global commands
        val registered = guild?.commands?.toList()
            ?: kord.globalCommands.toList()

        if (!bot.settings.applicationCommandsBuilder.register) {
            commands.forEach { commandObj ->
                val existingCommand = registered.firstOrNull { commandObj.matches(locale, it) }

                if (existingCommand != null) {
                    when (commandObj) {
                        is MessageCommand<*> -> messageCommands[existingCommand.id] = commandObj
                        is SlashCommand<*, *> -> slashCommands[existingCommand.id] = commandObj
                        is UserCommand<*> -> userCommands[existingCommand.id] = commandObj
                    }
                }
            }

            return  // We're only syncing them up, there's no other API work to do
        }

        // Extension commands that haven't been registered yet
        val toAdd = commands.filter { aC -> registered.all { dC -> !aC.matches(locale, dC) } }

        // Extension commands that were previously registered
        val toUpdate = commands.filter { aC -> registered.any { dC -> aC.matches(locale, dC) } }

        // Registered Discord commands that haven't been provided by extensions
        val toRemove = if (removeOthers) {
            registered.filter { dC -> commands.all { aC -> !aC.matches(locale, dC) } }
        } else {
            listOf()
        }

        logger.info {
            var message = if (guild == null) {
                "Global application commands: ${toAdd.size} to add / " +
                    "${toUpdate.size} to update / " +
                    "${toRemove.size} to remove"
            } else {
                "Application commands for guild ${guild.name}: ${toAdd.size} to add / " +
                    "${toUpdate.size} to update / " +
                    "${toRemove.size} to remove"
            }

            if (!removeOthers) {
                message += "\nThe `removeOthers` parameter is `false`, so no commands will be removed."
            }

            message
        }

        val toCreate = toAdd + toUpdate

        @Suppress("IfThenToElvis")  // Ultimately, this is far more readable
        val response = if (guild == null) {
            // We're registering global commands here, if the guild is null

            kord.createGlobalApplicationCommands {
                toCreate.forEach {
                    val name = it.getTranslatedName(locale)

                    logger.trace { "Adding/updating global ${it.type.name} command: $name" }

                    when (it) {
                        is MessageCommand<*> -> message(name) { this.register(locale, it) }
                        is UserCommand<*> -> user(name) { this.register(locale, it) }

                        is SlashCommand<*, *> -> input(
                            name, it.getTranslatedDescription(locale)
                        ) { this.register(locale, it) }
                    }
                }
            }.toList()
        } else {
            // We're registering guild-specific commands here, if the guild is available

            guild.createApplicationCommands {
                toCreate.forEach {
                    val name = it.getTranslatedName(locale)

                    logger.trace { "Adding/updating guild-specific ${it.type.name} command: $name" }

                    when (it) {
                        is MessageCommand<*> -> message(name) { this.register(locale, it) }
                        is UserCommand<*> -> user(name) { this.register(locale, it) }

                        is SlashCommand<*, *> -> input(
                            name, it.getTranslatedDescription(locale)
                        ) { this.register(locale, it) }
                    }
                }
            }.toList()
        }

        // Next, we need to associate all the commands we just registered with the commands in our extensions
        toCreate.forEach { command ->
            val match = response.first { command.matches(locale, it) }

            when (command) {
                is MessageCommand<*> -> messageCommands[match.id] = command
                is SlashCommand<*, *> -> slashCommands[match.id] = command
                is UserCommand<*> -> userCommands[match.id] = command
            }
        }

        // Finally, we can remove anything that needs to be removed
        toRemove.forEach {
            logger.trace { "Removing ${it.type.name} command: ${it.name}" }
            it.delete()
        }

        logger.info {
            if (guild == null) {
                "Finished synchronising global application commands"
            } else {
                "Finished synchronising application commands for guild ${guild.name}"
            }
        }
    }

    // endregion

    // region: Typed registration functions

    /** Register a message command. **/
    public override suspend fun register(command: MessageCommand<*>): MessageCommand<*>? {
        val commandId = createDiscordCommand(command) ?: return null

        messageCommands[commandId] = command

        return command
    }

    /** Register a slash command. **/
    public override suspend fun register(command: SlashCommand<*, *>): SlashCommand<*, *>? {
        val commandId = createDiscordCommand(command) ?: return null

        slashCommands[commandId] = command

        return command
    }

    /** Register a user command. **/
    public override suspend fun register(command: UserCommand<*>): UserCommand<*>? {
        val commandId = createDiscordCommand(command) ?: return null

        userCommands[commandId] = command

        return command
    }

    // endregion

    // region: Unregistration functions

    /** Unregister a message command. **/
    public override suspend fun unregister(command: MessageCommand<*>, delete: Boolean): MessageCommand<*>? {
        val filtered = messageCommands.filter { it.value == command }
        val id = filtered.keys.firstOrNull() ?: return null

        if (delete) {
            deleteGeneric(command, id)
        }

        return messageCommands.remove(id)
    }

    /** Unregister a slash command. **/
    public override suspend fun unregister(command: SlashCommand<*, *>, delete: Boolean): SlashCommand<*, *>? {
        val filtered = slashCommands.filter { it.value == command }
        val id = filtered.keys.firstOrNull() ?: return null

        if (delete) {
            deleteGeneric(command, id)
        }

        return slashCommands.remove(id)
    }

    /** Unregister a user command. **/
    public override suspend fun unregister(command: UserCommand<*>, delete: Boolean): UserCommand<*>? {
        val filtered = userCommands.filter { it.value == command }
        val id = filtered.keys.firstOrNull() ?: return null

        if (delete) {
            deleteGeneric(command, id)
        }

        return userCommands.remove(id)
    }

    // endregion

    // region: Event handlers

    /** Event handler for message commands. **/
    public override suspend fun handle(event: MessageCommandInteractionCreateEvent) {
        val commandId = event.interaction.invokedCommandId
        val command = messageCommands[commandId]

        command ?: return logger.warn { "Received interaction for unknown message command: ${commandId.asString}" }

        command.call(event)
    }

    /** Event handler for slash commands. **/
    public override suspend fun handle(event: ChatInputCommandInteractionCreateEvent) {
        val commandId = event.interaction.command.rootId
        val command = slashCommands[commandId]

        command ?: return logger.warn { "Received interaction for unknown slash command: ${commandId.asString}" }

        command.call(event)
    }

    /** Event handler for user commands. **/
    public override suspend fun handle(event: UserCommandInteractionCreateEvent) {
        val commandId = event.interaction.invokedCommandId
        val command = userCommands[commandId]

        command ?: return logger.warn { "Received interaction for unknown user command: ${commandId.asString}" }

        command.call(event)
    }

    // endregion
}

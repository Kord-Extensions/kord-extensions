@file:Suppress(
    "TooGenericExceptionCaught",
    "StringLiteralDuplication",
    "AnnotationSpacing",
    "SpacingBetweenAnnotations"
)
@file:OptIn(ExperimentalCoroutinesApi::class, KordUnsafe::class, KordExperimental::class)

package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandParser
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.commands.converters.SlashCommandConverter
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.registry.DefaultLocalRegistryStorage
import com.kotlindiscord.kord.extensions.registry.RegistryStorage
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.createApplicationCommands
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.createMessageCommand
import dev.kord.core.behavior.createUserCommand
import dev.kord.core.entity.application.GuildApplicationCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.*
import dev.kord.rest.json.JsonErrorCode
import dev.kord.rest.request.KtorRequestException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/** Registry for all Discord application commands. **/
public open class ApplicationCommandRegistry : KoinComponent {
    private val logger = KotlinLogging.logger { }

    /** Current instance of the bot. **/
    public open val bot: ExtensibleBot by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public open val kord: Kord by inject()

    /** Command parser to use for slash commands. **/
    public open val argumentParser: SlashCommandParser = SlashCommandParser()

    /** Translations provider, for retrieving translations. **/
    public open val translationsProvider: TranslationsProvider by inject()

    /** Mapping of Discord-side command ID to a message command object. **/
    public open val messageCommands: RegistryStorage<Snowflake, MessageCommand<*>> = DefaultLocalRegistryStorage()

    /** Mapping of Discord-side command ID to a slash command object. **/
    public open val slashCommands: RegistryStorage<Snowflake, SlashCommand<*, *>> = DefaultLocalRegistryStorage()

    /** Mapping of Discord-side command ID to a user command object. **/
    public open val userCommands: RegistryStorage<Snowflake, UserCommand<*>> = DefaultLocalRegistryStorage()

    /** Whether the initial sync has been finished, and commands should be registered directly. **/
    public open var initialised: Boolean = false

    /** Quick access to the human-readable name for a Discord application command type. **/
    public val ApplicationCommandType.name: String
        get() = when (this) {
            is ApplicationCommandType.Unknown -> "unknown"

            ApplicationCommandType.ChatInput -> "slash"
            ApplicationCommandType.Message -> "message"
            ApplicationCommandType.User -> "user"
        }

    /** Handles the initial registration of commands, after extensions have been loaded. **/
    public open suspend fun initialRegistration() {
        if (initialised) {
            return
        }

        if (!bot.settings.applicationCommandsBuilder.register) {
            logger.debug {
                "Application command registration is disabled, pairing existing commands with extension commands"
            }
        }

        val commands: MutableList<ApplicationCommand<*>> = mutableListOf()

        bot.extensions.values.forEach {
            commands += it.messageCommands
            commands += it.slashCommands
            commands += it.userCommands
        }

        try {
            syncAll(true, commands)
        } catch (t: Throwable) {
            logger.error(t) { "Failed to synchronise application commands" }
        }

        initialised = true
    }

    // region: Untyped sync functions

    /** Register multiple generic application commands. **/
    public open suspend fun syncAll(removeOthers: Boolean = false, commands: List<ApplicationCommand<*>>) {
        // Make registry know each command
        commands.forEach {
            when (it) {
                is MessageCommand<*> -> messageCommands.register(it)
                is UserCommand<*> -> userCommands.register(it)
                is SlashCommand<*, *> -> slashCommands.register(it)
            }
        }

        val knownCommands = merge(messageCommands.entryFlow(), slashCommands.entryFlow(), userCommands.entryFlow())
            .toList()

        val groupedCommands = commands.groupBy { it.guildId }

        groupedCommands.forEach {
            try {
                sync(removeOthers, it.key, knownCommands, it.value)
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

        val commandsWithPerms = merge(messageCommands.entryFlow(), slashCommands.entryFlow(), userCommands.entryFlow())
            .filter {
                it.value.allowedRoles.isNotEmpty() ||
                    it.value.allowedUsers.isNotEmpty() ||
                    it.value.disallowedRoles.isNotEmpty() ||
                    it.value.disallowedUsers.isNotEmpty() ||
                    !it.value.allowByDefault
            }
            .toList()
            .groupBy { it.value.guildId }

        try {
            commandsWithPerms.forEach { (guildId, commands) ->
                if (guildId != null) {
                    kord.bulkEditApplicationCommandPermissions(kord.resources.applicationId, guildId) {
                        commands.forEach { (id, commandObj) ->
                            command(id) {
                                commandObj.allowedUsers.map { user(it, true) }
                                commandObj.disallowedUsers.map { user(it, false) }

                                commandObj.allowedRoles.map { role(it, true) }
                                commandObj.disallowedRoles.map { role(it, false) }
                            }
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
        knownCommands: List<RegistryStorage.StorageEntry<Snowflake, out ApplicationCommand<*>>>,
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

        if (!bot.settings.applicationCommandsBuilder.register) {
            val registered = guild?.commands?.toList()
                ?: kord.globalCommands.toList()
            commands.forEach { commandObj ->
                val existingCommand = registered.firstOrNull { commandObj.matches(locale, it) }

                if (existingCommand != null) {
                    when (commandObj) {
                        is MessageCommand<*> -> messageCommands.set(existingCommand.id, commandObj)
                        is SlashCommand<*, *> -> slashCommands.set(existingCommand.id, commandObj)
                        is UserCommand<*> -> userCommands.set(existingCommand.id, commandObj)
                    }
                }
            }

            return  // We're only syncing them up, there's no other API work to do
        }

        // Extension commands that haven't been registered yet
        val toAdd = commands.filter { c -> knownCommands.all { !c.matches(locale, it.value) } }

        // Extension commands that were previously registered
        val toUpdate = commands.filter { c -> knownCommands.any { c.matches(locale, it.value) } }

        // Registered Discord commands that haven't been provided by extensions
        val toRemove = if (removeOthers) {
            knownCommands.filter { c -> commands.all { !it.matches(locale, c.value) } }
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
                is MessageCommand<*> -> messageCommands.set(match.id, command)
                is SlashCommand<*, *> -> slashCommands.set(match.id, command)
                is UserCommand<*> -> userCommands.set(match.id, command)
            }
        }

        // Finally, we can remove anything that needs to be removed
        toRemove.forEach {
            logger.trace { "Removing ${it.value.type.name} command: ${it.value.name}" }
            when (guildId) {
                null -> kord.unsafe.globalApplicationCommand(kord.selfId, it.key)
                else -> kord.unsafe.guildApplicationCommand(guildId, kord.selfId, it.key)
            }.delete()
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

    // region: Typed batch registration functions

    /** Register multiple message commands. **/
    public open suspend fun registerAll(vararg commands: MessageCommand<*>): List<MessageCommand<*>> =
        commands.map {
            try {
                register(it) as MessageCommand<*>
            } catch (e: KtorRequestException) {
                logger.warn(e) {
                    "Failed to register ${it.type.name} command: ${it.name}" +
                        if (e.error?.message != null) {
                            "\n        Discord error message: ${e.error?.message}"
                        } else {
                            ""
                        }
                }

                null
            } catch (t: Throwable) {
                logger.warn(t) { "Failed to register ${it.type.name} command: ${it.name}" }

                null
            }
        }.filterNotNull()

    /** Register multiple slash commands. **/
    public open suspend fun registerAll(vararg commands: SlashCommand<*, *>): List<SlashCommand<*, *>> =
        commands.map {
            try {
                register(it) as SlashCommand<*, *>
            } catch (e: KtorRequestException) {
                logger.warn(e) {
                    "Failed to register ${it.type.name} command: ${it.name}" +
                        if (e.error?.message != null) {
                            "\n        Discord error message: ${e.error?.message}"
                        } else {
                            ""
                        }
                }

                null
            } catch (t: Throwable) {
                logger.warn(t) { "Failed to register ${it.type.name} command: ${it.name}" }

                null
            }
        }.filterNotNull()

    /** Register multiple user commands. **/
    public open suspend fun registerAll(vararg commands: UserCommand<*>): List<UserCommand<*>> =
        commands.map {
            try {
                register(it) as UserCommand<*>
            } catch (e: KtorRequestException) {
                logger.warn(e) {
                    "Failed to register ${it.type.name} command: ${it.name}" +
                        if (e.error?.message != null) {
                            "\n        Discord error message: ${e.error?.message}"
                        } else {
                            ""
                        }
                }

                null
            } catch (t: Throwable) {
                logger.warn(t) { "Failed to register ${it.type.name} command: ${it.name}" }

                null
            }
        }.filterNotNull()

    // endregion

    // region: Typed registration functions

    /** Register a message command. **/
    public open suspend fun register(command: MessageCommand<*>): MessageCommand<*>? {
        val locale = bot.settings.i18nBuilder.defaultLocale

        val guild = if (command.guildId != null) {
            kord.getGuild(command.guildId!!)
        } else {
            null
        }

        val name = command.getTranslatedName(locale)

        val response = if (guild == null) {
            // We're registering global commands here, if the guild is null

            kord.createGlobalMessageCommand(name) {
                logger.trace { "Adding/updating global ${command.type.name} command: $name" }

                this.register(locale, command)
            }
        } else {
            // We're registering guild-specific commands here, if the guild is available

            guild.createMessageCommand(name) {
                logger.trace { "Adding/updating guild-specific ${command.type.name} command: $name" }

                this.register(locale, command)
            }
        }

        try {
            if (guild != null) {
                kord.editApplicationCommandPermissions(kord.resources.applicationId, guild.id, response.id) {
                    command.allowedUsers.map { user(it, true) }
                    command.disallowedUsers.map { user(it, false) }

                    command.allowedRoles.map { role(it, true) }
                    command.disallowedRoles.map { role(it, false) }
                }

                logger.trace { "Applied permissions for command: ${command.name} ($command)" }
            } else {
                logger.warn { "Applying permissions to global application commands is currently not supported." }
            }
        } catch (e: KtorRequestException) {
            logger.error(e) {
                "Failed to apply application command permissions. This command will not be registered." +
                    if (e.error?.message != null) {
                        "\n        Discord error message: ${e.error?.message}"
                    } else {
                        ""
                    }
            }
        } catch (t: Throwable) {
            logger.error(t) {
                "Failed to apply application command permissions. This command will not be registered."
            }

            return null
        }

        messageCommands.set(response.id, command)

        return command
    }

    /** Register a slash command. **/
    public open suspend fun register(command: SlashCommand<*, *>): SlashCommand<*, *>? {
        val locale = bot.settings.i18nBuilder.defaultLocale

        val guild = if (command.guildId != null) {
            kord.getGuild(command.guildId!!)
        } else {
            null
        }

        val name = command.getTranslatedName(locale)
        val description = command.getTranslatedDescription(locale)

        val response = if (guild == null) {
            // We're registering global commands here, if the guild is null

            kord.createGlobalChatInputCommand(name, description) {
                logger.trace { "Adding/updating global ${command.type.name} command: $name" }

                this.register(locale, command)
            }
        } else {
            // We're registering guild-specific commands here, if the guild is available

            guild.createChatInputCommand(name, description) {
                logger.trace { "Adding/updating guild-specific ${command.type.name} command: $name" }

                this.register(locale, command)
            }
        }

        try {
            if (guild != null) {
                kord.editApplicationCommandPermissions(kord.resources.applicationId, guild.id, response.id) {
                    command.allowedUsers.map { user(it, true) }
                    command.disallowedUsers.map { user(it, false) }

                    command.allowedRoles.map { role(it, true) }
                    command.disallowedRoles.map { role(it, false) }
                }

                logger.trace { "Applied permissions for command: ${command.name} ($command)" }
            } else {
                logger.warn { "Applying permissions to global application commands is currently not supported." }
            }
        } catch (e: KtorRequestException) {
            logger.error(e) {
                "Failed to apply application command permissions. This command will not be registered." +
                    if (e.error?.message != null) {
                        "\n        Discord error message: ${e.error?.message}"
                    } else {
                        ""
                    }
            }
        } catch (t: Throwable) {
            logger.error(t) {
                "Failed to apply application command permissions. This command will not be registered."
            }

            return null
        }

        slashCommands.set(response.id, command)

        return command
    }

    /** Register a user command. **/
    public open suspend fun register(command: UserCommand<*>): UserCommand<*>? {
        val locale = bot.settings.i18nBuilder.defaultLocale

        val guild = if (command.guildId != null) {
            kord.getGuild(command.guildId!!)
        } else {
            null
        }

        val name = command.getTranslatedName(locale)

        val response = if (guild == null) {
            // We're registering global commands here, if the guild is null

            kord.createGlobalUserCommand(name) {
                logger.trace { "Adding/updating global ${command.type.name} command: $name" }

                this.register(locale, command)
            }
        } else {
            // We're registering guild-specific commands here, if the guild is available

            guild.createUserCommand(name) {
                logger.trace { "Adding/updating guild-specific ${command.type.name} command: $name" }

                this.register(locale, command)
            }
        }

        try {
            if (guild != null) {
                kord.editApplicationCommandPermissions(kord.resources.applicationId, guild.id, response.id) {
                    command.allowedUsers.map { user(it, true) }
                    command.disallowedUsers.map { user(it, false) }

                    command.allowedRoles.map { role(it, true) }
                    command.disallowedRoles.map { role(it, false) }
                }

                logger.trace { "Applied permissions for command: ${command.name} ($command)" }
            } else {
                logger.warn { "Applying permissions to global application commands is currently not supported." }
            }
        } catch (e: KtorRequestException) {
            logger.error(e) {
                "Failed to apply application command permissions. This command will not be registered." +
                    if (e.error?.message != null) {
                        "\n        Discord error message: ${e.error?.message}"
                    } else {
                        ""
                    }
            }
        } catch (t: Throwable) {
            logger.error(t) {
                "Failed to apply application command permissions. This command will not be registered."
            }

            return null
        }

        userCommands.set(response.id, command)

        return command
    }

    // endregion

    // region: Unregistration functions

    /** Unregister a message command. **/
    public open suspend fun unregisterGeneric(
        command: ApplicationCommand<*>,
        delete: Boolean = true
    ): ApplicationCommand<*>? =
        when (command) {
            is MessageCommand<*> -> unregister(command, delete)
            is SlashCommand<*, *> -> unregister(command, delete)
            is UserCommand<*> -> unregister(command, delete)

            else -> error("Unsupported application command type: ${command.type.name}")
        }

    /** Unregister a message command. **/
    public open suspend fun unregister(command: MessageCommand<*>, delete: Boolean = true): MessageCommand<*>? {
        val id = messageCommands.entryFlow()
            .firstOrNull { it.value.name == command.name }
            ?.key ?: return null

        if (delete) {
            deleteGeneric(command, id)
        }

        return messageCommands.remove(id)
    }

    /** Unregister a slash command. **/
    public open suspend fun unregister(command: SlashCommand<*, *>, delete: Boolean = true): SlashCommand<*, *>? {
        val id = messageCommands.entryFlow()
            .firstOrNull { it.value.name == command.name }
            ?.key ?: return null

        if (delete) {
            deleteGeneric(command, id)
        }

        return slashCommands.remove(id)
    }

    /** Unregister a user command. **/
    public open suspend fun unregister(command: UserCommand<*>, delete: Boolean = true): UserCommand<*>? {
        val id = messageCommands.entryFlow()
            .firstOrNull { it.value.name == command.name }
            ?.key ?: return null

        if (delete) {
            deleteGeneric(command, id)
        }

        return userCommands.remove(id)
    }

    /** @suppress Internal function used to delete the given command from Discord. Used by [unregister]. **/
    public open suspend fun deleteGeneric(
        command: ApplicationCommand<*>,
        discordCommandId: Snowflake,
    ) {
        try {
            if (command.guildId != null) {
                kord.unsafe.guildApplicationCommand(
                    command.guildId!!,
                    kord.resources.applicationId,
                    discordCommandId
                )
            } else {
                kord.unsafe.globalApplicationCommand(kord.resources.applicationId, discordCommandId).delete()
            }
        } catch (e: KtorRequestException) {
            logger.warn(e) {
                "Failed to delete ${command.type.name} command ${command.name}" +
                    if (e.error?.message != null) {
                        "\n        Discord error message: ${e.error?.message}"
                    } else {
                        ""
                    }
            }
        }
    }

    // endregion

    // region: Event handlers

    /** Event handler for message commands. **/
    public open suspend fun handle(event: MessageCommandInteractionCreateEvent) {
        val commandId = event.interaction.invokedCommandId
        val command = messageCommands.get(commandId)

        command ?: return logger.warn { "Received interaction for unknown message command: ${commandId.asString}" }

        command.call(event)
    }

    /** Event handler for slash commands. **/
    public open suspend fun handle(event: ChatInputCommandInteractionCreateEvent) {
        val commandId = event.interaction.command.rootId
        val command = slashCommands.get(commandId)

        command ?: return logger.warn { "Received interaction for unknown slash command: ${commandId.asString}" }

        command.call(event)
    }

    /** Event handler for user commands. **/
    public open suspend fun handle(event: UserCommandInteractionCreateEvent) {
        val commandId = event.interaction.invokedCommandId
        val command = userCommands.get(commandId)

        command ?: return logger.warn { "Received interaction for unknown user command: ${commandId.asString}" }

        command.call(event)
    }

    // endregion

    // region: Extensions

    /** Registration logic for slash commands, extracted for clarity. **/
    public open suspend fun ChatInputCreateBuilder.register(locale: Locale, command: SlashCommand<*, *>) {
        this.defaultPermission = command.guildId == null || command.allowByDefault

        if (command.hasBody) {
            val args = command.arguments?.invoke()

            if (args != null) {
                args.args.forEach { arg ->
                    val converter = arg.converter

                    if (converter !is SlashCommandConverter) {
                        error("Argument ${arg.displayName} does not support slash commands.")
                    }

                    if (this.options == null) this.options = mutableListOf()

                    val option = converter.toSlashOption(arg)

                    option.name = translationsProvider
                        .translate(option.name, locale, converter.bundle)
                        .lowercase()

                    this.options!! += option
                }
            }
        } else {
            command.subCommands.forEach {
                val args = it.arguments?.invoke()?.args?.map { arg ->
                    val converter = arg.converter

                    if (converter !is SlashCommandConverter) {
                        error("Argument ${arg.displayName} does not support slash commands.")
                    }

                    val option = converter.toSlashOption(arg)

                    option.name = translationsProvider
                        .translate(option.name, locale, converter.bundle)
                        .lowercase()

                    option
                }

                this.subCommand(
                    it.name,
                    it.getTranslatedDescription(locale)
                ) {
                    if (args != null) {
                        if (this.options == null) this.options = mutableListOf()

                        this.options!!.addAll(args)
                    }
                }
            }

            command.groups.values.forEach { group ->
                this.group(group.name, group.getTranslatedDescription(locale)) {
                    group.subCommands.forEach {
                        val args = it.arguments?.invoke()?.args?.map { arg ->
                            val converter = arg.converter

                            if (converter !is SlashCommandConverter) {
                                error("Argument ${arg.displayName} does not support slash commands.")
                            }

                            val option = converter.toSlashOption(arg)

                            option.name = translationsProvider
                                .translate(option.name, locale, converter.bundle)
                                .lowercase()

                            converter.toSlashOption(arg)
                        }

                        this.subCommand(
                            it.name,
                            it.getTranslatedDescription(locale)
                        ) {
                            if (args != null) {
                                if (this.options == null) this.options = mutableListOf()

                                this.options!!.addAll(args)
                            }
                        }
                    }
                }
            }
        }
    }

    /** Registration logic for message commands, extracted for clarity. **/
    @Suppress("UnusedPrivateMember")  // Only for now...
    public open fun MessageCommandCreateBuilder.register(locale: Locale, command: MessageCommand<*>) {
        this.defaultPermission = command.guildId == null || command.allowByDefault
    }

    /** Registration logic for user commands, extracted for clarity. **/
    @Suppress("UnusedPrivateMember")  // Only for now...
    public open fun UserCommandCreateBuilder.register(locale: Locale, command: UserCommand<*>) {
        this.defaultPermission = command.guildId == null || command.allowByDefault
    }

    /** Check whether the type and name of an extension-registered application command matches a Discord one. **/
    public open fun ApplicationCommand<*>.matches(
        locale: Locale,
        other: dev.kord.core.entity.application.ApplicationCommand
    ): Boolean {
        val match = type == other.type && getTranslatedName(locale).equals(other.name, true)

        return when (other) {
            is GuildApplicationCommand -> match && guildId == other.guildId
            else -> match
        }
    }

    /** Check whether the type and name of an extension-registered application command matches a Discord one. **/
    public open fun ApplicationCommand<*>.matches(
        locale: Locale,
        other: ApplicationCommand<*>
    ): Boolean = type == other.type && getTranslatedName(locale).equals(other.name, true) && guildId == other.guildId

    // endregion
}

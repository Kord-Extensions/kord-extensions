@file:Suppress(
    "UNCHECKED_CAST",
    "TooGenericExceptionCaught",
    "StringLiteralDuplication",
)
@file:OptIn(
    KordUnsafe::class,
    KordExperimental::class
)

package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandParser
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.commands.converters.SlashCommandConverter
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.createMessageCommand
import dev.kord.core.behavior.createUserCommand
import dev.kord.core.entity.Guild
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.*
import dev.kord.rest.request.KtorRequestException
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Abstract class representing common behavior for application command registries.
 *
 * Deals with the registration and syncing of, and dispatching to, all application commands.
 * Subtypes should build their functionality on top of this type.
 *
 * @see DefaultApplicationCommandRegistry
 */
public abstract class ApplicationCommandRegistry : KoinComponent {

    protected val logger: KLogger = KotlinLogging.logger { }

    /** Current instance of the bot. **/
    public open val bot: ExtensibleBot by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public open val kord: Kord by inject()

    /** Translations provider, for retrieving translations. **/
    public open val translationsProvider: TranslationsProvider by inject()

    /** Command parser to use for slash commands. **/
    public val argumentParser: SlashCommandParser = SlashCommandParser()

    /** Whether the initial sync has been finished, and commands should be registered directly. **/
    public var initialised: Boolean = false

    /** Quick access to the human-readable name for a Discord application command type. **/
    public val ApplicationCommandType.name: String
        get() = when (this) {
            is ApplicationCommandType.Unknown -> "unknown"

            ApplicationCommandType.ChatInput -> "slash"
            ApplicationCommandType.Message -> "message"
            ApplicationCommandType.User -> "user"
        }

    /** Handles the initial registration of commands, after extensions have been loaded. **/
    public suspend fun initialRegistration() {
        if (initialised) {
            return
        }

        val commands: MutableList<ApplicationCommand<*>> = mutableListOf()

        bot.extensions.values.forEach {
            commands += it.messageCommands
            commands += it.slashCommands
            commands += it.userCommands
        }

        try {
            initialize(commands)
        } catch (t: Throwable) {
            logger.error(t) { "Failed to initialize registry" }
        }

        initialised = true
    }

    /** Called once the initial registration started and all extensions are loaded. **/
    protected abstract suspend fun initialize(commands: List<ApplicationCommand<*>>)

    /** Register a [SlashCommand] to the registry.
     *
     * This method is only called after the [initialize] method and allows runtime modifications.
     */
    public abstract suspend fun register(command: SlashCommand<*, *>): SlashCommand<*, *>?

    /**
     * Register a [MessageCommand] to the registry.
     *
     * This method is only called after the [initialize] method and allows runtime modifications.
     */
    public abstract suspend fun register(command: MessageCommand<*>): MessageCommand<*>?

    /** Register a [UserCommand] to the registry.
     *
     * This method is only called after the [initialize] method and allows runtime modifications.
     */
    public abstract suspend fun register(command: UserCommand<*>): UserCommand<*>?

    /** Event handler for slash commands. **/
    public abstract suspend fun handle(event: ChatInputCommandInteractionCreateEvent)

    /** Event handler for message commands. **/
    public abstract suspend fun handle(event: MessageCommandInteractionCreateEvent)

    /** Event handler for user commands. **/
    public abstract suspend fun handle(event: UserCommandInteractionCreateEvent)

    /** Unregister a slash command. **/
    public abstract suspend fun unregister(command: SlashCommand<*, *>, delete: Boolean = true): SlashCommand<*, *>?

    /** Unregister a message command. **/
    public abstract suspend fun unregister(command: MessageCommand<*>, delete: Boolean = true): MessageCommand<*>?

    /** Unregister a user command. **/
    public abstract suspend fun unregister(command: UserCommand<*>, delete: Boolean = true): UserCommand<*>?

    // region: Utilities

    /** Unregister a generic [ApplicationCommand]. **/
    public open suspend fun unregisterGeneric(
        command: ApplicationCommand<*>,
        delete: Boolean = true,
    ): ApplicationCommand<*>? =
        when (command) {
            is MessageCommand<*> -> unregister(command, delete)
            is SlashCommand<*, *> -> unregister(command, delete)
            is UserCommand<*> -> unregister(command, delete)

            else -> error("Unsupported application command type: ${command.type.name}")
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
                ).delete()
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

    /** Register multiple slash commands. **/
    public open suspend fun <T : ApplicationCommand<*>> registerAll(vararg commands: T): List<T> =
        commands.mapNotNull {
            try {
                when (it) {
                    is SlashCommand<*, *> -> register(it) as T
                    is MessageCommand<*> -> register(it) as T
                    is UserCommand<*> -> register(it) as T

                    else -> throw IllegalArgumentException(
                        "The registry does not know about this type of ApplicationCommand"
                    )
                }
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
        }

    /**
     * Creates a KordEx [ApplicationCommand] as discord command and returns the created command's id as [Snowflake].
     */
    public open suspend fun createDiscordCommand(command: ApplicationCommand<*>): Snowflake? = when (command) {
        is SlashCommand<*, *> -> createDiscordSlashCommand(command)
        is UserCommand<*> -> createDiscordUserCommand(command)
        is MessageCommand<*> -> createDiscordMessageCommand(command)

        else -> throw IllegalArgumentException("Unknown ApplicationCommand type")
    }

    /**
     * Creates a KordEx [SlashCommand] as discord command and returns the created command's id as [Snowflake].
     */
    public open suspend fun createDiscordSlashCommand(command: SlashCommand<*, *>): Snowflake? {
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

        injectPermissions(guild, command, response.id) ?: return null

        return response.id
    }

    /**
     * Creates a KordEx [UserCommand] as discord command and returns the created command's id as [Snowflake].
     */
    public open suspend fun createDiscordUserCommand(command: UserCommand<*>): Snowflake? {
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

        injectPermissions(guild, command, response.id) ?: return null

        return response.id
    }

    /**
     * Creates a KordEx [MessageCommand] as discord command and returns the created command's id as [Snowflake].
     */
    public open suspend fun createDiscordMessageCommand(command: MessageCommand<*>): Snowflake? {
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

        injectPermissions(guild, command, response.id) ?: return null

        return response.id
    }

    // endregion

    // region: Permissions

    protected suspend fun <T : ApplicationCommand<*>> injectPermissions(
        guild: Guild?,
        command: T,
        commandId: Snowflake
    ): T? {
        try {
            if (guild != null) {
                kord.editApplicationCommandPermissions(kord.resources.applicationId, guild.id, commandId) {
                    injectRawPermissions(this, command)
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
        return command
    }

    protected fun injectRawPermissions(
        builder: ApplicationCommandPermissionsModifyBuilder,
        command: ApplicationCommand<*>
    ) {
        command.allowedUsers.map { builder.user(it, true) }
        command.disallowedUsers.map { builder.user(it, false) }

        command.allowedRoles.map { builder.role(it, true) }
        command.disallowedRoles.map { builder.role(it, false) }
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
    ): Boolean = type == other.type && getTranslatedName(locale).equals(other.name, true)

    // endregion
}

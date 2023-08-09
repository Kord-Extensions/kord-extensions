/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandParser
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.commands.converters.SlashCommandConverter
import com.kotlindiscord.kord.extensions.commands.getDefaultTranslatedDisplayName
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.asJavaLocale
import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Choice
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.Kord
import dev.kord.core.behavior.createChatInputCommand
import dev.kord.core.behavior.createMessageCommand
import dev.kord.core.behavior.createUserCommand
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.*
import dev.kord.rest.request.KtorRequestException
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.component.inject
import java.util.*
import javax.naming.InvalidNameException

/**
 * Abstract class representing common behavior for application command registries.
 *
 * Deals with the registration and syncing of, and dispatching to, all application commands.
 * Subtypes should build their functionality on top of this type.
 *
 * @see DefaultApplicationCommandRegistry
 */
public abstract class ApplicationCommandRegistry : KordExKoinComponent {

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
    public abstract suspend fun register(command: SlashCommand<*, *, *>): SlashCommand<*, *, *>?

    /**
     * Register a [MessageCommand] to the registry.
     *
     * This method is only called after the [initialize] method and allows runtime modifications.
     */
    public abstract suspend fun register(command: MessageCommand<*, *>): MessageCommand<*, *>?

    /** Register a [UserCommand] to the registry.
     *
     * This method is only called after the [initialize] method and allows runtime modifications.
     */
    public abstract suspend fun register(command: UserCommand<*, *>): UserCommand<*, *>?

    /** Event handler for slash commands. **/
    public abstract suspend fun handle(event: ChatInputCommandInteractionCreateEvent)

    /** Event handler for message commands. **/
    public abstract suspend fun handle(event: MessageCommandInteractionCreateEvent)

    /** Event handler for user commands. **/
    public abstract suspend fun handle(event: UserCommandInteractionCreateEvent)

    /** Event handler for autocomplete interactions. **/
    public abstract suspend fun handle(event: AutoCompleteInteractionCreateEvent)

    /** Unregister a slash command. **/
    public abstract suspend fun unregister(
        command: SlashCommand<*, *, *>,
        delete: Boolean = true
    ): SlashCommand<*, *, *>?

    /** Unregister a message command. **/
    public abstract suspend fun unregister(command: MessageCommand<*, *>, delete: Boolean = true): MessageCommand<*, *>?

    /** Unregister a user command. **/
    public abstract suspend fun unregister(command: UserCommand<*, *>, delete: Boolean = true): UserCommand<*, *>?

    // region: Utilities

    /** Unregister a generic [ApplicationCommand]. **/
    public open suspend fun unregisterGeneric(
        command: ApplicationCommand<*>,
        delete: Boolean = true,
    ): ApplicationCommand<*>? =
        when (command) {
            is MessageCommand<*, *> -> unregister(command, delete)
            is SlashCommand<*, *, *> -> unregister(command, delete)
            is UserCommand<*, *> -> unregister(command, delete)

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
        commands.sortedByDescending { it.name }.mapNotNull {
            try {
                when (it) {
                    is SlashCommand<*, *, *> -> register(it) as T
                    is MessageCommand<*, *> -> register(it) as T
                    is UserCommand<*, *> -> register(it) as T

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
        is SlashCommand<*, *, *> -> createDiscordSlashCommand(command)
        is UserCommand<*, *> -> createDiscordUserCommand(command)
        is MessageCommand<*, *> -> createDiscordMessageCommand(command)

        else -> throw IllegalArgumentException("Unknown ApplicationCommand type")
    }

    /**
     * Creates a KordEx [SlashCommand] as discord command and returns the created command's id as [Snowflake].
     */
    public open suspend fun createDiscordSlashCommand(command: SlashCommand<*, *, *>): Snowflake? {
        val locale = bot.settings.i18nBuilder.defaultLocale

        val guild = if (command.guildId != null) {
            kord.getGuildOrNull(command.guildId!!)
        } else {
            null
        }

        val (name, nameLocalizations) = command.localizedName
        val (description, descriptionLocalizations) = command.localizedDescription

        val response = if (guild == null) {
            // We're registering global commands here, if the guild is null

            kord.createGlobalChatInputCommand(name, description) {
                logger.trace { "Adding/updating global ${command.type.name} command: $name" }

                this.nameLocalizations = nameLocalizations
                this.descriptionLocalizations = descriptionLocalizations

                this.register(locale, command)
            }
        } else {
            // We're registering guild-specific commands here, if the guild is available

            guild.createChatInputCommand(name, description) {
                logger.trace { "Adding/updating guild-specific ${command.type.name} command: $name" }

                this.nameLocalizations = nameLocalizations
                this.descriptionLocalizations = descriptionLocalizations

                this.register(locale, command)
            }
        }

        return response.id
    }

    /**
     * Creates a KordEx [UserCommand] as discord command and returns the created command's id as [Snowflake].
     */
    public open suspend fun createDiscordUserCommand(command: UserCommand<*, *>): Snowflake? {
        val locale = bot.settings.i18nBuilder.defaultLocale

        val guild = if (command.guildId != null) {
            kord.getGuildOrNull(command.guildId!!)
        } else {
            null
        }

        val (name, nameLocalizations) = command.localizedName

        val response = if (guild == null) {
            // We're registering global commands here, if the guild is null

            kord.createGlobalUserCommand(name) {
                logger.trace { "Adding/updating global ${command.type.name} command: $name" }
                this.nameLocalizations = nameLocalizations

                this.register(locale, command)
            }
        } else {
            // We're registering guild-specific commands here, if the guild is available

            guild.createUserCommand(name) {
                logger.trace { "Adding/updating guild-specific ${command.type.name} command: $name" }
                this.nameLocalizations = nameLocalizations

                this.register(locale, command)
            }
        }

        return response.id
    }

    /**
     * Creates a KordEx [MessageCommand] as discord command and returns the created command's id as [Snowflake].
     */
    public open suspend fun createDiscordMessageCommand(command: MessageCommand<*, *>): Snowflake? {
        val locale = bot.settings.i18nBuilder.defaultLocale

        val guild = if (command.guildId != null) {
            kord.getGuildOrNull(command.guildId!!)
        } else {
            null
        }

        val (name, nameLocalizations) = command.localizedName

        val response = if (guild == null) {
            // We're registering global commands here, if the guild is null

            kord.createGlobalMessageCommand(name) {
                logger.trace { "Adding/updating global ${command.type.name} command: $name" }
                this.nameLocalizations = nameLocalizations

                this.register(locale, command)
            }
        } else {
            // We're registering guild-specific commands here, if the guild is available

            guild.createMessageCommand(name) {
                logger.trace { "Adding/updating guild-specific ${command.type.name} command: $name" }
                this.nameLocalizations = nameLocalizations

                this.register(locale, command)
            }
        }

        return response.id
    }

    // endregion

    // region: Extensions
    /** Registration logic for slash commands, extracted for clarity. **/
    public open suspend fun ChatInputCreateBuilder.register(locale: Locale, command: SlashCommand<*, *, *>) {
        if (this is GlobalChatInputCreateBuilder) {
            registerGlobalPermissions(locale, command)
        } else {
            registerGuildPermissions(locale, command)
        }

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

                    option.translate(command, arg)

                    if (option is BaseChoiceBuilder<*> && arg.converter.genericBuilder.autoCompleteCallback != null) {
                        option.choices?.clear()
                    }

                    option.autocomplete = arg.converter.genericBuilder.autoCompleteCallback != null

                    this.options!! += option
                }
            }
        } else {
            command.subCommands.sortedByDescending { it.name }.forEach {
                val args = it.arguments?.invoke()?.args?.map { arg ->
                    val converter = arg.converter

                    if (converter !is SlashCommandConverter) {
                        error("Argument ${arg.displayName} does not support slash commands.")
                    }

                    val option = converter.toSlashOption(arg)

                    option.translate(command, arg)

                    if (option is BaseChoiceBuilder<*> && arg.converter.genericBuilder.autoCompleteCallback != null) {
                        option.choices?.clear()
                    }

                    option.autocomplete = arg.converter.genericBuilder.autoCompleteCallback != null

                    option
                }

                val (name, nameLocalizations) = it.localizedName
                val (description, descriptionLocalizations) = it.localizedDescription

                this.subCommand(
                    name,
                    description
                ) {
                    this.nameLocalizations = nameLocalizations
                    this.descriptionLocalizations = descriptionLocalizations

                    if (args != null) {
                        if (this.options == null) this.options = mutableListOf()

                        this.options!!.addAll(args)
                    }
                }
            }

            command.groups.values.sortedByDescending { it.name }.forEach { group ->
                val (name, nameLocalizations) = group.localizedName
                val (description, descriptionLocalizations) = group.localizedDescription

                this.group(name, description) {
                    this.nameLocalizations = nameLocalizations
                    this.descriptionLocalizations = descriptionLocalizations

                    group.subCommands.sortedByDescending { it.name }.forEach {
                        val args = it.arguments?.invoke()?.args?.map { arg ->
                            val converter = arg.converter

                            if (converter !is SlashCommandConverter) {
                                error("Argument ${arg.displayName} does not support slash commands.")
                            }

                            val option = converter.toSlashOption(arg)

                            option.translate(command, arg)

                            if (
                                option is BaseChoiceBuilder<*> &&
                                arg.converter.genericBuilder.autoCompleteCallback != null
                            ) {
                                option.choices?.clear()
                            }

                            option.autocomplete = arg.converter.genericBuilder.autoCompleteCallback != null

                            option
                        }

                        val (name, nameLocalizations) = it.localizedName
                        val (description, descriptionLocalizations) = it.localizedDescription

                        this.subCommand(
                            name,
                            description
                        ) {
                            this.nameLocalizations = nameLocalizations
                            this.descriptionLocalizations = descriptionLocalizations

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
    public open fun MessageCommandCreateBuilder.register(locale: Locale, command: MessageCommand<*, *>) {
        registerGuildPermissions(locale, command)
    }

    /** Registration logic for user commands, extracted for clarity. **/
    @Suppress("UnusedPrivateMember")  // Only for now...
    public open fun UserCommandCreateBuilder.register(locale: Locale, command: UserCommand<*, *>) {
        registerGuildPermissions(locale, command)
    }

    /** Registration logic for message commands, extracted for clarity. **/
    @Suppress("UnusedPrivateMember")  // Only for now...
    public open fun GlobalMessageCommandCreateBuilder.register(locale: Locale, command: MessageCommand<*, *>) {
        registerGuildPermissions(locale, command)
        registerGlobalPermissions(locale, command)
    }

    /** Registration logic for user commands, extracted for clarity. **/
    @Suppress("UnusedPrivateMember")  // Only for now...
    public open fun GlobalUserCommandCreateBuilder.register(locale: Locale, command: UserCommand<*, *>) {
        registerGuildPermissions(locale, command)
        registerGlobalPermissions(locale, command)
    }

    /**
     * Registers the global permissions of [command].
     */
    public open fun GlobalApplicationCommandCreateBuilder.registerGlobalPermissions(
        locale: Locale,
        command: ApplicationCommand<*>,
    ) {
        registerGuildPermissions(locale, command)
        this.dmPermission = command.allowInDms
    }

    /**
     * Registers the guild permission of [command].
     */
    public open fun ApplicationCommandCreateBuilder.registerGuildPermissions(
        locale: Locale,
        command: ApplicationCommand<*>,
    ) {
        this.defaultMemberPermissions = command.defaultMemberPermissions
    }

    /** Check whether the type and name of an extension-registered application command matches a Discord one. **/
    public open fun ApplicationCommand<*>.matches(
        locale: Locale,
        other: dev.kord.core.entity.application.ApplicationCommand,
    ): Boolean = type == other.type && localizedName.default.equals(other.name, true)

    // endregion

    private fun OptionsBuilder.translate(command: ApplicationCommand<*>, argObj: Argument<*>) {
        val defaultName = argObj.getDefaultTranslatedDisplayName(command.translationsProvider, command)

        if (defaultName != defaultName.lowercase(command.translationsProvider.defaultLocale)) {
            throw InvalidNameException(
                "Argument $name for command ${command.name} does not have a lower-case name in the configured " +
                    "default locale: ${command.translationsProvider.defaultLocale} -> $defaultName - this will " +
                    "cause issues with matching your command arguments to the options provided by users on Discord"
            )
        }

        val (name, nameLocalizations) = command.localize(name, true)

        nameLocalizations.forEach { (locale, string) ->
            if (string != string.lowercase(locale.asJavaLocale())) {
                logger.warn {
                    "Argument $name for command ${command.name} is not lower-case in the ${locale.asJavaLocale()} " +
                        "locale: $string"
                }
            }
        }

        this.name = name
        this.nameLocalizations = nameLocalizations

        val (description, descriptionLocalizations) = command.localize(description)

        this.description = description
        this.descriptionLocalizations = descriptionLocalizations

        if (this is BaseChoiceBuilder<*> && !choices.isNullOrEmpty()) {
            translate(command)
        }
    }

    @Suppress("DEPRECATION_ERROR")
    private fun BaseChoiceBuilder<*>.translate(command: ApplicationCommand<*>) {
        choices = choices!!.map {
            val (name, nameLocalizations) = command.localize(it.name)

            when (it) {
                is Choice.NumberChoice -> Choice.NumberChoice(name, Optional(nameLocalizations), it.value)
                is Choice.StringChoice -> Choice.StringChoice(name, Optional(nameLocalizations), it.value)
                is Choice.IntegerChoice -> Choice.IntegerChoice(name, Optional(nameLocalizations), it.value)
            }
        }.toMutableList()
    }
}

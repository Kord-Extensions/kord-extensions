package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandParser
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Abstraction layer between the [ExtensibleBot] and it's event handling.
 *
 * An implementing class is supposed register all commands and handle their discord events and lifecycles.
 */
public abstract class ApplicationCommandRegistry : KoinComponent {

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

    /** Handles the initial registration of commands, after extensions have been loaded. **/
    public suspend fun initialRegistration() {
        if (initialised) {
            return
        }
        initialize()
        initialised = true
    }

    /** Called once the initial registration started and all extensions are loaded. **/
    protected abstract suspend fun initialize()

    /** Register a [SlashCommand] to the registry.
     *
     * This method is called before the [initialize] method.
     */
    public abstract suspend fun register(command: SlashCommand<*, *>): SlashCommand<*, *>?

    /**
     * Register a [MessageCommand] to the registry.
     *
     * This method is called before the [initialize] method.
     */
    public abstract suspend fun register(command: MessageCommand<*>): MessageCommand<*>?

    /** Register a [UserCommand] to the registry.
     *
     * This method is called before the [initialize] method.
     */
    public abstract suspend fun register(command: UserCommand<*>): UserCommand<*>?

    /** Event handler for slash commands. **/
    public abstract suspend fun handle(event: ChatInputCommandInteractionCreateEvent)

    /** Event handler for message commands. **/
    public abstract suspend fun handle(event: MessageCommandInteractionCreateEvent)

    /** Event handler for user commands. **/
    public abstract suspend fun handle(event: UserCommandInteractionCreateEvent)
}

@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.types.ChatCommandCheck
import com.kotlindiscord.kord.extensions.checks.types.MessageCommandCheck
import com.kotlindiscord.kord.extensions.checks.types.SlashCommandCheck
import com.kotlindiscord.kord.extensions.checks.types.UserCommandCheck
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandRegistry
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandRegistry
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.events.ExtensionStateEvent
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.gateway.Intent
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private val logger = KotlinLogging.logger {}

/**
 * Class representing a distinct set of functionality to be treated as a unit.
 *
 * Override this and create your own extensions with their own event handlers and commands.
 * This will allow you to keep distinct blocks of functionality separate, keeping the codebase
 * clean and configurable.
 */
public abstract class Extension : KoinComponent {
    /** The [ExtensibleBot] instance that this extension is installed to. **/
    public open val bot: ExtensibleBot by inject()

    /** Current Kord instance powering the bot. **/
    public open val kord: Kord by inject()

    /** Message command registry. **/
    public open val chatCommandRegistry: ChatCommandRegistry by inject()

    /** Slash command registry. **/
    public open val applicationCommandRegistry: ApplicationCommandRegistry by inject()

    /**
     * The name of this extension.
     *
     * Ensure you override this in your extension. This should be a unique name that can later
     * be used to refer to your specific extension after it's been registered.
     */
    public abstract val name: String

    /**
     * The current loading/unloading state of the extension.
     */
    public open var state: ExtensionState = ExtensionState.UNLOADED

    /** Check whether this extension's state is [ExtensionState.LOADED]. **/
    public open val loaded: Boolean get() = state == ExtensionState.LOADED

    /**
     * List of registered event handlers.
     *
     * When an extension is unloaded, all the event handlers are cancelled and
     * removed from the bot.
     */
    public open val eventHandlers: MutableList<EventHandler<out Event>> = mutableListOf()

    /**
     * List of registered commands.
     *
     * When an extension is unloaded, all the commands are removed from the bot.
     */
    public open val chatCommands: MutableList<ChatCommand<out Arguments>> = mutableListOf()

    /**
     * List of registered slash commands.
     *
     * Unlike normal commands, slash commands cannot be unregistered dynamically. However, slash commands that
     * belong to unloaded extensions will not execute.
     */
    public open val messageCommands: MutableList<MessageCommand<*>> = mutableListOf()

    /**
     * List of registered slash commands.
     *
     * Unlike normal commands, slash commands cannot be unregistered dynamically. However, slash commands that
     * belong to unloaded extensions will not execute.
     */
    public open val slashCommands: MutableList<SlashCommand<*, *>> = mutableListOf()

    /**
     * List of registered slash commands.
     *
     * Unlike normal commands, slash commands cannot be unregistered dynamically. However, slash commands that
     * belong to unloaded extensions will not execute.
     */
    public open val userCommands: MutableList<UserCommand<*>> = mutableListOf()

    /**
     * List of chat command checks.
     *
     * These checks will be checked against all chat commands in this extension.
     */
    public open val chatCommandChecks: MutableList<ChatCommandCheck> =
        mutableListOf()

    /**
     * List of message command checks.
     *
     * These checks will be checked against all message commands in this extension.
     */
    public val messageCommandChecks: MutableList<MessageCommandCheck> = mutableListOf()

    /**
     * List of slash command checks.
     *
     * These checks will be checked against all slash commands in this extension.
     */
    public val slashCommandChecks: MutableList<SlashCommandCheck> = mutableListOf()

    /**
     * List of user command checks.
     *
     * These checks will be checked against all user commands in this extension.
     */
    public val userCommandChecks: MutableList<UserCommandCheck> = mutableListOf()

    /** String representing the bundle to get translations from for command names/descriptions. **/
    public open val bundle: String? = null

    /** Set of intents required by this extension's event handlers and commands. **/
    public open val intents: MutableSet<Intent> = mutableSetOf()

    /**
     * Override this in your subclass and use it to register your commands and event
     * handlers.
     *
     * This function simply allows you to register commands and event handlers in the context
     * of a suspended function, which is required in order to make use of some other APIs. As a
     * result, we recommend you make use of this in all your extensions, instead of init {}
     * blocks.
     *
     * This function is called on first extension load, and whenever it's reloaded after that.
     */
    public abstract suspend fun setup()

    /**
     * @suppress This is an internal API function used as part of extension lifecycle management.
     */
    public open suspend fun doSetup() {
        this.setState(ExtensionState.LOADING)

        @Suppress("TooGenericExceptionCaught")
        try {
            this.setup()
        } catch (t: Throwable) {
            this.setState(ExtensionState.FAILED_LOADING)
            throw t
        }

        this.setState(ExtensionState.LOADED)
    }

    /** Update this extension's state, firing the extension state change event. **/
    public open suspend fun setState(state: ExtensionState) {
        bot.send(ExtensionStateEvent(this, state))

        this.state = state
    }

    /**
     * If you need to, override this function and use it to clean up your extension when
     * it's unloaded.
     *
     * You do not need to override this to clean up commands and event handlers, that's
     * handled for you.
     */
    public open suspend fun unload() {
        logger.trace { "Unload function not overridden." }
    }

    /**
     * Unload all event handlers and commands for this extension.
     *
     * This function is called as part of unloading extensions, which may be
     * done programmatically.
     *
     * @suppress Internal function
     */
    public open suspend fun doUnload() {
        var error: Throwable? = null

        this.setState(ExtensionState.UNLOADING)

        @Suppress("TooGenericExceptionCaught")
        try {
            this.unload()
        } catch (t: Throwable) {
            error = t

            this.setState(ExtensionState.FAILED_UNLOADING)
        }

        for (handler in eventHandlers) {
            handler.job?.cancel()
            bot.removeEventHandler(handler)
        }

        for (command in chatCommands) {
            chatCommandRegistry.remove(command)
        }

        eventHandlers.clear()
        chatCommands.clear()

        if (error != null) {
            throw error
        }

        this.setState(ExtensionState.UNLOADED)
    }
}

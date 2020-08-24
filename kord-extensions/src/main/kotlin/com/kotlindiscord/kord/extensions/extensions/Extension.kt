package com.kotlindiscord.kord.extensions.extensions

import com.gitlab.kordlib.core.event.Event
import com.kotlindiscord.kord.extensions.*
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.events.EventHandler
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Class representing a distinct set of functionality to be treated as a unit.
 *
 * Override this and create your own extensions with their own event handlers and commands.
 * This will allow you to keep distinct blocks of functionality separate, keeping the codebase
 * clean and configurable.
 *
 * @param bot The [ExtensibleBot] instance that this extension is installed to.
 */
abstract class Extension(val bot: ExtensibleBot) {
    /**
     * The name of this extension.
     *
     * Ensure you override this in your extension. This should be a unique name that can later
     * be used to refer to your specific extension after it's been registered.
     */
    abstract val name: String

    /**
     * Override this in your subclass and use it to register your commands and event
     * handlers.
     *
     * This function simply allows you to register commands and event handlers in the context
     * of a suspended function, which is required in order to make use of some other APIs. As a
     * result, we recommend you make use of this in all your extensions, instead of init {}
     * blocks.
     *
     * This function is only ever called once.
     */
    abstract suspend fun setup()

    /**
     * @suppress This is an internal API function used as part of extension lifecycle management.
     */
    suspend fun doSetup() {
        this.setup()
        loaded = true
    }

    /**
     * Whether this extension is currently loaded.
     *
     * This is set during loading/unloading of the extension and is used to ensure
     * things aren't being called when they shouldn't be.
     */
    var loaded = false

    /**
     * List of registered event handlers.
     *
     * When an extension is unloaded, all the event handlers are cancelled and
     * removed from the bot.
     */
    val eventHandlers = mutableListOf<EventHandler<out Event>>()

    /**
     * List of registered commands.
     *
     * When an extension is unloaded, all the commands are removed from the bot.
     */
    val commands = mutableListOf<Command>()

    /**
     * DSL function for easily registering a command.
     *
     * Use this in your setup function to register a command that may be executed on Discord.
     *
     * @param T
     * @param body Builder lambda used for setting up the command object.
     */
    suspend fun command(body: suspend Command.() -> Unit): Command {
        val commandObj = Command(this)

        body.invoke(commandObj)

        try {
            commandObj.validate()
            bot.addCommand(commandObj)
            commands.add(commandObj)
        } catch (e: CommandRegistrationException) {
            logger.error(e) { "Failed to register command - $e" }
        } catch (e: InvalidCommandException) {
            logger.error(e) { "Failed to register command - $e" }
        }

        return commandObj
    }

    /**
     * Unload all event handlers and commands for this extension.
     *
     * This function is called as part of unloading extensions, which may be
     * done programmatically.
     */
    fun unload() {
        for (handler in eventHandlers) {
            handler.job?.cancel()
            bot.removeEventHandler(handler)
        }

        for (command in commands) {
            bot.removeCommand(command)
        }

        eventHandlers.clear()
        commands.clear()

        loaded = false
    }

    /**
     * DSL function for easily registering an event handler.
     *
     * Use this in your setup function to register an event handler that reacts to a given event.
     *
     * @param body Builder lambda used for setting up the event handler object.
     */
    suspend inline fun <reified T : Event> event(noinline body: suspend EventHandler<T>.() -> Unit): EventHandler<T> {
        val eventHandler = EventHandler<T>(this, T::class)
        val logger = KotlinLogging.logger {}

        body.invoke(eventHandler)

        try {
            eventHandler.validate()
            eventHandler.job = bot.addEventHandler(eventHandler)
            eventHandlers.add(eventHandler)
        } catch (e: EventHandlerRegistrationException) {
            logger.error(e) { "Failed to register event handler - $e" }
        } catch (e: InvalidEventHandlerException) {
            logger.error(e) { "Failed to register event handler - $e" }
        }

        return eventHandler
    }
}

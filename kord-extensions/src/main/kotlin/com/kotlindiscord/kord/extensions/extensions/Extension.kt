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
        } catch (e: CommandRegistrationException) {
            logger.error(e) { "Failed to register command - $e" }
        } catch (e: InvalidCommandException) {
            logger.error(e) { "Failed to register command - $e" }
        }

        return commandObj
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
            bot.addEventHandler(eventHandler)
        } catch (e: EventHandlerRegistrationException) {
            logger.error(e) { "Failed to register event handler - $e" }
        } catch (e: InvalidEventHandlerException) {
            logger.error(e) { "Failed to register event handler - $e" }
        }

        return eventHandler
    }
}

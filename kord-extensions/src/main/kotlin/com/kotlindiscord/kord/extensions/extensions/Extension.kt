package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.*
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.GroupCommand
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.events.ExtensionLoadedEvent
import com.kotlindiscord.kord.extensions.events.ExtensionUnloadedEvent
import mu.KotlinLogging
import org.koin.core.Koin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import kotlin.reflect.KClass

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
public abstract class Extension(public val bot: ExtensibleBot) {
    /**
     * The name of this extension.
     *
     * Ensure you override this in your extension. This should be a unique name that can later
     * be used to refer to your specific extension after it's been registered.
     */
    public abstract val name: String

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
        this.setup()
        loaded = true

        bot.send(ExtensionLoadedEvent(bot, this))
    }

    /**
     * Whether this extension is currently loaded.
     *
     * This is set during loading/unloading of the extension and is used to ensure
     * things aren't being called when they shouldn't be.
     */
    public open var loaded: Boolean = false

    /**
     * List of registered event handlers.
     *
     * When an extension is unloaded, all the event handlers are cancelled and
     * removed from the bot.
     */
    public open val eventHandlers: MutableList<EventHandler<out Any>> = mutableListOf()

    /**
     * List of registered commands.
     *
     * When an extension is unloaded, all the commands are removed from the bot.
     */
    public open val commands: MutableList<Command> = mutableListOf()

    /** Quick access to this bot's Koin instance. **/
    public open val koin: Koin = bot.koin

    /**
     * DSL function for easily registering a command.
     *
     * Use this in your setup function to register a command that may be executed on Discord.
     *
     * @param body Builder lambda used for setting up the command object.
     */
    public open suspend fun command(body: suspend Command.() -> Unit): Command {
        val commandObj = Command(this)
        body.invoke(commandObj)

        return command(commandObj)
    }

    /**
     * Function for registering a custom command object.
     *
     * You can use this if you have a custom command subclass you need to register.
     *
     * @param commandObj Command object to register.
     */
    public open suspend fun command(commandObj: Command): Command {
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
     * DSL function for easily registering a grouped command.
     *
     * Use this in your setup function to register a group of commands.
     *
     * The body of the grouped command will be executed if there is no
     * matching subcommand.
     *
     * @param body Builder lambda used for setting up the command object.
     */
    public open suspend fun group(body: suspend GroupCommand.() -> Unit): GroupCommand {
        val commandObj = GroupCommand(this)
        body.invoke(commandObj)

        return command(commandObj) as GroupCommand
    }

    /**
     * If you need to, override this function and use it to clean up your extension when
     * it's unloaded.
     *
     * You do not need to override this to clean up commands and event handlers, that's
     * handled for you.
     */
    public open suspend fun unload() {
        logger.debug { "Unload function not overridden." }
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
        this.unload()

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
        bot.send(ExtensionUnloadedEvent(bot, this))
    }

    /**
     * DSL function for easily registering an event handler.
     *
     * Use this in your setup function to register an event handler that reacts to a given event.
     *
     * @param body Builder lambda used for setting up the event handler object.
     */
    public suspend inline fun <reified T : Any> event(
        noinline body: suspend EventHandler<T>.() -> Unit
    ): EventHandler<T> {
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

    /** Quick access to the current Koin context's `inject` function. **/
    public inline fun <reified T : Any> kInject(
        qualifier: Qualifier? = null,
        mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
        noinline parameters: ParametersDefinition? = null
    ): Lazy<T> = koin.inject<T>(qualifier, mode, parameters)

    /** Quick access to the current Koin context's `injectOrNull` function. **/
    public inline fun <reified T : Any> kInjectOrNull(
        qualifier: Qualifier? = null,
        mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
        noinline parameters: ParametersDefinition? = null
    ): Lazy<T?> = koin.injectOrNull<T>(qualifier, mode, parameters)

    /** Quick access to the current Koin context's `get` function. **/
    public inline fun <reified T : Any> kGet(
        qualifier: Qualifier? = null,
        noinline parameters: ParametersDefinition? = null
    ): T = koin.get<T>(qualifier, parameters)

    /** Quick access to the current Koin context's `getOrNull` function. **/
    public inline fun <reified T : Any> kGetOrNull(
        qualifier: Qualifier? = null,
        noinline parameters: ParametersDefinition? = null
    ): T? = koin.getOrNull<T>(qualifier, parameters)

    /** Quick access to the current Koin context's `get` function. **/
    public fun <T : Any> kGet(
        clazz: KClass<T>,
        qualifier: Qualifier? = null,
        parameters: ParametersDefinition? = null
    ): T = koin.get<T>(clazz, qualifier, parameters)

    /** Quick access to the current Koin context's `getOrNull` function. **/
    public fun <T : Any> kGetOrNull(
        clazz: KClass<T>,
        qualifier: Qualifier? = null,
        parameters: ParametersDefinition? = null
    ): T? = koin.getOrNull<T>(clazz, qualifier, parameters)
}

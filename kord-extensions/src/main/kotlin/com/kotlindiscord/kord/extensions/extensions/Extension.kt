package com.kotlindiscord.kord.extensions.extensions

import com.kotlindiscord.kord.extensions.*
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.userFor
import com.kotlindiscord.kord.extensions.commands.GroupCommand
import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.MessageCommandRegistry
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandRegistry
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.events.ExtensionStateEvent
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.Serializable
import java.util.*

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
    private val messageCommandsRegistry: MessageCommandRegistry by inject()

    /** Slash command registry. **/
    private val slashCommandsRegistry: SlashCommandRegistry by inject()

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
    public open val eventHandlers: MutableList<EventHandler<out Any>> = mutableListOf()

    /**
     * List of registered commands.
     *
     * When an extension is unloaded, all the commands are removed from the bot.
     */
    public open val commands: MutableList<MessageCommand<out Arguments>> = mutableListOf()

    /**
     * List of registered slash commands.
     *
     * Unlike normal commands, slash commands cannot be unregistered dynamically. However, slash commands that
     * belong to unloaded extensions will not execute.
     */
    public open val slashCommands: MutableList<SlashCommand<out Arguments>> = mutableListOf()

    /**
     * List of slash command checks.
     *
     * These checks will be checked against all commands in this extension.
     */
    public open val commandChecks: MutableList<suspend (MessageCreateEvent) -> Boolean> = mutableListOf()

    /**
     * List of slash command checks.
     *
     * These checks will be checked against all slash commands in this extension.
     */
    public open val slashCommandChecks: MutableList<suspend (InteractionCreateEvent) -> Boolean> = mutableListOf()

    /** String representing the bundle to get translations from for command names/descriptions. **/
    public open val bundle: String? = null

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
        bot.send(ExtensionStateEvent(bot, this, state))

        this.state = state
    }

    /**
     * DSL function for easily registering a command.
     *
     * Use this in your setup function to register a command that may be executed on Discord.
     *
     * @param body Builder lambda used for setting up the command object.
     */
    @ExtensionDSL
    public open suspend fun <T : Arguments> command(
        arguments: (() -> T)?,
        body: suspend MessageCommand<T>.() -> Unit
    ): MessageCommand<T> {
        val commandObj = MessageCommand(this, arguments)
        body.invoke(commandObj)

        return command(commandObj)
    }

    /**
     * DSL function for easily registering a command, without arguments.
     *
     * Use this in your setup function to register a command that may be executed on Discord.
     *
     * @param body Builder lambda used for setting up the command object.
     */
    @ExtensionDSL
    public open suspend fun command(
        body: suspend MessageCommand<Arguments>.() -> Unit
    ): MessageCommand<Arguments> {
        val commandObj = MessageCommand<Arguments>(this)
        body.invoke(commandObj)

        return command(commandObj)
    }

    /**
     * Function for registering a custom command object.
     *
     * You can use this if you have a custom command subclass you need to register.
     *
     * @param commandObj MessageCommand object to register.
     */
    public open suspend fun <T : Arguments> command(commandObj: MessageCommand<T>): MessageCommand<T> {
        try {
            commandObj.validate()
            messageCommandsRegistry.add(commandObj)
            commands.add(commandObj)
        } catch (e: CommandRegistrationException) {
            logger.error(e) { "Failed to register command - $e" }
        } catch (e: InvalidCommandException) {
            logger.error(e) { "Failed to register command - $e" }
        }

        return commandObj
    }

    /**
     * DSL function for easily registering a slash command, with arguments.
     *
     * Use this in your setup function to register a slash command that may be executed on Discord.
     *
     * @param arguments Arguments builder (probably a reference to the class constructor).
     * @param body Builder lambda used for setting up the slash command object.
     */
    @ExtensionDSL
    public open suspend fun <T : Arguments> slashCommand(
        arguments: (() -> T),
        body: suspend SlashCommand<T>.() -> Unit
    ): SlashCommand<T> {
        val commandObj = SlashCommand(this, arguments)
        body.invoke(commandObj)

        return slashCommand(commandObj)
    }

    /**
     * DSL function for easily registering a slash command, without arguments.
     *
     * Use this in your setup function to register a slash command that may be executed on Discord.
     *
     * @param body Builder lambda used for setting up the slash command object.
     */
    @ExtensionDSL
    public open suspend fun slashCommand(
        body: suspend SlashCommand<out Arguments>.() -> Unit
    ): SlashCommand<out Arguments> {
        val commandObj = SlashCommand<Arguments>(this, null)
        body.invoke(commandObj)

        return slashCommand(commandObj)
    }

    /**
     * Function for registering a custom slash command object.
     *
     * You can use this if you have a custom slash command subclass you need to register.
     *
     * @param commandObj SlashCommand object to register.
     */
    public open suspend fun <T : Arguments> slashCommand(
        commandObj: SlashCommand<T>
    ): SlashCommand<T> {
        try {
            commandObj.validate()
            slashCommands.add(commandObj)
            slashCommandsRegistry.register(commandObj, commandObj.guild)
        } catch (e: CommandRegistrationException) {
            logger.error(e) { "Failed to register slash command - $e" }
        } catch (e: InvalidCommandException) {
            logger.error(e) { "Failed to register slash command - $e" }
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
    @ExtensionDSL
    public open suspend fun <T : Arguments> group(
        arguments: (() -> T)?,
        body: suspend GroupCommand<T>.() -> Unit
    ): GroupCommand<T> {
        val commandObj = GroupCommand(this, arguments)
        body.invoke(commandObj)

        return command(commandObj) as GroupCommand
    }

    /**
     * DSL function for easily registering a grouped command, without its own arguments.
     *
     * Use this in your setup function to register a group of commands.
     *
     * The body of the grouped command will be executed if there is no
     * matching subcommand.
     *
     * @param body Builder lambda used for setting up the command object.
     */
    @ExtensionDSL
    public open suspend fun group(body: suspend GroupCommand<Arguments>.() -> Unit): GroupCommand<Arguments> {
        val commandObj = GroupCommand<Arguments>(this)
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

        for (command in commands) {
            messageCommandsRegistry.remove(command)
        }

        eventHandlers.clear()
        commands.clear()

        if (error != null) {
            throw error
        }

        this.setState(ExtensionState.UNLOADED)
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

    /**
     * Define a check which must pass for the command to be executed. This check will be applied to all
     * slash commands in this extension.
     *
     * A command may have multiple checks - all checks must pass for the command to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to all slash commands in this extension.
     */
    public open fun slashCheck(vararg checks: suspend (InteractionCreateEvent) -> Boolean) {
        checks.forEach { slashCommandChecks.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to all slash commands in this extension.
     */
    @ExtensionDSL
    public open fun slashCheck(check: suspend (InteractionCreateEvent) -> Boolean) {
        slashCommandChecks.add(check)
    }

    /**
     * Define a check which must pass for the command to be executed. This check will be applied to all commands
     * in this extension.
     *
     * A command may have multiple checks - all checks must pass for the command to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to all commands in this extension.
     */
    @ExtensionDSL
    public open fun check(vararg checks: suspend (MessageCreateEvent) -> Boolean) {
        checks.forEach { commandChecks.add(it) }
    }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to all commands in this extension.
     */
    @ExtensionDSL
    public open fun check(check: suspend (MessageCreateEvent) -> Boolean) {
        commandChecks.add(check)
    }

    /**
     * Convenience function to create an `interactionButton` with a randomly-generated UUID.
     */
    @OptIn(KordPreview::class)
    public inline fun ActionRowBuilder.button(
        style: ButtonStyle,
        builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit
    ): Unit =
        interactionButton(style, UUID.randomUUID().toString(), builder)

    /**
     * Register an event handler for this specific button, to be fired when it's clicked.
     *
     * **Note:** Buttons will not be automatically disabled after [timeout]/[fireOnce] has been met.
     *
     * @param ackType Acknowledgement type, to ack automatically. Defaults to `EPHEMERAL`, set to `null` to disable.
     * @param timeout How long to wait before the bot stops waiting for button clicks, if needed.
     * @param fireOnce Whether to stop listening for clicks after the first one.
     * @param body Code to run after the click.
     */
    @OptIn(KordPreview::class)
    @ExtensionDSL
    public suspend fun ButtonBuilder.InteractionButtonBuilder.action(
        ackType: AutoAckType? = AutoAckType.EPHEMERAL,
        timeout: Long? = null,
        fireOnce: Boolean = true,
        body: suspend ComponentInteractionContext.() -> Unit
    ) {
        var isRunning = false
        var delayJob: Job? = null

        event<InteractionCreateEvent> {
            // To seconds!
            @Suppress("MagicNumber")
fun delay() = kord.launch {
                delay(timeout!! * 1000)

                if (!isRunning && job?.isCancelled != true) {
                    job?.cancel()
                    bot.removeEventHandler(this@event)
                }
            }

            check {
                val interaction = it.interaction as? ComponentInteraction

                interaction != null && interaction.componentId == customId
            }

            this.action {
                isRunning = true

                val firstBreadcrumb = if (sentry.enabled) {
                    val channel = channelFor(event)
                    val guild = guildFor(event)?.asGuildOrNull()

                    val data = mutableMapOf<String, Serializable>()

                    if (channel != null) {
                        data["channel"] = when (channel) {
                            is DmChannel -> "Private Message (${channel.id.asString})"
                            is GuildMessageChannel -> "#${channel.name} (${channel.id.asString})"

                            else -> channel.id.asString
                        }
                    }

                    if (guild != null) {
                        data["guild"] = "${guild.name} (${guild.id.asString})"
                    }

                    sentry.createBreadcrumb(
                        category = "interaction",
                        type = "user",
                        message = "Interaction for component \"$customId\" received.",
                        data = data
                    )
                } else {
                    null
                }

                val interaction = event.interaction as ComponentInteraction

                val response = when (ackType) {
                    AutoAckType.EPHEMERAL -> interaction.acknowledgeEphemeral()
                    AutoAckType.PUBLIC -> interaction.acknowledgePublic()

                    else -> null
                }

                suspend fun respond(text: String) =
                    when (response) {
                        is EphemeralInteractionResponseBehavior -> response.followUp(text)

                        is PublicInteractionResponseBehavior -> response.followUp {
                            content = text
                        }

                        else -> interaction.respondEphemeral(text)
                    }

                val interactionContext = ComponentInteractionContext(
                    this@Extension,
                    event,
                    response,
                    interaction
                )

                @Suppress("TooGenericExceptionCaught")
                try {
                    body(interactionContext)
                } catch (e: CommandException) {
                    respond(e.toString())
                } catch (t: Throwable) {
                    if (sentry.enabled) {
                        logger.debug { "Submitting error to sentry." }

                        lateinit var sentryId: SentryId

                        val user = userFor(event)?.asUserOrNull()
                        val channel = channelFor(event)?.asChannelOrNull()

                        Sentry.withScope {
                            if (user != null) {
                                it.user(user)
                            }

                            it.tag("private", "false")

                            if (channel is DmChannel) {
                                it.tag("private", "true")
                            }

                            it.tag("extension", this@Extension.name)
                            it.addBreadcrumb(firstBreadcrumb!!)

                            interactionContext.breadcrumbs.forEach(it::addBreadcrumb)

                            sentryId = Sentry.captureException(t, "Component interaction execution failed.")

                            logger.debug { "Error submitted to Sentry: $sentryId" }
                        }

                        sentry.addEventId(sentryId)

                        logger.error(t) { "Error thrown during component interaction" }

                        if (extension.bot.extensions.containsKey("sentry")) {
                            respond(
                                interactionContext.translate(
                                    "commands.error.user.sentry.slash",
                                    null,
                                    replacements = arrayOf(sentryId)
                                )
                            )
                        } else {
                            respond(
                                interactionContext.translate("commands.error.user")
                            )
                        }
                    } else {
                        logger.error(t) { "Error thrown during component interaction" }

                        respond(interactionContext.translate("commands.error.user", null))
                    }
                }

                if (fireOnce) {
                    delayJob?.cancel()
                    bot.removeEventHandler(this@event)
                    this@event.job?.cancel()
                } else {
                    isRunning = false

                    delayJob?.cancel()
                    delayJob = delay()
                }
            }

            if (timeout != null) {
                delayJob = delay()
            }
        }
    }
}

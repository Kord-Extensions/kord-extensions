package com.kotlindiscord.kord.extensions

import com.gitlab.kordlib.common.entity.Status
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.event.gateway.DisconnectEvent
import com.gitlab.kordlib.core.event.gateway.ReadyEvent
import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.on
import com.gitlab.kordlib.gateway.Intents
import com.gitlab.kordlib.gateway.RequestGuildMembers
import com.gitlab.kordlib.gateway.builder.PresenceBuilder
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.events.ExtensionEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.HelpExtension
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import net.time4j.tz.repo.TZDATA
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * An extensible bot, wrapping a Kord instance.
 *
 * This is your jumping-off point. ExtensibleBot provides a system for managing extensions, commands and event
 * handlers. Either subclass ExtensibleBot or use it as-is if it suits your needs.
 *
 * @param addHelpExtension Whether to automatically install the bundled help command extension.
 * @param commandThreads Number of threads to use for command execution. Defaults to twice the number of CPU threads.
 * @param invokeCommandOnMention Whether to invoke commands that are prefixed with a mention, as well as the prefix.
 * @param messageCacheSize How many previous messages to store - default to 10,000.
 * @param prefix The command prefix, for command invocations on Discord.
 * @param token The Discord bot's login token.
 * @param guildsToFill Guild IDs (as strings) to request all members for on connect. Set to null for all guilds.
 * @param fillPresences Whether to request presences from the members retrieved by [guildsToFill].
 */
open class ExtensibleBot(
    private val token: String,
    open val prefix: String,

    open val addHelpExtension: Boolean = true,
    open val invokeCommandOnMention: Boolean = true,
    open val messageCacheSize: Int = 10_000,
    open val commandThreads: Int = Runtime.getRuntime().availableProcessors() * 2,
    open val guildsToFill: List<String>? = listOf(),
    open val fillPresences: Boolean? = null
) {
    /**
     * @suppress
     */
    open lateinit var kord: Kord  // Kord doesn't allow us to inherit the class, let's wrap it instead

    /**
     * A list of all registered commands.
     */
    open val commands: MutableList<Command> = mutableListOf()

    /**
     * A list of all registered event handlers.
     */
    open val eventHandlers: MutableList<EventHandler<out Any>> = mutableListOf()

    /**
     * A map of the names of all loaded [Extension]s to their instances.
     */
    open val extensions: MutableMap<String, Extension> = mutableMapOf()

    /** @suppress **/
    open val eventPublisher = BroadcastChannel<Any>(1)

    /** @suppress **/
    open var initialized: Boolean = false

    /** A [Flow] representing a combined set of Kord events and Kord Extensions events. **/
    open val events get() = eventPublisher.asFlow().buffer(Channel.UNLIMITED)

    /** @suppress **/
    open val logger = KotlinLogging.logger {}

    /** @suppress **/
    open val commandThreadPool = Executors
        .newFixedThreadPool(commandThreads)
        .asCoroutineDispatcher()

    init {
        TZDATA.init()  // Set up time4j, since we use it
    }

    /**
     * This function kicks off the process, by setting up the bot and having it login.
     */
    open suspend fun start(
        presenceBuilder: PresenceBuilder.() -> Unit = { status = Status.Online },
        intents: (Intents.IntentsBuilder.() -> Unit)? = null
    ) {
        kord = Kord(token) {
            cache {
                messages(lruCache(messageCacheSize))
            }

            if (intents != null) {
                this.intents(intents)
            }
        }

        registerListeners()
        addDefaultExtensions()

        kord.on<Event> {
            val event = this

            kord.launch {
                eventPublisher.send(event)
            }
        }

        kord.login(presenceBuilder)
    }

    /** This function sets up all of the bot's default event listeners. **/
    open suspend fun registerListeners() {
        on<GuildCreateEvent> {
            if (guildsToFill == null || guild.id.value in guildsToFill!!) {
                logger.info { "Requesting members for guild: ${guild.name}" }

                gateway.send(
                    RequestGuildMembers(
                        guildId = listOf(guild.id.value),
                        presences = fillPresences
                    )
                )
            }
        }

        on<DisconnectEvent.DiscordCloseEvent> {
            logger.warn { "Disconnected: $closeCode" }
        }

        on<ReadyEvent> {
            if (!initialized) {  // We do this because a reconnect will cause this event to happen again.
                for (extension in extensions.keys) {
                    @Suppress("TooGenericExceptionCaught")  // Anything could happen here
                    try {
                        loadExtension(extension)
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to set up '$extension' extension." }
                    }
                }

                initialized = true

                // Since the setup method is called after the first ReadyEvent, all ReadyEvent handlers need to be
                // manually called here in order to make sure they fire as expected. However, since the setup method
                // has now been run, they'll be properly subscribed next time.
                for (handler in eventHandlers) {
                    if (handler.type == ReadyEvent::class) {
                        @Suppress("TooGenericExceptionCaught")  // Anything could happen here
                        try {
                            val event = this

                            kord.launch {
                                (handler as EventHandler<ReadyEvent>)  // We know it wants a ReadyEvent already
                                    .call(event)
                            }
                        } catch (e: Exception) {
                            logger.error(e) {
                                "ReadyEvent handler in '${handler.extension.name}' extension threw an exception."
                            }
                        }
                    }
                }
            }

            logger.info { "Ready!" }
        }

        on<MessageCreateEvent> {
            var commandName: String? = null
            var parts = parseMessage(this.message)

            if (parts.isEmpty()) {
                // Empty message.
                return@on
            }

            if (parts.size == 1) {
                // It's just the command with no arguments

                if (parts[0].startsWith(prefix)) {
                    commandName = parts[0]
                    parts = arrayOf()
                } else {
                    // Doesn't start with the right prefix
                    return@on
                }
            } else {
                val mention = kord.getSelf().mention

                when {
                    parts[0].startsWith(prefix) -> {
                        // Command with args
                        commandName = parts[0]
                        parts = parts.sliceArray(1 until parts.size)
                    }
                    invokeCommandOnMention && parts[0] == mention -> {
                        // Command with a mention; first part is exactly the mention
                        commandName = parts[1]

                        parts = if (parts.size > 2) {
                            parts.sliceArray(2 until parts.size)
                        } else {
                            arrayOf()
                        }
                    }
                    invokeCommandOnMention && parts[0].startsWith(mention) -> {
                        // Command with a mention; no space between mention and command
                        commandName = parts[0].slice(mention.length until parts[0].length)
                        parts = parts.sliceArray(1 until parts.size)
                    }
                }
            }

            if (commandName == null || commandName == prefix) {
                return@on  // After all that, we couldn't find a command.
            }

            if (commandName.startsWith(prefix)) {
                commandName = commandName.slice(prefix.length until commandName.length)
            }

            commandName = commandName.toLowerCase()

            val command = commands.firstOrNull { it.name == commandName }
                ?: commands.firstOrNull { it.aliases.contains(commandName) }

            val event = this

            commandThreadPool.invoke {
                command?.call(event, parts)
            }
        }
    }

    /** This function adds all of the default extensions when the bot is being set up. **/
    open fun addDefaultExtensions() {
        if (addHelpExtension) {
            logger.info { "Adding help extension." }
            addExtension(HelpExtension::class)
        }
    }

    /**
     * Subscribe to an event. You shouldn't need to use this directly, but it's here just in case.
     *
     * You can subscribe to any type, realistically - but this is intended to be used only with Kord
     * [Event] subclasses, and our own [ExtensionEvent]s.
     *
     * @param T Type of event to subscribe to.
     * @param scope Coroutine scope to run the body of your callback under.
     * @param consumer The callback to run when the event is fired.
     */
    inline fun <reified T : Any> on(
        launch: Boolean = true,
        scope: CoroutineScope = this.kord,
        noinline consumer: suspend T.() -> Unit
    ) = events.buffer(Channel.UNLIMITED).filterIsInstance<T>().onEach {
        runCatching { if (launch) kord.launch { consumer(it) } else consumer(it) }
            .onFailure { logger.catching(it) }
    }.catch { logger.catching(it) }.launchIn(scope)

    /**
     * @suppress
     */
    open suspend fun send(event: Event) {
        eventPublisher.send(event)
    }

    /**
     * @suppress
     */
    open suspend fun send(event: ExtensionEvent) {
        eventPublisher.send(event)
    }

    /**
     * Install an [Extension] to this bot.
     *
     * This function will instantiate the given extension class, call its [Extension.setup]
     * function, and store it in the [extensions] map.
     *
     * @param extension The [Extension] class to install.
     * @throws InvalidExtensionException Thrown if the extension has no primary constructor.
     */
    @Throws(InvalidExtensionException::class)
    open fun addExtension(extension: KClass<out Extension>) {
        val ctor = extension.primaryConstructor ?: throw InvalidExtensionException(extension, "No primary constructor")

        val extensionObj = ctor.call(this)

        extensions[extensionObj.name] = extensionObj
    }

    /**
     * Reload an installed [Extension] from this bot, by name.
     *
     * This function **does not** remove the extension object - it simply
     * removes its event handlers and commands. Unloaded extensions can
     * be loaded again by calling [ExtensibleBot.loadExtension].
     *
     * This function simply returns if the extension isn't found.
     *
     * @param extension The name of the [Extension] to unload.
     */
    @Throws(InvalidExtensionException::class)
    open suspend fun loadExtension(extension: String) {
        val extensionObj = extensions[extension] ?: return

        if (!extensionObj.loaded) {
            extensionObj.doSetup()
        }
    }

    /**
     * Unload an installed [Extension] from this bot, by name.
     *
     * This function **does not** remove the extension object - it simply
     * removes its event handlers and commands. Unloaded extensions can
     * be loaded again by calling [ExtensibleBot.loadExtension].
     *
     * This function simply returns if the extension isn't found.
     *
     * @param extension The name of the [Extension] to unload.
     */
    open suspend fun unloadExtension(extension: String) {
        val extensionObj = extensions[extension] ?: return

        if (extensionObj.loaded) {
            extensionObj.doUnload()
        }
    }

    /**
     * Directly register a [Command] to this bot.
     *
     * Generally speaking, you shouldn't call this directly - instead, create an [Extension] and
     * call the [Extension.command] function in your [Extension.setup] function.
     *
     * This function will throw a [CommandRegistrationException] if the command has already been registered, if
     * a command with the same name exists, or if a command with one of the same aliases exists.
     *
     * @param command The command to be registered.
     * @throws CommandRegistrationException Thrown if the command could not be registered.
     */
    @Throws(CommandRegistrationException::class)
    open fun addCommand(command: Command) {
        val existingCommand = commands.any { it.name == command.name }
        val existingAlias: String? = commands.flatMap {
            it.aliases.toList()
        }.firstOrNull { command.aliases.contains(it) }

        if (existingCommand) {
            throw CommandRegistrationException(
                command.name,
                "Command with this name already registered in '${command.extension.name}' extension."
            )
        }

        if (existingAlias != null) {
            throw CommandRegistrationException(
                command.name,
                "Command with alias '$existingAlias' already registered in '${command.extension.name}' extension."
            )
        }

        if (commands.contains(command)) {
            throw CommandRegistrationException(
                command.name,
                "Command already registered in '${command.extension.name}' extension."
            )
        }

        commands.add(command)
    }

    /**
     * Directly remove a registered [Command] from this bot.
     *
     * This function is used when extensions are unloaded, in order to clear out their commands.
     * No exception is thrown if the command wasn't registered.
     *
     * @param command The command to be removed.
     */
    open fun removeCommand(command: Command) = commands.remove(command)

    /**
     * Directly register an [EventHandler] to this bot.
     *
     * Generally speaking, you shouldn't call this directly - instead, create an [Extension] and
     * call the [Extension.event] function in your [Extension.setup] function.
     *
     * This function will throw an [EventHandlerRegistrationException] if the event handler has already been registered.
     *
     * @param handler The event handler to be registered.
     * @throws EventHandlerRegistrationException Thrown if the event handler could not be registered.
     */
    @Throws(EventHandlerRegistrationException::class)
    inline fun <reified T : Any> addEventHandler(handler: EventHandler<T>): Job {
        if (eventHandlers.contains(handler)) {
            throw EventHandlerRegistrationException(
                "Event handler already registered in '${handler.extension.name}' extension."
            )
        }

        val job = on<T> { handler.call(this) }

        eventHandlers.add(handler)

        return job
    }

    /**
     * Directly remove a registered [EventHandler] from this bot.
     *
     * This function is used when extensions are unloaded, in order to clear out their event handlers.
     * No exception is thrown if the event handler wasn't registered.
     *
     * @param handler The event handler to be removed.
     */
    open fun removeEventHandler(handler: EventHandler<out Any>) = eventHandlers.remove(handler)
}

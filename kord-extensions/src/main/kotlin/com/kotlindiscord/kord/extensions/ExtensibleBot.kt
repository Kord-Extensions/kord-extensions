package com.kotlindiscord.kord.extensions

import com.kotlindiscord.kord.extensions.builders.StartBuilder
import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.events.ExtensionEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.HelpExtension
import com.kotlindiscord.kord.extensions.extensions.SentryExtension
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.slash_commands.SlashCommandRegistry
import com.kotlindiscord.kord.extensions.utils.module
import com.kotlindiscord.kord.extensions.utils.parse
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.requestMembers
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.DisconnectEvent
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import mu.KLogger
import mu.KotlinLogging
import net.time4j.tz.repo.TZDATA
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.dsl.koinApplication
import org.koin.logger.slf4jLogger
import java.io.File
import java.util.*
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
 * @param addSentryExtension Whether to automatically install the bundled Sentry command extension.
 * @param commandThreads Number of threads to use for command execution. Defaults to twice the number of CPU threads.
 * @param invokeCommandOnMention Whether to invoke commands that are prefixed with a mention, as well as the prefix.
 * @param messageCacheSize How many previous messages to store - default to 10,000.
 * @param prefix The command prefix, for command invocations on Discord.
 * @param token The Discord bot's login token.
 * @param guildsToFill Guild IDs to request all members for on connect. Set to null for all guilds, omit for none.
 * @param fillPresences Whether to request presences from the members retrieved by [guildsToFill].
 * @param koinLogLevel Logging level Koin should use, defaulting to INFO.
 */
public open class ExtensibleBot(
    private val token: String,

    public open val prefix: String,

    public open val addHelpExtension: Boolean = true,
    public open val addSentryExtension: Boolean = true,
    public open val invokeCommandOnMention: Boolean = true,
    public open val messageCacheSize: Int = 10_000,
    public open val commandThreads: Int = Runtime.getRuntime().availableProcessors() * 2,
    public open val guildsToFill: List<Snowflake>? = listOf(),
    public open val fillPresences: Boolean? = null,
    public open val koinLogLevel: Level = Level.ERROR
) {
    /**
     * @suppress
     */
    public open lateinit var kord: Kord  // Kord doesn't allow us to inherit the class, let's wrap it instead

    /**
     * Sentry adapter, for working with Sentry.
     */
    public open val sentry: SentryAdapter = SentryAdapter()

    /**
     * A list of all registered commands.
     */
    public open val commands: MutableList<MessageCommand> = mutableListOf()

    /**
     * A list of all registered event handlers.
     */
    public open val eventHandlers: MutableList<EventHandler<out Any>> = mutableListOf()

    /**
     * A map of the names of all loaded [Extension]s to their instances.
     */
    public open val extensions: MutableMap<String, Extension> = mutableMapOf()

    /** @suppress **/
    public open val eventPublisher: BroadcastChannel<Any> = BroadcastChannel(1)

    /** @suppress **/
    public open var initialized: Boolean = false

    /** A [Flow] representing a combined set of Kord events and Kord Extensions events. **/
    public open val events: Flow<Any> get() = eventPublisher.asFlow().buffer(Channel.UNLIMITED)

    /** @suppress **/
    public open val logger: KLogger = KotlinLogging.logger {}

    /** @suppress **/
    public open val commandThreadPool: ExecutorCoroutineDispatcher by lazy {
        Executors
            .newFixedThreadPool(commandThreads)
            .asCoroutineDispatcher()
    }

    /** Configured Koin application. **/
    public open val koinApp: KoinApplication = koinApplication {
        slf4jLogger(koinLogLevel)
        environmentProperties()

        if (File("koin.properties").exists()) {
            fileProperties("koin.properties")
        }

        modules()
    }

    /** Koin context, specific to this bot. Make use of it instead of a global Koin context, if you need Koin. **/
    public val koin: Koin = koinApp.koin

    /** Slash command registry, keeps track of and executes slash commands. **/
    public open val slashCommands: SlashCommandRegistry by koin.inject<SlashCommandRegistry>()

    init {
        TZDATA.init()  // Set up time4j

        koin.module { single { this@ExtensibleBot } }
        koin.module { single { sentry } }
        koin.module { single { SlashCommandRegistry(this@ExtensibleBot) } }
    }

    /**
     * This function kicks off the process, by setting up the bot and having it login.
     */
    public open suspend fun start(builder: suspend StartBuilder.() -> Unit = {}) {
        val startBuilder = StartBuilder()

        builder.invoke(startBuilder)

        kord = Kord(token) {
            cache {
                messages(lruCache(messageCacheSize))
            }

            if (startBuilder.intentsBuilder != null) {
                this.intents = Intents(startBuilder.intentsBuilder!!)
            }
        }

        koin.module { single { kord } }

        registerListeners()
        addDefaultExtensions()

        kord.on<Event> {
            val event = this

            kord.launch {
                send(event)
            }
        }

        kord.login(startBuilder.presenceBuilder)
    }

    /** This function sets up all of the bot's default event listeners. **/
    @OptIn(PrivilegedIntent::class)
    public open suspend fun registerListeners() {
        on<GuildCreateEvent> {
            if (guildsToFill == null || guildsToFill!!.contains(guild.id)) {
                logger.info { "Requesting members for guild: ${guild.name}" }

                guild.requestMembers {
                    presences = fillPresences
                    requestAllMembers()
                }.collect()
            }
        }

        on<DisconnectEvent.DiscordCloseEvent> {
            logger.warn { "Disconnected: $closeCode" }
        }

        on<InteractionCreateEvent> {
            slashCommands.handle(this)
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

                slashCommands.syncAll()
            }

            logger.info { "Ready!" }
        }

        on<MessageCreateEvent> {
            var commandName: String? = null
            var parts = message.parse()

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
                        // MessageCommand with args
                        commandName = parts[0]
                        parts = parts.sliceArray(1 until parts.size)
                    }
                    invokeCommandOnMention && parts[0] == mention -> {
                        // MessageCommand with a mention; first part is exactly the mention
                        commandName = parts[1]

                        parts = if (parts.size > 2) {
                            parts.sliceArray(2 until parts.size)
                        } else {
                            arrayOf()
                        }
                    }
                    invokeCommandOnMention && parts[0].startsWith(mention) -> {
                        // MessageCommand with a mention; no space between mention and command
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

            if (commandName.contains("\n")) {
                val split = commandName.split("\n", limit = 2)

                commandName = split.first()

                parts = if (parts.isEmpty()) {
                    arrayOf("\n${split.last()}")
                } else {
                    arrayOf("\n${split.last()}", *parts)
                }
            }

            commandName = commandName.toLowerCase()

            val command = commands.firstOrNull { it.name == commandName }
                ?: commands.firstOrNull { it.aliases.contains(commandName) }

            val event = this

            commandThreadPool.invoke {
                command?.call(event, commandName, parts)
            }
        }
    }

    /** This function adds all of the default extensions when the bot is being set up. **/
    public open fun addDefaultExtensions() {
        if (addHelpExtension) {
            logger.debug { "Adding help extension." }
            addExtension(HelpExtension::class)
        }

        if (addSentryExtension) {
            logger.debug { "Adding sentry extension." }
            addExtension(SentryExtension::class)
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
    public inline fun <reified T : Any> on(
        launch: Boolean = true,
        scope: CoroutineScope = this.kord,
        noinline consumer: suspend T.() -> Unit
    ): Job = events.buffer(Channel.UNLIMITED).filterIsInstance<T>().onEach {
        runCatching { if (launch) kord.launch { consumer(it) } else consumer(it) }
            .onFailure { logger.catching(it) }
    }.catch { logger.catching(it) }.launchIn(scope)

    /**
     * @suppress
     */
    public suspend inline fun send(event: Event) {
        eventPublisher.send(event)
    }

    /**
     * @suppress
     */
    public suspend inline fun send(event: ExtensionEvent) {
        eventPublisher.send(event)
    }

    /**
     * Install an [Extension] to this bot.
     *
     * This function will instantiate the given extension classand store the resulting extension object, ready to be
     * set up when the next [ReadyEvent] happens.
     *
     * @param extension The [Extension] class to install.
     * @throws InvalidExtensionException Thrown if the extension has no primary constructor.
     */
    @Throws(InvalidExtensionException::class)
    @Deprecated(
        "Use the newer addExtension(builder) function instead. It's shorter and more flexible.",
        ReplaceWith("this.addExtension(builder)", "com.kotlindiscord.kord.extensions.ExtensibleBot"),
        DeprecationLevel.WARNING
    )
    public open fun addExtension(extension: KClass<out Extension>) {
        val ctor = extension.primaryConstructor ?: throw InvalidExtensionException(extension, "No primary constructor")

        val extensionObj = ctor.call(this)

        extensions[extensionObj.name] = extensionObj
    }

    /**
     * Install an [Extension] to this bot.
     *
     * This function will call the given builder function and store the resulting extension object, ready to be
     * set up when the next [ReadyEvent] happens.
     *
     * @param builder Builder function (or extension constructor) that takes an [ExtensibleBot] instance and
     * returns an [Extension].
     */
    @Throws(InvalidExtensionException::class)
    public open fun addExtension(builder: (ExtensibleBot) -> Extension) {
        val extensionObj = builder.invoke(this)

        extensions[extensionObj.name] = extensionObj
    }

    /**
     * Reload an unloaded [Extension] from this bot, by name.
     *
     * This function **does not** create a new extension object - it simply
     * calls its `setup()` function. Loaded extensions can
     * be unload again by calling [unloadExtension].
     *
     * This function simply returns if the extension isn't found.
     *
     * @param extension The name of the [Extension] to unload.
     */
    @Throws(InvalidExtensionException::class)
    public open suspend fun loadExtension(extension: String) {
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
     * be loaded again by calling [loadExtension].
     *
     * This function simply returns if the extension isn't found.
     *
     * @param extension The name of the [Extension] to unload.
     */
    public open suspend fun unloadExtension(extension: String) {
        val extensionObj = extensions[extension] ?: return

        if (extensionObj.loaded) {
            extensionObj.doUnload()
        }
    }

    /**
     * Directly register a [MessageCommand] to this bot.
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
    public open fun addCommand(command: MessageCommand) {
        val existingCommand = commands.any { it.name == command.name }
        val existingAlias: String? = commands.flatMap {
            it.aliases.toList()
        }.firstOrNull { command.aliases.contains(it) }

        if (existingCommand) {
            throw CommandRegistrationException(
                command.name,
                "MessageCommand with this name already registered in '${command.extension.name}' extension."
            )
        }

        if (existingAlias != null) {
            throw CommandRegistrationException(
                command.name,
                "MessageCommand with alias '$existingAlias' already registered in '${command.extension.name}' extension."
            )
        }

        if (commands.contains(command)) {
            throw CommandRegistrationException(
                command.name,
                "MessageCommand already registered in '${command.extension.name}' extension."
            )
        }

        commands.add(command)
    }

    /**
     * Directly remove a registered [MessageCommand] from this bot.
     *
     * This function is used when extensions are unloaded, in order to clear out their commands.
     * No exception is thrown if the command wasn't registered.
     *
     * @param command The command to be removed.
     */
    public open fun removeCommand(command: MessageCommand): Boolean = commands.remove(command)

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
    public inline fun <reified T : Any> addEventHandler(handler: EventHandler<T>): Job {
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
    public open fun removeEventHandler(handler: EventHandler<out Any>): Boolean = eventHandlers.remove(handler)
}

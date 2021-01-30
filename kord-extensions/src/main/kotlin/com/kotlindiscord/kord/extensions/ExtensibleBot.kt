package com.kotlindiscord.kord.extensions

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandRegistry
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.events.ExtensionEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ExtensionState
import com.kotlindiscord.kord.extensions.extensions.HelpExtension
import com.kotlindiscord.kord.extensions.extensions.SentryExtension
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.utils.module
import com.kotlindiscord.kord.extensions.utils.parse
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
 * You shouldn't construct this class directly - use the builder pattern via the companion object's `invoke` method:
 * `ExtensibleBot(token) { extensions { add(::MyExtension) } }`.
 *
 * @param settings Bot builder object containing the bot's settings.
 * @param token Token for connecting to Discord.
 */
public open class ExtensibleBot(public val settings: ExtensibleBotBuilder, private val token: String) {
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
    public open val commands: MutableList<MessageCommand<out Arguments>> = mutableListOf()

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

    /** A [Flow] representing a combined set of Kord events and Kord Extensions events. **/
    public open val events: Flow<Any> get() = eventPublisher.asFlow().buffer(Channel.UNLIMITED)

    /** @suppress **/
    public open var initialized: Boolean = false

    /** @suppress **/
    public open val logger: KLogger = KotlinLogging.logger {}

    /** @suppress **/
    public open val commandThreadPool: ExecutorCoroutineDispatcher by lazy {
        Executors
            .newFixedThreadPool(settings.commandsBuilder.threads)
            .asCoroutineDispatcher()
    }

    /** Configured Koin application. **/
    public open val koinApp: KoinApplication = koinApplication {
        slf4jLogger(settings.koinLogLevel)
        environmentProperties()

        if (File("koin.properties").exists()) {
            fileProperties("koin.properties")
        }

        modules()
    }

    /** Quick access to the bot's configured command prefix. **/
    public open val prefix: String get() = settings.commandsBuilder.prefix

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

    /** @suppress Function that sets up the bot early on, called by the builder. **/
    public open suspend fun setup() {
        kord = Kord(token) {
            cache {
                settings.cacheBuilder.builder.invoke(this, it)
            }

            if (settings.intentsBuilder != null) {
                this.intents = Intents(settings.intentsBuilder!!)
            }
        }

        koin.module { single { kord } }

        registerListeners()
        addDefaultExtensions()

        kord.on<Event> {
            kord.launch {
                send(this@on)
            }
        }
    }

    /** Start up the bot and log into Discord. **/
    public open suspend fun start(): Unit = kord.login(settings.presenceBuilder)

    /** This function sets up all of the bot's default event listeners. **/
    @OptIn(PrivilegedIntent::class)
    public open suspend fun registerListeners() {
        on<GuildCreateEvent> {
            if (
                settings.membersBuilder.guildsToFill == null ||
                settings.membersBuilder.guildsToFill!!.contains(guild.id)
            ) {
                logger.info { "Requesting members for guild: ${guild.name}" }

                guild.requestMembers {
                    presences = settings.membersBuilder.fillPresences
                    requestAllMembers()
                }.collect()
            }
        }

        on<DisconnectEvent.DiscordCloseEvent> {
            logger.warn { "Disconnected: $closeCode" }
        }

        if (settings.commandsBuilder.slashCommands) {
            on<InteractionCreateEvent> {
                slashCommands.handle(this)
            }
        }

        on<ReadyEvent> {
            if (!initialized) {  // We do this because a reconnect will cause this event to happen again.
                initialized = true

                if (settings.commandsBuilder.slashCommands) {
                    slashCommands.syncAll()
                } else {
                    logger.info {
                        "Slash command support is disabled - set `handleSlashCommands` to `true` if " +
                            "you want to use them."
                    }
                }
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
                    settings.commandsBuilder.invokeOnMention && parts[0] == mention -> {
                        // MessageCommand with a mention; first part is exactly the mention
                        commandName = parts[1]

                        parts = if (parts.size > 2) {
                            parts.sliceArray(2 until parts.size)
                        } else {
                            arrayOf()
                        }
                    }
                    settings.commandsBuilder.invokeOnMention && parts[0].startsWith(mention) -> {
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
                    arrayOf("\n${split.last()}") + parts
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
    public open suspend fun addDefaultExtensions() {
        if (settings.extensionsBuilder.help) {
            this.addExtension(::HelpExtension)
        }

        if (settings.extensionsBuilder.sentry) {
            this.addExtension(::SentryExtension)
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
        DeprecationLevel.ERROR
    )
    public open suspend fun addExtension(extension: KClass<out Extension>) {
        val ctor = extension.primaryConstructor ?: throw InvalidExtensionException(extension, "No primary constructor")

        val extensionObj = ctor.call(this)

        addExtension { extensionObj }
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
    public open suspend fun addExtension(builder: (ExtensibleBot) -> Extension) {
        val extensionObj = builder.invoke(this)

        extensions[extensionObj.name] = extensionObj
        loadExtension(extensionObj.name)

        if (!extensionObj.loaded) {
            logger.warn { "Failed to set up extension: ${extensionObj.name}" }
        } else {
            logger.debug { "Loaded extension: ${extensionObj.name}" }
        }
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
    public open fun addCommand(command: MessageCommand<out Arguments>) {
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
                "MessageCommand with alias '$existingAlias' already registered in '${command.extension.name}' " +
                    "extension."
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
    public open fun removeCommand(command: MessageCommand<out Arguments>): Boolean = commands.remove(command)

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

    public companion object {
        /**
         * DSL function for creating a bot instance. Use the bot class like a builder!
         *
         * `ExtensibleBot(token) { extensions { add(::MyExtension) } }`
         */
        public suspend operator fun invoke(token: String, builder: ExtensibleBotBuilder.() -> Unit): ExtensibleBot =
            ExtensibleBotBuilder().apply(builder).build(token)

        /**
         * DSL function for creating a bot instance. Token only. This is provided for completeness, but you probably
         * want to configure your bot using the other version of this function.
         */
        public suspend operator fun invoke(token: String): ExtensibleBot =
            this(token) {}
    }
}

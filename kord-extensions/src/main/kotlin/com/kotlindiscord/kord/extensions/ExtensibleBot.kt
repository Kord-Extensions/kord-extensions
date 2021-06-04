package com.kotlindiscord.kord.extensions

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.MessageCommandRegistry
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandRegistry
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.events.ExtensionEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.impl.HelpExtension
import com.kotlindiscord.kord.extensions.extensions.impl.SentryExtension
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.utils.loadModule
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.bind

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
public open class ExtensibleBot(public val settings: ExtensibleBotBuilder, private val token: String) : KoinComponent {
    /**
     * @suppress
     */
    @Deprecated(
        "Use Koin to get this instead. This will be private in future.",
        ReplaceWith(
            "getKoin().get<Kord>()",

            "com.kotlindiscord.kord.extensions.utils.getKoin",
            "dev.kord.core.Kord"
        ),
        level = DeprecationLevel.WARNING
    )
    public val kord: Kord by inject()

    /**
     * Sentry adapter, for working with Sentry.
     */
    @Deprecated(
        "Use Koin to get this instead. This will be removed in future.",
        ReplaceWith(
            "getKoin().get<SentryAdapter>()",

            "com.kotlindiscord.kord.extensions.utils.getKoin",
            "com.kotlindiscord.kord.extensions.sentry.SentryAdapter"
        ),
        level = DeprecationLevel.ERROR
    )
    public open val sentry: SentryAdapter by inject()

    /** Translations provider, for retrieving translations. **/
    @Deprecated(
        "Use Koin to get this instead. This will be removed in future.",
        ReplaceWith(
            "getKoin().get<TranslationsProvider>()",

            "com.kotlindiscord.kord.extensions.utils.getKoin",
            "com.kotlindiscord.kord.extensions.i18n.TranslationsProvider"
        ),
        level = DeprecationLevel.ERROR
    )
    public val translationsProvider: TranslationsProvider by inject()

    /** Message command registry, keeps track of and executes message commands. **/
    @Deprecated(
        "Use Koin to get this instead. This will be made private in future.",
        ReplaceWith(
            "getKoin().get<MessageCommandRegistry>()",

            "com.kotlindiscord.kord.extensions.utils.getKoin",
            "com.kotlindiscord.kord.extensions.commands.MessageCommandRegistry"
        ),
        level = DeprecationLevel.WARNING
    )
    public open val messageCommands: MessageCommandRegistry by inject()

    /** Slash command registry, keeps track of and executes slash commands. **/
    @Deprecated(
        "Use Koin to get this instead. This will be made private in future.",
        ReplaceWith(
            "getKoin().get<SlashCommandRegistry>()",

            "com.kotlindiscord.kord.extensions.utils.getKoin",
            "com.kotlindiscord.kord.extensions.commands.slash.SlashCommandRegistry"
        ),
        level = DeprecationLevel.WARNING
    )
    public open val slashCommands: SlashCommandRegistry by inject()

    /**
     * A list of all registered event handlers.
     */
    public open val eventHandlers: MutableList<EventHandler<out Any>> = mutableListOf()

    /**
     * A map of the names of all loaded [Extension]s to their instances.
     */
    public open val extensions: MutableMap<String, Extension> = mutableMapOf()

    /** @suppress **/
    public open val eventPublisher: MutableSharedFlow<Any> = MutableSharedFlow()

    /** A [Flow] representing a combined set of Kord events and Kord Extensions events. **/
    public open val events: SharedFlow<Any> = eventPublisher.asSharedFlow()

    /** @suppress **/
    public open var initialized: Boolean = false

    /** @suppress **/
    public open val logger: KLogger = KotlinLogging.logger {}

    /** @suppress Function that sets up the bot early on, called by the builder. **/
    public open suspend fun setup() {
        val kord = Kord(token) {
            cache {
                settings.cacheBuilder.builder.invoke(this, it)
            }

            if (settings.intentsBuilder != null) {
                this.intents = Intents(settings.intentsBuilder!!)
            }

            enableShutdownHook = settings.hooksBuilder.kordShutdownHook
        }

        loadModule { single { kord } bind Kord::class }

        settings.cacheBuilder.dataCacheBuilder.invoke(kord, kord.cache)

        registerListeners()
        addDefaultExtensions()

        kord.on<Event> {
            kord.launch {
                send(this@on)
            }
        }
    }

    /** Start up the bot and log into Discord. **/
    public open suspend fun start() {
        settings.hooksBuilder.runBeforeStart(this)

        getKoin().get<Kord>().login(settings.presenceBuilder)
    }

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

        on<ReadyEvent> {
            if (!initialized) {  // We do this because a reconnect will cause this event to happen again.
                initialized = true

                if (settings.slashCommandsBuilder.enabled) {
                    getKoin().get<SlashCommandRegistry>().syncAll()
                } else {
                    logger.info {
                        "Slash command support is disabled - set `enabled` to `true` in the `slashCommands` builder" +
                            " if you want to use them."
                    }
                }
            }

            logger.info { "Ready!" }
        }

        if (settings.messageCommandsBuilder.enabled) {
            on<MessageCreateEvent> {
                getKoin().get<MessageCommandRegistry>().handleEvent(this)
            }
        }

        if (settings.slashCommandsBuilder.enabled) {
            on<InteractionCreateEvent> {
                getKoin().get<SlashCommandRegistry>().handle(this)
            }
        }
    }

    /** This function adds all of the default extensions when the bot is being set up. **/
    public open suspend fun addDefaultExtensions() {
        if (settings.extensionsBuilder.helpExtensionBuilder.enableBundledExtension) {
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
        scope: CoroutineScope = this.getKoin().get<Kord>(),
        noinline consumer: suspend T.() -> Unit
    ): Job =
        events.buffer(Channel.UNLIMITED)
            .filterIsInstance<T>()
            .onEach {
                runCatching {
                    if (launch) scope.launch { consumer(it) } else consumer(it)
                }.onFailure { logger.catching(it) }
            }.catch { logger.catching(it) }
            .launchIn(scope)

    /**
     * @suppress
     */
    public suspend inline fun send(event: Event) {
        eventPublisher.emit(event)
    }

    /**
     * @suppress
     */
    public suspend inline fun send(event: ExtensionEvent) {
        eventPublisher.emit(event)
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
    public open suspend fun addExtension(builder: () -> Extension) {
        val extensionObj = builder.invoke()

        extensions[extensionObj.name] = extensionObj
        loadExtension(extensionObj.name)

        if (!extensionObj.loaded) {
            logger.warn { "Failed to set up extension: ${extensionObj.name}" }
        } else {
            logger.debug { "Loaded extension: ${extensionObj.name}" }

            settings.hooksBuilder.runExtensionAdded(this, extensionObj)
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
     * Find the first loaded extension that is an instance of the type provided in `T`.
     *
     * This can be used to find an extension based on, for example, an implemented interface.
     *
     * @param T Type to match extensions against.
     */
    public inline fun <reified T> findExtension(): T? =
        findExtensions<T>().firstOrNull()

    /**
     * Find all loaded extensions that are instances of the type provided in `T`.
     *
     * This can be used to find extensions based on, for example, an implemented interface.
     *
     * @param T Type to match extensions against.
     */
    public inline fun <reified T> findExtensions(): List<T> =
        extensions.values.filterIsInstance<T>()

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
    @Deprecated(
        "Use the equivalent function within `MessageCommandRegistry` instead.",

        ReplaceWith(
            "getKoin().get<MessageCommandRegistry>().add(command)",

            "org.koin.core.component.KoinComponent.getKoin",
            "com.kotlindiscord.kord.extensions.commands.MessageCommand"
        ),
        level = DeprecationLevel.ERROR
    )
    @Throws(CommandRegistrationException::class)
    public open fun addCommand(command: MessageCommand<out Arguments>): Unit = getKoin()
        .get<MessageCommandRegistry>()
        .add(command)

    /**
     * Directly remove a registered [MessageCommand] from this bot.
     *
     * This function is used when extensions are unloaded, in order to clear out their commands.
     * No exception is thrown if the command wasn't registered.
     *
     * @param command The command to be removed.
     */
    @Deprecated(
        "Use the equivalent function within `MessageCommandRegistry` instead.",

        ReplaceWith(
            "getKoin().get<MessageCommandRegistry>().remove(command)",

            "org.koin.core.component.KoinComponent.getKoin",
            "com.kotlindiscord.kord.extensions.commands.MessageCommand"
        ),
        level = DeprecationLevel.ERROR
    )
    public open fun removeCommand(command: MessageCommand<out Arguments>): Boolean = getKoin()
        .get<MessageCommandRegistry>()
        .remove(command)

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

/**
 * DSL function for creating a bot instance. This is the Kord Extensions entrypoint.
 *
 * `ExtensibleBot(token) { extensions { add(::MyExtension) } }`
 */
@Suppress("FunctionNaming")  // This is a factory function
public suspend fun ExtensibleBot(token: String, builder: suspend ExtensibleBotBuilder.() -> Unit): ExtensibleBot {
    val settings = ExtensibleBotBuilder()

    builder(settings)

    return settings.build(token)
}

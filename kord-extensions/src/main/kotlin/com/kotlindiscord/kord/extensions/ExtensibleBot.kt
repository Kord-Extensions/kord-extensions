package com.kotlindiscord.kord.extensions

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.event.gateway.ReadyEvent
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.on
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.HelpExtension
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

private val logger = KotlinLogging.logger {}

/**
 * An extensible bot, wrapping a Kord instance.
 *
 * This is your jumping-off point. ExtensibleBot provides a system for managing extensions, commands and event
 * handlers. Either subclass ExtensibleBot or use it as-is if it suits your needs.
 *
 * @param addHelpExtension Whether to automatically install the bundled help command extension.
 * @param invokeCommandOnMention Whether to invoke commands that are prefixed with a mention, as well as the prefix.
 * @param prefix The command prefix, for command invocations on Discord.
 * @param token The Discord bot's login token.
 */
open class ExtensibleBot(
    private val token: String,
    val prefix: String,

    val addHelpExtension: Boolean = true,
    val invokeCommandOnMention: Boolean = true
) {
    /**
     * @suppress
     */
    lateinit var kord: Kord  // Kord doesn't allow us to inherit the class, let's wrap it instead

    /**
     * A list of all registered commands.
     */
    val commands: MutableList<Command> = mutableListOf()

    /**
     * A list of all registered event handlers.
     */
    val eventHandlers: MutableList<EventHandler<out Event>> = mutableListOf()

    /**
     * A map of the names of all loaded [Extension]s to their instances.
     */
    val extensions: MutableMap<String, Extension> = mutableMapOf()

    private var initialized: Boolean = false

    /**
     * This function kicks off the process, by setting up the bot and having it login.
     */
    suspend fun start() {
        kord = Kord(token)
        registerListeners()
        addDefaultExtensions()

        kord.login()
    }

    /** This function sets up all of the bot's default event listeners. **/
    private suspend fun registerListeners() {
        kord.on<ReadyEvent> {
            if (!initialized) {  // We do this because a reconnect will cause this event to happen again.
                for (extension in extensions.values) {
                    @Suppress("TooGenericExceptionCaught")  // Anything could happen here
                    try {
                        extension.setup()
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to set up '${extension.name}' extension." }
                    }
                }

                initialized = true
            }

            logger.info { "Ready!" }
        }

        kord.on<MessageCreateEvent> {
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

            command?.call(this, parts)
        }
    }

    /** This function adds all of the default extensions when the bot is being set up. **/
    private suspend fun addDefaultExtensions() {
        if (addHelpExtension) {
            logger.info { "Adding help extension." }
            addExtension(HelpExtension::class)
        }
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
    suspend fun addExtension(extension: KClass<out Extension>) {
        val ctor = extension.primaryConstructor ?: throw InvalidExtensionException(extension, "No primary constructor")

        val extensionObj = ctor.call(this)

        extensions[extensionObj.name] = extensionObj
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
    fun addCommand(command: Command) {
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
    inline fun <reified T : Event> addEventHandler(handler: EventHandler<T>) {
        if (eventHandlers.contains(handler)) {
            throw EventHandlerRegistrationException(
                "Event handler already registered in '${handler.extension.name}' extension."
            )
        }

        kord.on<T> { handler.call(this) }

        eventHandlers.add(handler)
    }
}

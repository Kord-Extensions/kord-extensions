package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.KoinAccessor
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.parse
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.invoke
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.Executors

/**
 * A class for the registration and dispatching of message-based commands.
 *
 * @param bot Current instance of the bot.
 */
@OptIn(KoinApiExtension::class, KordPreview::class)
public open class MessageCommandRegistry(
    public open val bot: ExtensibleBot,
    koinAccessor: KoinComponent = KoinAccessor(bot)
) : KoinComponent by koinAccessor {
    /**
     * A list of all registered commands.
     */
    public open val commands: MutableList<MessageCommand<out Arguments>> = mutableListOf()

    /** @suppress **/
    public val settings: ExtensibleBotBuilder by inject()

    /** @suppress **/
    public open val commandThreadPool: ExecutorCoroutineDispatcher by lazy {
        Executors
            .newFixedThreadPool(settings.messageCommandsBuilder.threads)
            .asCoroutineDispatcher()
    }

    /**
     * Directly register a [MessageCommand] to this command registry.
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
    public open fun add(command: MessageCommand<out Arguments>) {
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
     * Directly remove a registered [MessageCommand] from this command registry.
     *
     * This function is used when extensions are unloaded, in order to clear out their commands.
     * No exception is thrown if the command wasn't registered.
     *
     * @param command The command to be removed.
     */
    public open fun remove(command: MessageCommand<out Arguments>): Boolean = commands.remove(command)

    /**
     * Given a [MessageCreateEvent], return the prefix that should be used for a command invocation.
     *
     * By default, this can be set in [ExtensibleBotBuilder] - but you can override this function in subclasses if
     * needed.
     */
    public open suspend fun getPrefix(event: MessageCreateEvent): String =
        settings.messageCommandsBuilder.prefixCallback(event, settings.messageCommandsBuilder.defaultPrefix)

    /**
     * Check whether the given string starts with a mention referring to the bot. If so, the matching mention string
     * is returned, otherwise `null`.
     */
    public open fun String.startsWithSelfMention(): String? {
        val mention = "<@${bot.kord.selfId.value}>"
        val nickMention = "<@!${bot.kord.selfId.value}>"

        return when {
            startsWith(mention) -> mention
            startsWith(nickMention) -> nickMention

            else -> null
        }
    }

    /**
     * Handles an incoming [MessageCreateEvent] and dispatches a command invocation, if possible.
     */
    public open suspend fun handleEvent(event: MessageCreateEvent) {
        val message = event.message
        var commandName: String? = null
        var parts = message.parse()
        val prefix = getPrefix(event)

        if (parts.isEmpty()) {
            // Empty message.
            return
        }

        if (parts.size == 1) {
            // It's just the command with no arguments

            if (parts[0].startsWith(prefix)) {
                commandName = parts[0]
                parts = arrayOf()
            } else {
                // Doesn't start with the right prefix
                return
            }
        } else {
            val matchedMention = parts[0].startsWithSelfMention()

            when {
                parts[0].startsWith(prefix) -> {
                    // MessageCommand with args

                    commandName = parts[0]
                    parts = parts.sliceArray(1 until parts.size)
                }

                settings.messageCommandsBuilder.invokeOnMention &&
                    matchedMention != null && parts[0] == matchedMention -> {
                    // MessageCommand with a mention; first part is exactly the mention

                    commandName = parts[1]

                    parts = if (parts.size > 2) {
                        parts.sliceArray(2 until parts.size)
                    } else {
                        arrayOf()
                    }
                }

                settings.messageCommandsBuilder.invokeOnMention &&
                    matchedMention != null && parts[0].startsWith(matchedMention) -> {
                    // MessageCommand with a mention; no space between mention and command

                    commandName = parts[0].slice(matchedMention.length until parts[0].length)
                    parts = parts.sliceArray(1 until parts.size)
                }
            }
        }

        if (commandName == null || commandName == prefix) {
            return  // After all that, we couldn't find a command.
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

        commandThreadPool.invoke {
            command?.call(event, commandName, parts)
        }
    }
}

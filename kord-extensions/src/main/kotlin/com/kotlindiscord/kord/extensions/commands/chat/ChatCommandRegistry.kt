package com.kotlindiscord.kord.extensions.commands.chat

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * A class for the registration and dispatching of message-based commands.
 */
@OptIn(KordPreview::class)
public open class ChatCommandRegistry : KoinComponent {
    /** Current instance of the bot. **/
    public val bot: ExtensibleBot by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /** Chat command parser object. **/
    public open val parser: ChatCommandParser = ChatCommandParser()

    /**
     * A list of all registered commands.
     */
    public open val commands: MutableList<ChatCommand<out Arguments>> = mutableListOf()

    /** @suppress **/
    public val botSettings: ExtensibleBotBuilder by inject()

    /** Whether chat commands are enabled in the bot's settings. **/
    public open val enabled: Boolean get() = botSettings.chatCommandsBuilder.enabled

    /**
     * Directly register a [ChatCommand] to this command registry.
     *
     * Generally speaking, you shouldn't call this directly - instead, create an [Extension] and
     * call the [Extension.messageContentCommand] function in your [Extension.setup] function.
     *
     * This function will throw a [CommandRegistrationException] if the command has already been registered, if
     * a command with the same name exists, or if a command with one of the same aliases exists.
     *
     * @param command The command to be registered.
     * @throws CommandRegistrationException Thrown if the command could not be registered.
     */
    @Throws(CommandRegistrationException::class)
    public open fun add(command: ChatCommand<out Arguments>) {
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
     * Directly remove a registered [ChatCommand] from this command registry.
     *
     * This function is used when extensions are unloaded, in order to clear out their commands.
     * No exception is thrown if the command wasn't registered.
     *
     * @param command The command to be removed.
     */
    public open fun remove(command: ChatCommand<out Arguments>): Boolean = commands.remove(command)

    /**
     * Given a [MessageCreateEvent], return the prefix that should be used for a command invocation.
     *
     * By default, this can be set in [ExtensibleBotBuilder] - but you can override this function in subclasses if
     * needed.
     */
    public open suspend fun getPrefix(event: MessageCreateEvent): String =
        botSettings.chatCommandsBuilder.prefixCallback(event, botSettings.chatCommandsBuilder.defaultPrefix)

    /**
     * Check whether the given string starts with a mention referring to the bot. If so, the matching mention string
     * is returned, otherwise `null`.
     */
    public open fun String.startsWithSelfMention(): String? {
        val mention = "<@${kord.selfId.value}>"
        val nickMention = "<@!${kord.selfId.value}>"

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
        var commandName: String?
        val prefix = getPrefix(event)
        var content = message.content

        if (content.isEmpty()) {
            // Empty message.
            return
        }

        val mention = content.startsWithSelfMention()

        content = when {
            // Starts with the right mention and mentions are allowed, so remove it
            mention != null && botSettings.chatCommandsBuilder.invokeOnMention -> content.substring(mention.length)

            // Starts with the right prefix, so remove it
            content.startsWith(prefix) -> content.substring(prefix.length)

            // Not a valid command, so stop here
            else -> return
        }.trim()  // Remove unnecessary spaces

        commandName = content.split(" ").first()
        content = content.substring(commandName.length).trim()  // Remove the command name and extra whitespace

//        if (parts.size == 1) {
//            // It's just the command with no arguments
//
//            if (parts[0].startsWith(prefix)) {
//                commandName = parts[0]
//                parts = arrayOf()
//            } else {
//                // Doesn't start with the right prefix
//                return
//            }
//        } else {
//            val matchedMention = parts[0].startsWithSelfMention()
//
//            when {
//                parts[0].startsWith(prefix) -> {
//                    // MessageCommand with args
//
//                    commandName = parts[0]
//                    parts = parts.sliceArray(1 until parts.size)
//
//                    argString = argString.replaceFirst(prefix, "")
//                        .trim()
//                }
//
//                botSettings.messageCommandsBuilder.invokeOnMention &&
//                    matchedMention != null && parts[0] == matchedMention -> {
//                    // MessageCommand with a mention; first part is exactly the mention
//
//                    commandName = parts[1]
//
//                    parts = if (parts.size > 2) {
//                        parts.sliceArray(2 until parts.size)
//                    } else {
//                        arrayOf()
//                    }
//
//                    argString = argString.replaceFirst(matchedMention, "")
//                        .trim()
//                }
//
//                botSettings.messageCommandsBuilder.invokeOnMention &&
//                    matchedMention != null && parts[0].startsWith(matchedMention) -> {
//                    // MessageCommand with a mention; no space between mention and command
//
//                    commandName = parts[0].slice(matchedMention.length until parts[0].length)
//                    parts = parts.sliceArray(1 until parts.size)
//
//                    argString = argString.replaceFirst(matchedMention, "")
//                        .trim()
//                }
//            }
//        }
//
//        if (commandName == null || commandName == prefix) {
//            return  // After all that, we couldn't find a command.
//        }
//
//        if (commandName.startsWith(prefix)) {
//            commandName = commandName.slice(prefix.length until commandName.length)
//        }
//
//        argString = argString.replaceFirst(commandName, "")
//            .trim()
//
//        if (commandName.contains("\n")) {
//            val split = commandName.split("\n", limit = 2)
//
//            commandName = split.first()
//
//            parts = if (parts.isEmpty()) {
//                arrayOf("\n${split.last()}")
//            } else {
//                arrayOf("\n${split.last()}") + parts
//            }
//        }

        commandName = commandName.lowercase()

        val command = getCommand(commandName, event)
        val parser = StringParser(content)

        command?.call(event, commandName, parser, content)
    }

    /**
     * Given a command name and [MessageCreateEvent], try to find a matching command.
     *
     * If a command supports locale fallback, this will also attempt to resolve names via the bot's default locale.
     */
    public open suspend fun getCommand(name: String, event: MessageCreateEvent): ChatCommand<out Arguments>? {
        val defaultLocale = botSettings.i18nBuilder.defaultLocale
        val locale = event.getLocale()

        return commands.firstOrNull { it.getTranslatedName(locale) == name }
            ?: commands.firstOrNull { it.getTranslatedAliases(locale).contains(name) }
            ?: commands.firstOrNull { it.localeFallback && it.getTranslatedName(defaultLocale) == name }
            ?: commands.firstOrNull { it.localeFallback && it.getTranslatedAliases(defaultLocale).contains(name) }
    }
}

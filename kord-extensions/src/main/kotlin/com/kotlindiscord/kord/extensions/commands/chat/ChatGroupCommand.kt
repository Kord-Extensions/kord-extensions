package com.kotlindiscord.kord.extensions.commands.chat

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.core.event.message.MessageCreateEvent
import mu.KotlinLogging
import org.koin.core.component.inject
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Class representing a grouped command, which is essentially a command with its own subcommands.
 *
 * You shouldn't need to use this class directly - instead, create an `Extension` and use the
 * `group` function to register your command group, by overriding the `Extension` setup function.
 *
 * @param extension The extension that registered this grouped command.
 * @param parent The [ChatGroupCommand] this group exists under, if any.
 */
@Suppress("LateinitVarOverridesLateinitVar")
// This is intentional
@ExtensionDSL
public open class ChatGroupCommand<T : Arguments>(
    extension: Extension,
    arguments: (() -> T)? = null,
    public open val parent: ChatGroupCommand<out Arguments>? = null
) : ChatCommand<T>(extension, arguments) {
    /** @suppress **/
    public val botSettings: ExtensibleBotBuilder by inject()

    /** @suppress **/
    public open val commands: MutableList<ChatCommand<out Arguments>> = mutableListOf()

    override lateinit var name: String

    /** @suppress **/
    override var body: suspend ChatCommandContext<out T>.() -> Unit = {
        sendHelp()
    }

    override suspend fun runChecks(event: MessageCreateEvent, sendMessage: Boolean): Boolean {
        var result = parent?.runChecks(event, sendMessage) ?: true

        if (result) {
            result = super.runChecks(event, sendMessage)
        }

        return result
    }

    /**
     * An internal function used to ensure that all of a command group's required arguments are present.
     *
     * @throws InvalidCommandException Thrown when a required argument hasn't been set.
     */
    @Throws(InvalidCommandException::class)
    override fun validate() {
        if (!::name.isInitialized) {
            throw InvalidCommandException(null, "No command name given.")
        }

        if (commands.isEmpty()) {
            throw InvalidCommandException(name, "No subcommands registered.")
        }
    }

    /**
     * DSL function for easily registering a command.
     *
     * Use this in your setup function to register a command that may be executed on Discord.
     *
     * @param body Builder lambda used for setting up the command object.
     */
    @ExtensionDSL
    public open suspend fun <R : Arguments> chatCommand(
        arguments: (() -> R)?,
        body: suspend ChatCommand<R>.() -> Unit
    ): ChatCommand<R> {
        val commandObj = ChatSubCommand<R>(extension, arguments, this)
        body.invoke(commandObj)

        return chatCommand(commandObj)
    }

    /**
     * DSL function for easily registering a command, without arguments.
     *
     * Use this in your setup function to register a command that may be executed on Discord.
     *
     * @param body Builder lambda used for setting up the command object.
     */
    @ExtensionDSL
    public open suspend fun chatCommand(
        body: suspend ChatCommand<Arguments>.() -> Unit
    ): ChatCommand<Arguments> {
        val commandObj = ChatSubCommand<Arguments>(extension, parent = this)
        body.invoke(commandObj)

        return chatCommand(commandObj)
    }

    /**
     * Function for registering a custom command object.
     *
     * You can use this if you have a custom command subclass you need to register.
     *
     * @param commandObj MessageCommand object to register.
     */
    @ExtensionDSL
    public open suspend fun <R : Arguments> chatCommand(
        commandObj: ChatCommand<R>
    ): ChatCommand<R> {
        try {
            commandObj.validate()
            commands.add(commandObj)
        } catch (e: CommandRegistrationException) {
            logger.error(e) { "Failed to register subcommand - $e" }
        } catch (e: InvalidCommandException) {
            logger.error(e) { "Failed to register subcommand - $e" }
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
    @Suppress("MemberNameEqualsClassName")  // Really?
    public open suspend fun <R : Arguments> chatGroupCommand(
        arguments: (() -> R)?,
        body: suspend ChatGroupCommand<R>.() -> Unit
    ): ChatGroupCommand<R> {
        val commandObj = ChatGroupCommand(extension, arguments, this)
        body.invoke(commandObj)

        return chatCommand(commandObj) as ChatGroupCommand<R>
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
    @Suppress("MemberNameEqualsClassName")  // Really?
    public open suspend fun chatGroupCommand(
        body: suspend ChatGroupCommand<Arguments>.() -> Unit
    ): ChatGroupCommand<Arguments> {
        val commandObj = ChatGroupCommand<Arguments>(extension, parent = this)
        body.invoke(commandObj)

        return chatCommand(commandObj) as ChatGroupCommand<Arguments>
    }

    /** @suppress **/
    public open suspend fun getCommand(
        name: String?,
        event: MessageCreateEvent
    ): ChatCommand<out Arguments>? {
        name ?: return null

        val defaultLocale = botSettings.i18nBuilder.defaultLocale
        val locale = event.getLocale()

        return commands.firstOrNull { it.getTranslatedName(locale) == name }
            ?: commands.firstOrNull { it.getTranslatedAliases(locale).contains(name) }
            ?: commands.firstOrNull { it.localeFallback && it.getTranslatedName(defaultLocale) == name }
            ?: commands.firstOrNull { it.localeFallback && it.getTranslatedAliases(defaultLocale).contains(name) }
    }

    /**
     * Execute this grouped command, given a [MessageCreateEvent].
     *
     * This function takes a [MessageCreateEvent] (generated when a message is received), and
     * processes it. The command's checks are invoked and, assuming all of the
     * checks passed, the command will search for a subcommand matching the first argument.
     * If a subcommand is found, it will be executed - otherwise, the the
     * [command body][action] is executed.
     *
     * If an exception is thrown by the [command body][action], it is caught and a traceback
     * is printed.
     *
     * @param event The message creation event.
     */
    override suspend fun call(
        event: MessageCreateEvent,
        commandName: String,
        parser: StringParser,
        argString: String,
        skipChecks: Boolean
    ) {
        if (skipChecks || !runChecks(event)) {
            return
        }

        val command = parser.peekNext()?.data?.lowercase()
        val subCommand = getCommand(command, event)

        if (subCommand == null) {
            super.call(event, commandName, parser, argString, true)
        } else {
            parser.parseNext()  // Advance the cursor so proper parsing can happen

            subCommand.call(event, commandName, StringParser(parser.consumeRemaining()), argString)
        }
    }

    /** Get the full command name, translated, with parent commands taken into account. **/
    public open suspend fun getFullTranslatedName(locale: Locale): String {
        parent ?: return this.getTranslatedName(locale)

        return parent!!.getFullTranslatedName(locale) + " " + this.getTranslatedName(locale)
    }
}

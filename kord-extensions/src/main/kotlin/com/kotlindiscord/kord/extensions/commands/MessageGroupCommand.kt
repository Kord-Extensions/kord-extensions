package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
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
 * @param parent The [GroupCommand] this group exists under, if any.
 */
@Suppress("LateinitVarOverridesLateinitVar")
// This is intentional
@ExtensionDSL
public open class GroupCommand<T : Arguments>(
    extension: Extension,
    arguments: (() -> T)? = null,
    public open val parent: GroupCommand<out Arguments>? = null
) : MessageCommand<T>(extension, arguments) {
    /** @suppress **/
    public val botSettings: ExtensibleBotBuilder by inject()

    /** @suppress **/
    public open val commands: MutableList<MessageCommand<out Arguments>> = mutableListOf()

    override lateinit var name: String

    /** @suppress **/
    override var body: suspend MessageCommandContext<out T>.() -> Unit = {
        sendHelp()
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
    public open suspend fun <R : Arguments> command(
        arguments: (() -> R)?,
        body: suspend MessageCommand<R>.() -> Unit
    ): MessageCommand<R> {
        val commandObj = MessageSubCommand<R>(extension, arguments, this)
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
    public open suspend fun command(
        body: suspend MessageCommand<Arguments>.() -> Unit
    ): MessageCommand<Arguments> {
        val commandObj = MessageSubCommand<Arguments>(extension, parent = this)
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
    public open suspend fun <R : Arguments> command(commandObj: MessageCommand<R>): MessageCommand<R> {
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
    public open suspend fun <R : Arguments> group(
        arguments: (() -> R)?,
        body: suspend GroupCommand<R>.() -> Unit
    ): GroupCommand<R> {
        val commandObj = GroupCommand(extension, arguments, this)
        body.invoke(commandObj)

        return command(commandObj) as GroupCommand<R>
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
    public open suspend fun group(
        body: suspend GroupCommand<Arguments>.() -> Unit
    ): GroupCommand<Arguments> {
        val commandObj = GroupCommand<Arguments>(extension, parent = this)
        body.invoke(commandObj)

        return command(commandObj) as GroupCommand<Arguments>
    }

    /** @suppress **/
    public open suspend fun getCommand(name: String?, event: MessageCreateEvent): MessageCommand<out Arguments>? {
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

        val command = parser.parseNext()?.data?.lowercase()
        val remainingArgs = parser.consumeRemaining()
        val subCommand = getCommand(command, event)

        if (subCommand == null) {
            super.call(event, commandName, parser, argString, true)
        } else {
            subCommand.call(event, commandName, StringParser(remainingArgs), argString)
        }
    }

    /** Get the full command name, translated, with parent commands taken into account. **/
    public open suspend fun getFullTranslatedName(locale: Locale): String {
        parent ?: return this.getTranslatedName(locale)

        return parent!!.getFullTranslatedName(locale) + " " + this.getTranslatedName(locale)
    }
}

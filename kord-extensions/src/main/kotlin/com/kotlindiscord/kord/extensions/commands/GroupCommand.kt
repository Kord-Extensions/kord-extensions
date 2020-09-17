package com.kotlindiscord.kord.extensions.commands

import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.CommandRegistrationException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.extensions.Extension
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Class representing a grouped command, which is essentially a command with its own subcommands.
 *
 * You shouldn't need to use this class directly - instead, create an `Extension` and use the
 * `group` function to register your command group, by overriding the `Extension` setup function.
 *
 * @param extension The extension that registered this grouped command.
 */
class GroupCommand(extension: Extension) : Command(extension) {
    /** @suppress **/
    val commands = mutableListOf<Command>()

    /** @suppress **/
    override var body: suspend CommandContext.() -> Unit = {
        val mention = message.author!!.mention

        val error = if (args.size > 0) {
            "$mention Unknown subcommand: ${args.first()}"
        } else {
            "$mention Subcommands: " + commands.joinToString(", ") { "`${it.name}`" }
        }

        message.channel.createMessage(error)
    }

    /**
     * DSL function for easily registering a command.
     *
     * Use this in your setup function to register a command that may be executed on Discord.
     *
     * @param body Builder lambda used for setting up the command object.
     */
    suspend fun command(body: suspend Command.() -> Unit): Command {
        val commandObj = Command(extension)

        body.invoke(commandObj)

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
    suspend fun group(body: suspend GroupCommand.() -> Unit): GroupCommand {
        val commandObj = GroupCommand(extension)

        body.invoke(commandObj)

        try {
            commandObj.validate()
            commands.add(commandObj)
        } catch (e: CommandRegistrationException) {
            logger.error(e) { "Failed to register command - $e" }
        } catch (e: InvalidCommandException) {
            logger.error(e) { "Failed to register command - $e" }
        }

        return commandObj
    }

    /** @suppress **/
    fun getCommand(commandName: String?) =
        commands.firstOrNull { it.name == commandName } ?: commands.firstOrNull { it.aliases.contains(commandName) }

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
    override suspend fun call(event: MessageCreateEvent, args: Array<String>, skipChecks: Boolean) {
        if (skipChecks || !runChecks(event)) {
            return
        }

        val commandName = args.firstOrNull()?.toLowerCase()
        val remainingArgs = args.drop(1).toTypedArray()
        val subCommand = getCommand(commandName)

        if (subCommand == null) {
            super.call(event, args, true)
        } else {
            subCommand.call(event, remainingArgs)
        }
    }
}

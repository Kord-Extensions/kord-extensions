package com.kotlindiscord.kord.extensions.commands

import dev.kord.core.event.message.MessageCreateEvent
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
 * @param parent The [GroupCommand] this group exists under, if any.
 */
@Suppress("LateinitVarOverridesLateinitVar")  // This is intentional
public open class GroupCommand(
    extension: Extension,
    public open val parent: GroupCommand? = null
) : MessageCommand(extension) {
    /** @suppress **/
    public open val commands: MutableList<MessageCommand> = mutableListOf<MessageCommand>()

    override lateinit var name: String

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

    /** @suppress **/
    override var body: suspend MessageCommandContext.() -> Unit = {
        val mention = message.author!!.mention

        val error = if (args.isNotEmpty()) {
            "$mention Unknown subcommand: `${args.first()}`"
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
    public open suspend fun command(body: suspend MessageCommand.() -> Unit): MessageCommand {
        val commandObj = MessageSubCommand(extension, this)
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
    public open suspend fun command(commandObj: MessageCommand): MessageCommand {
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
    public open suspend fun group(body: suspend GroupCommand.() -> Unit): GroupCommand {
        val commandObj = GroupCommand(extension, this)
        body.invoke(commandObj)

        return command(commandObj) as GroupCommand
    }

    /** @suppress **/
    public open fun getCommand(commandName: String?): MessageCommand? =
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
    override suspend fun call(
        event: MessageCreateEvent,
        commandName: String,
        args: Array<String>,
        skipChecks: Boolean
    ) {
        if (skipChecks || !runChecks(event)) {
            return
        }

        val command = args.firstOrNull()?.toLowerCase()
        val remainingArgs = args.drop(1).toTypedArray()
        val subCommand = getCommand(command)

        if (subCommand == null) {
            super.call(event, commandName, args, true)
        } else {
            subCommand.call(event, commandName, remainingArgs)
        }
    }

    /**
     * Get the name of this command, prefixed with the name of its parent (separated by spaces),
     * or just the command's name if there is no parent.
     */
    public open fun getFullName(): String {
        parent ?: return this.name

        return parent!!.getFullName() + " " + this.name
    }
}
